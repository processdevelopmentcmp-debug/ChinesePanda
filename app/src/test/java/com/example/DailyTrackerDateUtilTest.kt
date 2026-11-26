package com.example

import com.example.data.local.DailyTrackerDateUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DailyTrackerDateUtilTest {

    @Test
    fun computeStreaks_countsConsecutiveDaysIncludingToday() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMs = cal.timeInMillis
        val active = setOf(
            "2026-11-26",
            "2026-11-25",
            "2026-11-24",
            "2026-11-20",
            "2026-11-19",
            "2026-11-18",
            "2026-11-17"
        )
        val (current, best) = DailyTrackerDateUtil.computeStreaks(active, nowMs)
        assertEquals(3, current)
        assertEquals(4, best)
    }

    @Test
    fun computeStreaks_preservesCurrentStreakWhenStudiedYesterday() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMs = cal.timeInMillis
        val active = setOf("2026-11-25", "2026-11-24")
        val (current, best) = DailyTrackerDateUtil.computeStreaks(active, nowMs)
        assertEquals(2, current)
        assertEquals(2, best)
    }

    @Test
    fun computeStreaks_returnsZeroCurrentWhenBroken() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMs = cal.timeInMillis
        val active = setOf("2026-11-22", "2026-11-21")
        val (current, best) = DailyTrackerDateUtil.computeStreaks(active, nowMs)
        assertEquals(0, current)
        assertEquals(2, best)
    }

    @Test
    fun wordGoalCalculator_countsUniqueCjkCharactersAndCompletionHorizon() {
        val words = listOf("学习", "学生", "汉语", "你好!", "123")
        // Unique CJK chars: 学, 习, 生, 汉, 语, 你, 好 = 7
        assertEquals(7, com.example.data.local.DailyWordGoalCalculator.countUniqueCjkCharacters(words))
        assertEquals(25, com.example.data.local.DailyWordGoalCalculator.estimateCompletionDays(500, 20))
        assertEquals(17, com.example.data.local.DailyWordGoalCalculator.estimateCompletionDays(500, 30))
        assertEquals(0, com.example.data.local.DailyWordGoalCalculator.estimateCompletionDays(0, 20))
        assertEquals("Focused Pace", com.example.data.local.DailyWordGoalCalculator.paceTierLabel(20))
    }

    @Test
    fun dailyTrackerSnapshot_tracksWordAndCharacterGoalProgress() {
        val wordsSnapshot = com.example.data.local.DailyTrackerSnapshot(
            wordsGoal = 10,
            wordGoalUnit = com.example.data.local.DailyWordGoalUnit.WORDS,
            todayAcquiredWords = listOf("你好", "谢谢", "再见"),
            todayUniqueCharacters = 5
        )
        assertEquals(3, wordsSnapshot.todayWordGoalAcquired)
        assertEquals(7, wordsSnapshot.remainingWordsToday)
        assertEquals(false, wordsSnapshot.isWordGoalComplete)

        val charSnapshot = wordsSnapshot.copy(
            wordsGoal = 5,
            wordGoalUnit = com.example.data.local.DailyWordGoalUnit.CHARACTERS
        )
        assertEquals(5, charSnapshot.todayWordGoalAcquired)
        assertEquals(0, charSnapshot.remainingWordsToday)
        assertEquals(true, charSnapshot.isWordGoalComplete)
    }
}
