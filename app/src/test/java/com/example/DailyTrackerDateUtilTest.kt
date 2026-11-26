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
}
