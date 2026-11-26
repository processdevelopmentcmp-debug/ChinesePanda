package com.example

import com.example.data.HskCurriculumData
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.HskBand
import com.example.data.local.HskLevelSummary
import com.example.model.HskBadgeCalculator
import com.example.model.HskBadgeRank
import com.example.model.LearnedWordEntry
import com.example.model.UserProfile
import com.example.model.VocabWord
import com.example.model.WordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HskBadgeCalculatorTest {

    @Test
    fun rankForProgress_mapsThresholdsAccurately() {
        assertEquals(HskBadgeRank.LOCKED, HskBadgeCalculator.rankForProgress(0, 100))
        assertEquals(HskBadgeRank.BRONZE, HskBadgeCalculator.rankForProgress(1, 100))
        assertEquals(HskBadgeRank.BRONZE, HskBadgeCalculator.rankForProgress(24, 100))
        assertEquals(HskBadgeRank.SILVER, HskBadgeCalculator.rankForProgress(25, 100))
        assertEquals(HskBadgeRank.GOLD, HskBadgeCalculator.rankForProgress(60, 100))
        assertEquals(HskBadgeRank.JADE_MASTER, HskBadgeCalculator.rankForProgress(90, 100))
    }

    @Test
    fun buildShowcase_computesPerLevelBadgesAndAchievements() {
        val summaries = HskCurriculumData.hskLevels.map { meta ->
            HskLevelSummary(
                meta = meta,
                band = HskBand.fromLevel(meta.level),
                activeWordCount = 100,
                writableCount = 50
            )
        }
        val hsk1Words = (1..30).map { i ->
            LearnedWordEntry(
                word = VocabWord("w1_$i", "字$i", "zi", "char $i", hskLevel = 1, topicId = 1),
                srsCard = null,
                strength = if (i <= 25) WordStrength.STRONG else WordStrength.MEDIUM,
                isDue = false
            )
        }
        val hsk7Words = (1..5).map { i ->
            LearnedWordEntry(
                word = VocabWord("w8_$i", "龙$i", "long", "dragon $i", hskLevel = 8, topicId = 1),
                srsCard = null,
                strength = WordStrength.MEDIUM,
                isDue = false
            )
        }
        val profile = UserProfile(
            id = "u1",
            userId = "u1",
            displayName = "Test Panda",
            xp = 1200L,
            streak = 7L,
            completedUnitIds = listOf("u1", "u2")
        )
        val tracker = DailyTrackerSnapshot(
            wordGoalDaysHitLast7 = 4,
            currentStreakDays = 7,
            bestStreakDays = 7
        )

        val showcase = HskBadgeCalculator.buildShowcase(
            profile = profile,
            learnedEntries = hsk1Words + hsk7Words,
            levelSummaries = summaries,
            dailyTracker = tracker,
            completedVocabLessonIds = (1..8).map { "l_$it" }.toSet()
        )

        assertEquals(7, showcase.levelBadges.size)
        val hsk1Badge = showcase.levelBadges.first { it.level == 1 }
        assertEquals(HskBadgeRank.SILVER, hsk1Badge.rank)
        assertEquals(30, hsk1Badge.learnedCount)
        assertEquals(25, hsk1Badge.strongCount)
        assertEquals(30, hsk1Badge.wordsToNextRank) // 60 - 30 = 30 to reach Gold

        val hsk2Badge = showcase.levelBadges.first { it.level == 2 }
        assertEquals(HskBadgeRank.LOCKED, hsk2Badge.rank)
        assertFalse(hsk2Badge.isUnlocked)

        val hsk79Badge = showcase.levelBadges.first { it.level == 7 }
        assertEquals(HskBadgeRank.BRONZE, hsk79Badge.rank)
        assertEquals(5, hsk79Badge.learnedCount)

        assertEquals(2, showcase.unlockedLevelBadgesCount)
        assertTrue(showcase.achievements.first { it.id == "ach_first_10" }.isUnlocked)
        assertTrue(showcase.achievements.first { it.id == "ach_streak_7" }.isUnlocked)
        assertTrue(showcase.achievements.first { it.id == "ach_anki_25" }.isUnlocked)
        assertTrue(showcase.achievements.first { it.id == "ach_lessons_10" }.isUnlocked)
        assertTrue(showcase.achievements.first { it.id == "ach_xp_1000" }.isUnlocked)
        assertTrue(showcase.achievements.first { it.id == "ach_daily_goal" }.isUnlocked)
        assertFalse(showcase.achievements.first { it.id == "ach_vocab_100" }.isUnlocked)
    }
}
