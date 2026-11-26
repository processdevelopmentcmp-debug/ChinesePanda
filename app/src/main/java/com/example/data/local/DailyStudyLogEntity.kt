package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class DailyWordGoalUnit(
    val id: String,
    val label: String,
    val shortUnit: String,
    val description: String
) {
    WORDS(
        id = "words",
        label = "New HSK Words",
        shortUnit = "words",
        description = "Count each new HSK vocabulary word or phrase acquired today"
    ),
    CHARACTERS(
        id = "characters",
        label = "New Hanzi (汉字)",
        shortUnit = "chars",
        description = "Count each distinct Chinese character (汉字) learned today"
    );

    companion object {
        fun fromId(id: String?): DailyWordGoalUnit =
            entries.firstOrNull { it.id == id } ?: WORDS
    }
}

data class DailyWordGoalPreset(
    val count: Int,
    val title: String,
    val subtitle: String
)

object DailyWordGoalCalculator {
    const val MIN_DAILY_WORD_GOAL = 1
    const val MAX_DAILY_WORD_GOAL = 100

    val presets: List<DailyWordGoalPreset> = listOf(
        DailyWordGoalPreset(5, "Casual", "~4 min/day"),
        DailyWordGoalPreset(10, "Steady", "~8 min/day"),
        DailyWordGoalPreset(20, "Focused", "~15 min/day"),
        DailyWordGoalPreset(35, "Intensive", "~25 min/day"),
        DailyWordGoalPreset(50, "Mastery", "~35 min/day")
    )

    fun countUniqueCjkCharacters(words: Collection<String>): Int {
        val unique = HashSet<Char>()
        words.forEach { word ->
            word.forEach { ch ->
                if (isCjkIdeograph(ch)) {
                    unique.add(ch)
                }
            }
        }
        return unique.size
    }

    fun isCjkIdeograph(ch: Char): Boolean {
        val code = ch.code
        return (code in 0x4E00..0x9FFF) ||
            (code in 0x3400..0x4DBF) ||
            (code in 0xF900..0xFAFF)
    }

    fun estimateCompletionDays(remainingItems: Int, dailyGoal: Int): Int {
        if (remainingItems <= 0) return 0
        val safeGoal = dailyGoal.coerceAtLeast(1)
        return ceil(remainingItems.toDouble() / safeGoal.toDouble()).toInt()
    }

    fun estimateDailyMinutes(dailyGoal: Int, unit: DailyWordGoalUnit): Int {
        val factor = if (unit == DailyWordGoalUnit.CHARACTERS) 0.55 else 0.75
        return (dailyGoal.coerceAtLeast(1) * factor).roundToInt().coerceAtLeast(3)
    }

    fun paceTierLabel(dailyGoal: Int): String = when {
        dailyGoal <= 6 -> "Casual Pace"
        dailyGoal <= 14 -> "Steady Pace"
        dailyGoal <= 25 -> "Focused Pace"
        dailyGoal <= 42 -> "Intensive Pace"
        else -> "Mastery Pace"
    }

    fun formatHskScopeLabel(levelKey: String): String = when (levelKey) {
        "ALL" -> "All HSK 1–9"
        "7+" -> "HSK 7–9"
        else -> "HSK $levelKey"
    }

    fun formatCompletionHorizon(days: Int): String = when {
        days <= 0 -> "Level completed!"
        days == 1 -> "1 day left"
        days < 30 -> "$days days"
        days < 365 -> {
            val months = (days / 30.0)
            String.format(Locale.US, "%.1f months (%d days)", months, days)
        }
        else -> {
            val years = (days / 365.0)
            String.format(Locale.US, "%.1f years (%d days)", years, days)
        }
    }
}

@Entity(
    tableName = "daily_study_logs",
    primaryKeys = ["user_id", "date_key"]
)
data class DailyStudyLogEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "date_key") val dateKey: String, // yyyy-MM-dd
    @ColumnInfo(name = "xp_earned") val xpEarned: Int = 0,
    @ColumnInfo(name = "words_learned") val wordsLearned: Int = 0,
    @ColumnInfo(name = "lessons_completed") val lessonsCompleted: Int = 0,
    @ColumnInfo(name = "srs_reviewed") val srsReviewed: Int = 0,
    @ColumnInfo(name = "stories_explored") val storiesExplored: Int = 0,
    @ColumnInfo(name = "practice_sessions") val practiceSessions: Int = 0,
    @ColumnInfo(name = "study_minutes") val studyMinutes: Int = 0,
    @ColumnInfo(name = "updated_at_ms") val updatedAtMs: Long = 0L
) {
    val totalActivityScore: Int
        get() = xpEarned + wordsLearned * 2 + lessonsCompleted * 20 + srsReviewed * 5 +
            storiesExplored * 5 + practiceSessions * 10 + studyMinutes * 3

    val hasAnyStudy: Boolean
        get() = xpEarned > 0 || wordsLearned > 0 || lessonsCompleted > 0 ||
            srsReviewed > 0 || storiesExplored > 0 || practiceSessions > 0 || studyMinutes > 0
}

data class DailyDayPoint(
    val dateKey: String,
    val dayShortLabel: String, // Mon, Tue, ...
    val dayOfMonth: Int,
    val isToday: Boolean,
    val xpEarned: Int,
    val wordsLearned: Int,
    val lessonsCompleted: Int,
    val srsReviewed: Int,
    val storiesExplored: Int,
    val practiceSessions: Int,
    val studyMinutes: Int,
    val goalReached: Boolean,
    val wordGoalReached: Boolean = false
) {
    val hasActivity: Boolean
        get() = xpEarned > 0 || wordsLearned > 0 || lessonsCompleted > 0 ||
            srsReviewed > 0 || storiesExplored > 0 || practiceSessions > 0 || studyMinutes > 0
}

data class DailyMission(
    val id: String,
    val title: String,
    val subtitle: String,
    val current: Int,
    val target: Int,
    val targetNavTab: Int
) {
    val isComplete: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class DailyTrackerSnapshot(
    val todayDateKey: String = DailyTrackerDateUtil.todayKey(),
    val todayLog: DailyStudyLogEntity = DailyStudyLogEntity(userId = "", dateKey = DailyTrackerDateUtil.todayKey()),
    val xpGoal: Int = 100,
    val wordsGoal: Int = 20,
    val wordGoalUnit: DailyWordGoalUnit = DailyWordGoalUnit.WORDS,
    val wordGoalHskLevel: String = "1",
    val todayAcquiredWords: List<String> = emptyList(),
    val todayUniqueCharacters: Int = 0,
    val last7Days: List<DailyDayPoint> = emptyList(),
    val last28Days: List<DailyDayPoint> = emptyList(),
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val wordGoalDaysHitLast7: Int = 0,
    val wordGoalStreakDays: Int = 0,
    val totalActiveDays: Int = 0,
    val weeklyXpTotal: Int = 0,
    val weeklyWordsTotal: Int = 0,
    val missions: List<DailyMission> = emptyList()
) {
    val todayWordGoalAcquired: Int
        get() = when (wordGoalUnit) {
            DailyWordGoalUnit.WORDS -> maxOf(todayLog.wordsLearned, todayAcquiredWords.size)
            DailyWordGoalUnit.CHARACTERS -> maxOf(todayUniqueCharacters, todayLog.wordsLearned)
        }

    val remainingWordsToday: Int
        get() = (wordsGoal - todayWordGoalAcquired).coerceAtLeast(0)

    val xpProgress: Float
        get() = (todayLog.xpEarned.toFloat() / xpGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val wordsProgress: Float
        get() = (todayWordGoalAcquired.toFloat() / wordsGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val overallDailyProgress: Float
        get() = ((xpProgress + wordsProgress) / 2f).coerceIn(0f, 1f)

    val isWordGoalComplete: Boolean
        get() = todayWordGoalAcquired >= wordsGoal

    val isDailyGoalComplete: Boolean
        get() = todayLog.xpEarned >= xpGoal || isWordGoalComplete
}

object DailyTrackerDateUtil {
    private const val PATTERN = "yyyy-MM-dd"

    fun formatDateKey(calendar: Calendar): String {
        val sdf = SimpleDateFormat(PATTERN, Locale.US)
        return sdf.format(calendar.time)
    }

    fun todayKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
        return formatDateKey(cal)
    }

    fun dayOfYearSeed(nowMs: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
        return cal.get(Calendar.YEAR) * 366 + cal.get(Calendar.DAY_OF_YEAR)
    }

    fun dateKeyDaysAgo(daysAgo: Int, nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        return formatDateKey(cal)
    }

    fun shortWeekdayLabel(daysAgo: Int, nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        return SimpleDateFormat("EEE", Locale.US).format(cal.time)
    }

    fun dayOfMonth(daysAgo: Int, nowMs: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun computeStreaks(activeDateKeys: Set<String>, nowMs: Long = System.currentTimeMillis()): Pair<Int, Int> {
        if (activeDateKeys.isEmpty()) return 0 to 0

        // Current streak: counts back from today (or yesterday if user hasn't studied yet today)
        val today = dateKeyDaysAgo(0, nowMs)
        val yesterday = dateKeyDaysAgo(1, nowMs)
        var currentStreak = 0
        var offset = when {
            today in activeDateKeys -> 0
            yesterday in activeDateKeys -> 1
            else -> -1
        }
        if (offset >= 0) {
            while (dateKeyDaysAgo(offset, nowMs) in activeDateKeys) {
                currentStreak++
                offset++
            }
        }

        // Best streak across sorted keys
        val sorted = activeDateKeys.sorted()
        val sdf = SimpleDateFormat(PATTERN, Locale.US)
        var best = 0
        var run = 0
        var prevDayEpochDays: Long? = null
        for (key in sorted) {
            val parsed = runCatching { sdf.parse(key) }.getOrNull() ?: continue
            val dayNum = (parsed.time + 43_200_000L) / 86_400_000L
            if (prevDayEpochDays != null && dayNum == prevDayEpochDays + 1L) {
                run++
            } else {
                run = 1
            }
            if (run > best) best = run
            prevDayEpochDays = dayNum
        }

        return currentStreak to maxOf(best, currentStreak)
    }
}
