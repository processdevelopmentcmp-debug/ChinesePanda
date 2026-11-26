package com.example.model

import com.example.data.HskCurriculumData
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.HskLevelSummary
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class HskBadgeRank(
    val label: String,
    val stars: Int,
    val minPercent: Int
) {
    LOCKED("Unstarted", 0, 0),
    BRONZE("Bronze", 1, 1),
    SILVER("Silver", 2, 25),
    GOLD("Gold", 3, 60),
    JADE_MASTER("Jade Master", 4, 90);

    fun nextRank(): HskBadgeRank? = when (this) {
        LOCKED -> BRONZE
        BRONZE -> SILVER
        SILVER -> GOLD
        GOLD -> JADE_MASTER
        JADE_MASTER -> null
    }
}

data class HskLevelBadgeProgress(
    val level: Int,              // 1..6, or 7 representing HSK 7–9
    val levelKey: String,        // "1".."6", "7+"
    val label: String,           // "HSK 1" .. "HSK 7–9"
    val bandName: String,        // "Beginner", "Elementary", ...
    val sealCharacter: String,   // 芽, 竹, 松, 鹤, 梅, 凤, 龙
    val emblemTitle: String,     // "Bamboo Sprout", "Rising Bamboo", ...
    val learnedCount: Int,
    val strongCount: Int,
    val totalWords: Int,
    val progress: Float,
    val rank: HskBadgeRank,
    val nextRankTargetWords: Int
) {
    val percentInt: Int get() = (progress * 100f).roundToInt().coerceIn(0, 100)
    val isUnlocked: Boolean get() = rank != HskBadgeRank.LOCKED
    val wordsToNextRank: Int get() = (nextRankTargetWords - learnedCount).coerceAtLeast(0)
}

data class LearnerAchievement(
    val id: String,
    val title: String,
    val chineseIdiom: String,
    val description: String,
    val current: Int,
    val target: Int,
    val category: String
) {
    val isUnlocked: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class HskProfileShowcase(
    val levelBadges: List<HskLevelBadgeProgress> = emptyList(),
    val achievements: List<LearnerAchievement> = emptyList(),
    val learnerTitle: String = "Bamboo Novice",
    val learnerTitleZh: String = "竹林初学",
    val unlockedLevelBadgesCount: Int = 0,
    val unlockedAchievementsCount: Int = 0,
    val totalLearnedWords: Int = 0,
    val totalStrongWords: Int = 0,
    val totalCatalogWords: Int = 11_092
) {
    val overallCatalogProgress: Float
        get() = (totalLearnedWords.toFloat() / totalCatalogWords.coerceAtLeast(1)).coerceIn(0f, 1f)
}

object HskBadgeCalculator {

    private data class LevelThemeSpec(
        val level: Int,
        val levelKey: String,
        val sealChar: String,
        val emblemTitle: String,
        val defaultWordCount: Int
    )

    private val LEVEL_SPECS = listOf(
        LevelThemeSpec(1, "1", "芽", "Bamboo Sprout", 500),
        LevelThemeSpec(2, "2", "竹", "Rising Bamboo", 772),
        LevelThemeSpec(3, "3", "松", "Mountain Pine", 973),
        LevelThemeSpec(4, "4", "鹤", "Cloud Crane", 1000),
        LevelThemeSpec(5, "5", "梅", "Winter Plum", 1071),
        LevelThemeSpec(6, "6", "凤", "Golden Phoenix", 1140),
        LevelThemeSpec(7, "7+", "龙", "Celestial Dragon", 5636)
    )

    fun rankForProgress(learnedCount: Int, totalWords: Int): HskBadgeRank {
        if (learnedCount <= 0 || totalWords <= 0) return HskBadgeRank.LOCKED
        val scaledPct = learnedCount.toLong() * 100L
        val total = totalWords.toLong()
        return when {
            scaledPct >= total * HskBadgeRank.JADE_MASTER.minPercent -> HskBadgeRank.JADE_MASTER
            scaledPct >= total * HskBadgeRank.GOLD.minPercent -> HskBadgeRank.GOLD
            scaledPct >= total * HskBadgeRank.SILVER.minPercent -> HskBadgeRank.SILVER
            else -> HskBadgeRank.BRONZE
        }
    }

    fun targetWordsForRank(rank: HskBadgeRank?, totalWords: Int): Int {
        if (rank == null || totalWords <= 0) return totalWords
        return when (rank) {
            HskBadgeRank.LOCKED -> 0
            HskBadgeRank.BRONZE -> 1
            HskBadgeRank.SILVER -> ((totalWords * rank.minPercent + 99) / 100).coerceAtLeast(2)
            HskBadgeRank.GOLD -> ((totalWords * rank.minPercent + 99) / 100).coerceAtLeast(3)
            HskBadgeRank.JADE_MASTER -> ((totalWords * rank.minPercent + 99) / 100).coerceAtLeast(4)
        }
    }

    fun resolveLearnerTitle(
        totalLearnedWords: Int,
        unlockedLevelBadges: Int,
        xp: Long
    ): Pair<String, String> = when {
        totalLearnedWords >= 2500 || unlockedLevelBadges >= 6 -> "Celestial Dragon" to "龙腾四海"
        totalLearnedWords >= 1000 || unlockedLevelBadges >= 5 -> "Imperial Sage" to "博古通今"
        totalLearnedWords >= 350 || unlockedLevelBadges >= 4 -> "Golden Phoenix" to "丹凤朝阳"
        totalLearnedWords >= 120 || unlockedLevelBadges >= 3 || xp >= 1200L -> "Jade Scholar" to "温润如玉"
        totalLearnedWords >= 30 || unlockedLevelBadges >= 2 || xp >= 400L -> "Hanzi Explorer" to "循序渐进"
        totalLearnedWords >= 5 || xp >= 150L -> "Bamboo Apprentice" to "初露锋芒"
        else -> "Bamboo Novice" to "竹林初学"
    }

    fun buildShowcase(
        profile: UserProfile,
        learnedEntries: List<LearnedWordEntry>,
        levelSummaries: List<HskLevelSummary>,
        dailyTracker: DailyTrackerSnapshot,
        completedVocabLessonIds: Set<String>
    ): HskProfileShowcase {
        val metaByLevel = HskCurriculumData.hskLevels.associateBy { it.level }
        val summaryByLevel = levelSummaries.associateBy { it.meta.level }

        val levelBadges = LEVEL_SPECS.map { spec ->
            val matchingLearned = if (spec.level >= 7) {
                learnedEntries.filter { it.word.hskLevel >= 7 }
            } else {
                learnedEntries.filter { it.word.hskLevel == spec.level }
            }
            val learnedCount = matchingLearned.size
            val strongCount = matchingLearned.count { it.strength == WordStrength.STRONG }

            val liveTotal = if (spec.level >= 7) {
                levelSummaries.filter { it.meta.level >= 7 }.sumOf { it.activeWordCount }
            } else {
                summaryByLevel[spec.level]?.activeWordCount ?: 0
            }
            val totalWords = if (liveTotal > 0) liveTotal else spec.defaultWordCount
            val progress = (learnedCount.toFloat() / totalWords.coerceAtLeast(1)).coerceIn(0f, 1f)
            val rank = rankForProgress(learnedCount, totalWords)
            val nextTarget = targetWordsForRank(rank.nextRank(), totalWords)
            val meta = metaByLevel[spec.level]

            HskLevelBadgeProgress(
                level = spec.level,
                levelKey = spec.levelKey,
                label = meta?.label ?: if (spec.level >= 7) "HSK 7–9" else "HSK ${spec.level}",
                bandName = meta?.band ?: "HSK 3.0",
                sealCharacter = spec.sealChar,
                emblemTitle = spec.emblemTitle,
                learnedCount = learnedCount,
                strongCount = strongCount,
                totalWords = totalWords,
                progress = progress,
                rank = rank,
                nextRankTargetWords = nextTarget
            )
        }

        val totalLearned = learnedEntries.size
        val totalStrong = learnedEntries.count { it.strength == WordStrength.STRONG }
        val totalLessons = completedVocabLessonIds.size + profile.completedUnitIds.size
        val bestStreak = maxOf(profile.streak.toInt(), dailyTracker.currentStreakDays, dailyTracker.bestStreakDays)
        val wordGoalHits = maxOf(dailyTracker.wordGoalDaysHitLast7, if (dailyTracker.isWordGoalComplete) 1 else 0)
        val unlockedLevelCount = levelBadges.count { it.isUnlocked }
        val totalCatalog = levelBadges.sumOf { it.totalWords }.coerceAtLeast(11_092)

        val achievements = listOf(
            LearnerAchievement(
                id = "ach_first_10",
                title = "First Sprouts",
                chineseIdiom = "初学乍练",
                description = "Acquire your first 10 HSK vocabulary words",
                current = totalLearned,
                target = 10,
                category = "Vocabulary"
            ),
            LearnerAchievement(
                id = "ach_vocab_100",
                title = "Century Lexicon",
                chineseIdiom = "积少成多",
                description = "Build a learned vault of 100 HSK words",
                current = totalLearned,
                target = 100,
                category = "Vocabulary"
            ),
            LearnerAchievement(
                id = "ach_vocab_500",
                title = "HSK Scholar",
                chineseIdiom = "博学多才",
                description = "Acquire 500 words across HSK 3.0 tiers",
                current = totalLearned,
                target = 500,
                category = "Vocabulary"
            ),
            LearnerAchievement(
                id = "ach_daily_goal",
                title = "Daily Goal Finisher",
                chineseIdiom = "持之以恒",
                description = "Hit your daily word acquisition goal 3 days in a week",
                current = wordGoalHits,
                target = 3,
                category = "Habit"
            ),
            LearnerAchievement(
                id = "ach_streak_7",
                title = "7-Day Flame Keeper",
                chineseIdiom = "百折不挠",
                description = "Maintain a 7-day Mandarin study streak",
                current = bestStreak,
                target = 7,
                category = "Habit"
            ),
            LearnerAchievement(
                id = "ach_anki_25",
                title = "Memory Architect",
                chineseIdiom = "温故知新",
                description = "Reach Strong mastery on 25 Anki / SM-2 flashcards",
                current = totalStrong,
                target = 25,
                category = "Mastery"
            ),
            LearnerAchievement(
                id = "ach_lessons_10",
                title = "Path Conqueror",
                chineseIdiom = "步步高升",
                description = "Complete 10 interactive HSK lessons or Core Path units",
                current = totalLessons,
                target = 10,
                category = "Lessons"
            ),
            LearnerAchievement(
                id = "ach_xp_1000",
                title = "Imperial XP Legend",
                chineseIdiom = "出类拔萃",
                description = "Earn 1,000 total XP across lessons, reviews & AI practice",
                current = profile.xp.toInt(),
                target = 1000,
                category = "Gamification"
            )
        )

        val (titleEn, titleZh) = resolveLearnerTitle(
            totalLearnedWords = totalLearned,
            unlockedLevelBadges = unlockedLevelCount,
            xp = profile.xp
        )

        return HskProfileShowcase(
            levelBadges = levelBadges,
            achievements = achievements,
            learnerTitle = titleEn,
            learnerTitleZh = titleZh,
            unlockedLevelBadgesCount = unlockedLevelCount,
            unlockedAchievementsCount = achievements.count { it.isUnlocked },
            totalLearnedWords = totalLearned,
            totalStrongWords = totalStrong,
            totalCatalogWords = totalCatalog
        )
    }
}
