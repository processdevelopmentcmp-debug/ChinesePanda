package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDisplayPrefs
import com.example.data.AppThemeMode
import com.example.firebase.configureAppCheck
import com.example.ui.screens.PandaAppRoot
import com.example.ui.theme.PandaChineseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModels
        configureAppCheck(this, intent)

        // STEP 2: Enable edge-to-edge display
        enableEdgeToEdge()

        val displayPrefs = AppDisplayPrefs.getInstance(applicationContext)

        setContent {
            val appFontScale by displayPrefs.fontScale.collectAsStateWithLifecycle()
            val themeMode by displayPrefs.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            PandaChineseTheme(
                darkTheme = isDark,
                appFontScale = appFontScale
            ) {
                PandaAppRoot(displayPrefs = displayPrefs)
            }
        }
    }
}
