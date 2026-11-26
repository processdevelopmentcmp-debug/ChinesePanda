package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val goalReached: Boolean
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
    val last7Days: List<DailyDayPoint> = emptyList(),
    val last28Days: List<DailyDayPoint> = emptyList(),
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val totalActiveDays: Int = 0,
    val weeklyXpTotal: Int = 0,
    val weeklyWordsTotal: Int = 0,
    val missions: List<DailyMission> = emptyList()
) {
    val xpProgress: Float
        get() = (todayLog.xpEarned.toFloat() / xpGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val wordsProgress: Float
        get() = (todayLog.wordsLearned.toFloat() / wordsGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val overallDailyProgress: Float
        get() = ((xpProgress + wordsProgress) / 2f).coerceIn(0f, 1f)

    val isDailyGoalComplete: Boolean
        get() = todayLog.xpEarned >= xpGoal || todayLog.wordsLearned >= wordsGoal
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
