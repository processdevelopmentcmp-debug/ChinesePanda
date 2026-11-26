package com.example.data

import com.example.data.local.DailyDayPoint
import com.example.data.local.DailyMission
import com.example.data.local.DailyStudyLogDao
import com.example.data.local.DailyStudyLogEntity
import com.example.data.local.DailyTrackerDateUtil
import com.example.data.local.DailyTrackerSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DailyLearningTrackerRepository(
    private val dao: DailyStudyLogDao,
    private val displayPrefs: AppDisplayPrefs
) {
    private val writeMutex = Mutex()

    fun observeTrackerSnapshot(userId: String): Flow<DailyTrackerSnapshot> {
        return combine(
            dao.observeAllLogsForUser(userId),
            displayPrefs.dailyXpGoal,
            displayPrefs.dailyWordsGoal
        ) { allLogs, xpGoal, wordsGoal ->
            val nowMs = System.currentTimeMillis()
            val todayKey = DailyTrackerDateUtil.todayKey(nowMs)
            val byDate = allLogs.associateBy { it.dateKey }
            val todayLog = byDate[todayKey] ?: DailyStudyLogEntity(
                userId = userId,
                dateKey = todayKey
            )

            val last28Days = (27 downTo 0).map { daysAgo ->
                val dateKey = DailyTrackerDateUtil.dateKeyDaysAgo(daysAgo, nowMs)
                val log = byDate[dateKey]
                val xp = log?.xpEarned ?: 0
                val words = log?.wordsLearned ?: 0
                DailyDayPoint(
                    dateKey = dateKey,
                    dayShortLabel = DailyTrackerDateUtil.shortWeekdayLabel(daysAgo, nowMs),
                    dayOfMonth = DailyTrackerDateUtil.dayOfMonth(daysAgo, nowMs),
                    isToday = daysAgo == 0,
                    xpEarned = xp,
                    wordsLearned = words,
                    lessonsCompleted = log?.lessonsCompleted ?: 0,
                    srsReviewed = log?.srsReviewed ?: 0,
                    storiesExplored = log?.storiesExplored ?: 0,
                    practiceSessions = log?.practiceSessions ?: 0,
                    studyMinutes = log?.studyMinutes ?: 0,
                    goalReached = xp >= xpGoal || words >= wordsGoal
                )
            }
            val last7Days = last28Days.takeLast(7)

            val activeDateKeys = allLogs
                .filter { it.hasAnyStudy }
                .map { it.dateKey }
                .toSet()
            val (currentStreak, bestStreak) = DailyTrackerDateUtil.computeStreaks(activeDateKeys, nowMs)

            val missions = listOf(
                DailyMission(
                    id = "mission_lesson",
                    title = "Complete 1 HSK Lesson",
                    subtitle = "Finish any 10-word or Core Path lesson",
                    current = todayLog.lessonsCompleted,
                    target = 1,
                    targetNavTab = 0
                ),
                DailyMission(
                    id = "mission_srs",
                    title = "Review 5 Flashcards",
                    subtitle = "Strengthen long-term memory in SM-2 Review",
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
                todayLog = todayLog,
                xpGoal = xpGoal,
                wordsGoal = wordsGoal,
                last7Days = last7Days,
                last28Days = last28Days,
                currentStreakDays = currentStreak,
                bestStreakDays = bestStreak,
                totalActiveDays = activeDateKeys.size,
                weeklyXpTotal = last7Days.sumOf { it.xpEarned },
                weeklyWordsTotal = last7Days.sumOf { it.wordsLearned },
                missions = missions
            )
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
