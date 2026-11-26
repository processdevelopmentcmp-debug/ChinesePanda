package com.example.data

import android.content.Context
import android.content.SharedPreferences
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
        prefs.getInt(KEY_DAILY_WORDS_GOAL, DEFAULT_WORDS_GOAL).coerceIn(5, 200)
    )
    val dailyWordsGoal: StateFlow<Int> = _dailyWordsGoal.asStateFlow()

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
        val clamped = wordsGoal.coerceIn(5, 200)
        _dailyWordsGoal.value = clamped
        prefs.edit().putInt(KEY_DAILY_WORDS_GOAL, clamped).apply()
    }

    companion object {
        const val MIN_FONT_SCALE = 0.80f
        const val MAX_FONT_SCALE = 1.55f
        const val DEFAULT_FONT_SCALE = 1.00f
        const val DEFAULT_XP_GOAL = 100
        const val DEFAULT_WORDS_GOAL = 20

        private const val PREFS_NAME = "panda_app_display_prefs"
        private const val KEY_FONT_SCALE = "app_font_scale"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_DAILY_XP_GOAL = "daily_xp_goal"
        private const val KEY_DAILY_WORDS_GOAL = "daily_words_goal"

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
