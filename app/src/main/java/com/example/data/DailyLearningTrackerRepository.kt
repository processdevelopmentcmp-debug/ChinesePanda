package com.example.data

import com.example.data.local.DailyDayPoint
import com.example.data.local.DailyMission
import com.example.data.local.DailyStudyLogDao
import com.example.data.local.DailyStudyLogEntity
import com.example.data.local.DailyTrackerDateUtil
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.DailyWordGoalCalculator
import com.example.data.local.DailyWordGoalUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private data class DailyGoalConfigTuple(
    val xpGoal: Int,
    val wordsGoal: Int,
    val unit: DailyWordGoalUnit,
    val hskLevel: String
)

class DailyLearningTrackerRepository(
    private val dao: DailyStudyLogDao,
    private val displayPrefs: AppDisplayPrefs
) {
    private val writeMutex = Mutex()

    fun observeTrackerSnapshot(userId: String): Flow<DailyTrackerSnapshot> {
        val goalConfigFlow = combine(
            displayPrefs.dailyXpGoal,
            displayPrefs.dailyWordsGoal,
            displayPrefs.dailyWordGoalUnit,
            displayPrefs.dailyWordGoalHskLevel
        ) { xpGoal, wordsGoal, unit, hskLevel ->
            DailyGoalConfigTuple(xpGoal, wordsGoal, unit, hskLevel)
        }

        return combine(
            dao.observeAllLogsForUser(userId),
            goalConfigFlow,
            displayPrefs.acquiredWordsRevision
        ) { allLogs, config, _ ->
            val nowMs = System.currentTimeMillis()
            val todayKey = DailyTrackerDateUtil.todayKey(nowMs)
            val byDate = allLogs.associateBy { it.dateKey }
            val todayLog = byDate[todayKey] ?: DailyStudyLogEntity(
                userId = userId,
                dateKey = todayKey
            )

            val todayAcquiredWords = displayPrefs.getAcquiredWordsForDay(userId, todayKey)
            val todayUniqueChars = DailyWordGoalCalculator.countUniqueCjkCharacters(todayAcquiredWords)
            val todayEffectiveWordCount = when (config.unit) {
                DailyWordGoalUnit.WORDS -> maxOf(todayLog.wordsLearned, todayAcquiredWords.size)
                DailyWordGoalUnit.CHARACTERS -> maxOf(todayUniqueChars, todayLog.wordsLearned)
            }

            val last28Days = (27 downTo 0).map { daysAgo ->
                val dateKey = DailyTrackerDateUtil.dateKeyDaysAgo(daysAgo, nowMs)
                val log = byDate[dateKey]
                val xp = log?.xpEarned ?: 0
                val dayAcquiredWords = if (daysAgo == 0) {
                    todayAcquiredWords
                } else {
                    displayPrefs.getAcquiredWordsForDay(userId, dateKey)
                }
                val wordsCount = when {
                    daysAgo == 0 -> todayEffectiveWordCount
                    config.unit == DailyWordGoalUnit.CHARACTERS && dayAcquiredWords.isNotEmpty() ->
                        maxOf(DailyWordGoalCalculator.countUniqueCjkCharacters(dayAcquiredWords), log?.wordsLearned ?: 0)
                    else -> maxOf(log?.wordsLearned ?: 0, dayAcquiredWords.size)
                }
                val hitWordGoal = wordsCount >= config.wordsGoal
                DailyDayPoint(
                    dateKey = dateKey,
                    dayShortLabel = DailyTrackerDateUtil.shortWeekdayLabel(daysAgo, nowMs),
                    dayOfMonth = DailyTrackerDateUtil.dayOfMonth(daysAgo, nowMs),
                    isToday = daysAgo == 0,
                    xpEarned = xp,
                    wordsLearned = wordsCount,
                    lessonsCompleted = log?.lessonsCompleted ?: 0,
                    srsReviewed = log?.srsReviewed ?: 0,
                    storiesExplored = log?.storiesExplored ?: 0,
                    practiceSessions = log?.practiceSessions ?: 0,
                    studyMinutes = log?.studyMinutes ?: 0,
                    goalReached = xp >= config.xpGoal || hitWordGoal,
                    wordGoalReached = hitWordGoal
                )
            }
            val last7Days = last28Days.takeLast(7)

            val activeDateKeys = buildSet {
                allLogs.filter { it.hasAnyStudy }.forEach { add(it.dateKey) }
                if (todayAcquiredWords.isNotEmpty()) add(todayKey)
            }
            val (currentStreak, bestStreak) = DailyTrackerDateUtil.computeStreaks(activeDateKeys, nowMs)

            val wordGoalHitKeys = buildSet {
                last28Days.filter { it.wordGoalReached }.forEach { add(it.dateKey) }
                allLogs.filter { it.wordsLearned >= config.wordsGoal }.forEach { add(it.dateKey) }
            }
            val (wordGoalStreak, _) = DailyTrackerDateUtil.computeStreaks(wordGoalHitKeys, nowMs)
            val wordGoalDaysHitLast7 = last7Days.count { it.wordGoalReached }

            val hskLabel = DailyWordGoalCalculator.formatHskScopeLabel(config.hskLevel)
            val unitWord = if (config.unit == DailyWordGoalUnit.CHARACTERS) "Characters" else "Words"

            val missions = listOf(
                DailyMission(
                    id = "mission_word_goal",
                    title = "Learn ${config.wordsGoal} New $hskLabel $unitWord",
                    subtitle = "Your daily acquisition goal · ${(config.wordsGoal - todayEffectiveWordCount).coerceAtLeast(0)} remaining today",
                    current = todayEffectiveWordCount,
                    target = config.wordsGoal,
                    targetNavTab = 0
                ),
                DailyMission(
                    id = "mission_srs",
                    title = "Review 5 Flashcards",
                    subtitle = "Strengthen long-term memory in Anki / SM-2 Review",
                    current = todayLog.srsReviewed,
                    target = 5,
                    targetNavTab = 3
                ),
                DailyMission(
                    id = "mission_stories",
                    title = "Explore 3 Character Stories",
                    subtitle = "Learn radicals & history via CharacterPop / HanziCraft",
                    current = todayLog.storiesExplored,
                    target = 3,
                    targetNavTab = 1
                ),
                DailyMission(
                    id = "mission_practice",
                    title = "Practice Writing or AI Tutor",
                    subtitle = "Chat with Panda Laoshi or grade a sentence",
                    current = todayLog.practiceSessions,
                    target = 2,
                    targetNavTab = 4
                )
            )

            DailyTrackerSnapshot(
                todayDateKey = todayKey,
                todayLog = todayLog.copy(wordsLearned = todayEffectiveWordCount),
                xpGoal = config.xpGoal,
                wordsGoal = config.wordsGoal,
                wordGoalUnit = config.unit,
                wordGoalHskLevel = config.hskLevel,
                todayAcquiredWords = todayAcquiredWords,
                todayUniqueCharacters = todayUniqueChars,
                last7Days = last7Days,
                last28Days = last28Days,
                currentStreakDays = currentStreak,
                bestStreakDays = bestStreak,
                wordGoalDaysHitLast7 = wordGoalDaysHitLast7,
                wordGoalStreakDays = wordGoalStreak,
                totalActiveDays = activeDateKeys.size,
                weeklyXpTotal = last7Days.sumOf { it.xpEarned },
                weeklyWordsTotal = last7Days.sumOf { it.wordsLearned },
                missions = missions
            )
        }
    }

    /**
     * Records genuinely new HSK words acquired today, deduplicating within the current day
     * so re-studying the same word does not double-count daily acquisition progress.
     */
    suspend fun recordNewWordsAcquired(
        userId: String,
        newHanzi: Collection<String>
    ): Int {
        if (userId.isBlank() || newHanzi.isEmpty()) return 0
        return withContext(Dispatchers.IO) {
            writeMutex.withLock {
                val nowMs = System.currentTimeMillis()
                val dateKey = DailyTrackerDateUtil.todayKey(nowMs)
                val addedToday = displayPrefs.addAcquiredWordsForDay(userId, dateKey, newHanzi)
                if (addedToday > 0) {
                    val todayWords = displayPrefs.getAcquiredWordsForDay(userId, dateKey)
                    val existing = dao.getLogForDay(userId, dateKey)
                        ?: DailyStudyLogEntity(userId = userId, dateKey = dateKey)
                    val updatedCount = maxOf(existing.wordsLearned + addedToday, todayWords.size)
                    dao.upsertLog(
                        existing.copy(
                            wordsLearned = updatedCount,
                            updatedAtMs = nowMs
                        )
                    )
                }
                addedToday
            }
        }
    }

    suspend fun recordActivity(
        userId: String,
        xpDelta: Int = 0,
        wordsDelta: Int = 0,
        lessonsDelta: Int = 0,
        srsDelta: Int = 0,
        storiesDelta: Int = 0,
        practiceDelta: Int = 0,
        minutesDelta: Int = 0
    ) {
        if (userId.isBlank()) return
        if (xpDelta == 0 && wordsDelta == 0 && lessonsDelta == 0 && srsDelta == 0 &&
            storiesDelta == 0 && practiceDelta == 0 && minutesDelta == 0
        ) return

        withContext(Dispatchers.IO) {
            writeMutex.withLock {
                val nowMs = System.currentTimeMillis()
                val dateKey = DailyTrackerDateUtil.todayKey(nowMs)
                val existing = dao.getLogForDay(userId, dateKey)
                    ?: DailyStudyLogEntity(userId = userId, dateKey = dateKey)
                val updated = existing.copy(
                    xpEarned = (existing.xpEarned + xpDelta).coerceAtLeast(0),
                    wordsLearned = (existing.wordsLearned + wordsDelta).coerceAtLeast(0),
                    lessonsCompleted = (existing.lessonsCompleted + lessonsDelta).coerceAtLeast(0),
                    srsReviewed = (existing.srsReviewed + srsDelta).coerceAtLeast(0),
                    storiesExplored = (existing.storiesExplored + storiesDelta).coerceAtLeast(0),
                    practiceSessions = (existing.practiceSessions + practiceDelta).coerceAtLeast(0),
                    studyMinutes = (existing.studyMinutes + minutesDelta).coerceAtLeast(0),
                    updatedAtMs = nowMs
                )
                dao.upsertLog(updated)
            }
        }
    }
}
