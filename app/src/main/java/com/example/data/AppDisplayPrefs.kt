package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.DailyWordGoalCalculator
import com.example.data.local.DailyWordGoalUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

enum class AppThemeMode(val id: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromId(id: String?): AppThemeMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

class AppDisplayPrefs private constructor(
    private val prefs: SharedPreferences
) {
    private val _fontScale = MutableStateFlow(
        prefs.getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE).coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
    )
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _themeMode = MutableStateFlow(
        AppThemeMode.fromId(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.id))
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _dailyXpGoal = MutableStateFlow(
        prefs.getInt(KEY_DAILY_XP_GOAL, DEFAULT_XP_GOAL).coerceIn(20, 500)
    )
    val dailyXpGoal: StateFlow<Int> = _dailyXpGoal.asStateFlow()

    private val _dailyWordsGoal = MutableStateFlow(
        prefs.getInt(KEY_DAILY_WORDS_GOAL, DEFAULT_WORDS_GOAL)
            .coerceIn(DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL, DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL)
    )
    val dailyWordsGoal: StateFlow<Int> = _dailyWordsGoal.asStateFlow()

    private val _dailyWordGoalUnit = MutableStateFlow(
        DailyWordGoalUnit.fromId(prefs.getString(KEY_DAILY_WORD_GOAL_UNIT, DailyWordGoalUnit.WORDS.id))
    )
    val dailyWordGoalUnit: StateFlow<DailyWordGoalUnit> = _dailyWordGoalUnit.asStateFlow()

    private val _dailyWordGoalHskLevel = MutableStateFlow(
        prefs.getString(KEY_DAILY_WORD_GOAL_HSK, DEFAULT_WORD_GOAL_HSK) ?: DEFAULT_WORD_GOAL_HSK
    )
    val dailyWordGoalHskLevel: StateFlow<String> = _dailyWordGoalHskLevel.asStateFlow()

    // Bumped whenever a user's today-acquired word list updates so Flows re-read the exact set
    private val _acquiredWordsRevision = MutableStateFlow(0L)
    val acquiredWordsRevision: StateFlow<Long> = _acquiredWordsRevision.asStateFlow()

    fun setFontScale(scale: Float) {
        val snapped = (scale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE) * 20f).roundToInt() / 20f
        _fontScale.value = snapped
        prefs.edit().putFloat(KEY_FONT_SCALE, snapped).apply()
    }

    fun stepFontScale(delta: Float) {
        setFontScale(_fontScale.value + delta)
    }

    fun resetFontScale() {
        setFontScale(DEFAULT_FONT_SCALE)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.id).apply()
    }

    fun setDailyXpGoal(xpGoal: Int) {
        val clamped = xpGoal.coerceIn(20, 500)
        _dailyXpGoal.value = clamped
        prefs.edit().putInt(KEY_DAILY_XP_GOAL, clamped).apply()
    }

    fun setDailyWordsGoal(wordsGoal: Int) {
        val clamped = wordsGoal.coerceIn(
            DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
            DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
        )
        _dailyWordsGoal.value = clamped
        prefs.edit().putInt(KEY_DAILY_WORDS_GOAL, clamped).apply()
    }

    fun setDailyWordGoalUnit(unit: DailyWordGoalUnit) {
        _dailyWordGoalUnit.value = unit
        prefs.edit().putString(KEY_DAILY_WORD_GOAL_UNIT, unit.id).apply()
    }

    fun setDailyWordGoalHskLevel(levelKey: String) {
        val normalized = if (levelKey in VALID_HSK_GOAL_KEYS) levelKey else DEFAULT_WORD_GOAL_HSK
        _dailyWordGoalHskLevel.value = normalized
        prefs.edit().putString(KEY_DAILY_WORD_GOAL_HSK, normalized).apply()
    }

    fun setDailyWordGoalConfig(wordsGoal: Int, unit: DailyWordGoalUnit, levelKey: String) {
        val clamped = wordsGoal.coerceIn(
            DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
            DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
        )
        val normalized = if (levelKey in VALID_HSK_GOAL_KEYS) levelKey else DEFAULT_WORD_GOAL_HSK
        _dailyWordsGoal.value = clamped
        _dailyWordGoalUnit.value = unit
        _dailyWordGoalHskLevel.value = normalized
        prefs.edit()
            .putInt(KEY_DAILY_WORDS_GOAL, clamped)
            .putString(KEY_DAILY_WORD_GOAL_UNIT, unit.id)
            .putString(KEY_DAILY_WORD_GOAL_HSK, normalized)
            .apply()
    }

    fun getAcquiredWordsForDay(userId: String, dateKey: String): List<String> {
        if (userId.isBlank() || dateKey.isBlank()) return emptyList()
        val raw = prefs.getString(acquiredKey(userId, dateKey), "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    /**
     * Records newly acquired Hanzi words for [dateKey] preserving insertion order,
     * and returns the count of words that were newly added for this day.
     */
    fun addAcquiredWordsForDay(userId: String, dateKey: String, newHanzi: Collection<String>): Int {
        if (userId.isBlank() || dateKey.isBlank() || newHanzi.isEmpty()) return 0
        val existing = getAcquiredWordsForDay(userId, dateKey).toMutableList()
        val existingSet = existing.toHashSet()
        var addedCount = 0
        newHanzi.forEach { raw ->
            val clean = raw.trim()
            if (clean.isNotEmpty() && existingSet.add(clean)) {
                existing.add(clean)
                addedCount++
            }
        }
        if (addedCount > 0) {
            prefs.edit()
                .putString(acquiredKey(userId, dateKey), existing.joinToString("|"))
                .apply()
            _acquiredWordsRevision.value = _acquiredWordsRevision.value + 1L
        }
        return addedCount
    }

    private fun acquiredKey(userId: String, dateKey: String): String =
        "daily_acquired_words_${userId}_$dateKey"

    companion object {
        const val MIN_FONT_SCALE = 0.80f
        const val MAX_FONT_SCALE = 1.55f
        const val DEFAULT_FONT_SCALE = 1.00f
        const val DEFAULT_XP_GOAL = 100
        const val DEFAULT_WORDS_GOAL = 20
        const val DEFAULT_WORD_GOAL_HSK = "1"

        private val VALID_HSK_GOAL_KEYS = setOf("1", "2", "3", "4", "5", "6", "7+", "ALL")

        private const val PREFS_NAME = "panda_app_display_prefs"
        private const val KEY_FONT_SCALE = "app_font_scale"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_DAILY_XP_GOAL = "daily_xp_goal"
        private const val KEY_DAILY_WORDS_GOAL = "daily_words_goal"
        private const val KEY_DAILY_WORD_GOAL_UNIT = "daily_word_goal_unit"
        private const val KEY_DAILY_WORD_GOAL_HSK = "daily_word_goal_hsk"

        @Volatile
        private var INSTANCE: AppDisplayPrefs? = null

        fun getInstance(context: Context): AppDisplayPrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDisplayPrefs(
                    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { INSTANCE = it }
            }
        }
    }
}
