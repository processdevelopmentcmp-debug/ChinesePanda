package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val PandaLightColorScheme = lightColorScheme(
    primary = JadePrimary,
    onPrimary = Color.White,
    primaryContainer = JadeLightContainer,
    onPrimaryContainer = JadeDeep,
    secondary = GoldAccent,
    onSecondary = Color(0xFF2B1C00),
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldOnContainer,
    tertiary = IndigoAccent,
    onTertiary = Color.White,
    tertiaryContainer = IndigoContainer,
    onTertiaryContainer = Color(0xFF1E1B4B),
    error = CoralVermilion,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = CoralOnContainer,
    background = JadeSoftBg,
    onBackground = InkPrimaryLight,
    surface = SurfaceLight,
    onSurface = InkPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = InkSecondaryLight,
    outline = OutlineLight
)

private val PandaDarkColorScheme = darkColorScheme(
    primary = JadeVibrant,
    onPrimary = Color(0xFF00382C),
    primaryContainer = JadeDeep,
    onPrimaryContainer = JadeLightContainer,
    secondary = GoldBright,
    onSecondary = Color(0xFF332100),
    secondaryContainer = Color(0xFF4D3504),
    onSecondaryContainer = GoldContainer,
    tertiary = Color(0xFFA5B4FC),
    onTertiary = Color(0xFF1E1B4B),
    tertiaryContainer = Color(0xFF312E81),
    onTertiaryContainer = IndigoContainer,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF5C1410),
    errorContainer = Color(0xFF7A1E19),
    onErrorContainer = CoralContainer,
    background = BackgroundDark,
    onBackground = InkPrimaryDark,
    surface = SurfaceDark,
    onSurface = InkPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = InkSecondaryDark,
    outline = OutlineDark
)

val PandaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PandaChineseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appFontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PandaDarkColorScheme else PandaLightColorScheme
    val baseDensity = LocalDensity.current
    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = (baseDensity.fontScale * appFontScale).coerceIn(0.75f, 2.2f)
    )
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = PandaShapes,
            content = content
        )
    }
}
