package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TextDecrease
import androidx.compose.material.icons.rounded.TextIncrease
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppDisplayPrefs
import com.example.data.AppThemeMode
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadePrimary
import kotlin.math.roundToInt

private data class FontPresetOption(
    val label: String,
    val scale: Float
)

private val FONT_PRESETS = listOf(
    FontPresetOption("Compact", 0.85f),
    FontPresetOption("Default", 1.00f),
    FontPresetOption("Cozy", 1.15f),
    FontPresetOption("Large", 1.30f),
    FontPresetOption("XL", 1.50f)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FontAndDisplaySheet(
    currentScale: Float,
    themeMode: AppThemeMode,
    audioSpeed: Float,
    onScaleChange: (Float) -> Unit,
    onStepScale: (Float) -> Unit,
    onResetScale: () -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAudioSpeedChange: (Float) -> Unit,
    onTestAudio: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val percent = (currentScale * 100f).roundToInt()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp).size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.font_size_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Resizes all Hanzi, Pinyin & UI text app-wide",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onResetScale,
                    modifier = Modifier.testTag("font_size_reset_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.font_size_reset))
                }
            }

            // Live Preview Card (inherits live LocalDensity fontScale!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = JadePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = JadePrimary
                        ) {
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "每天学习中文",
                        fontFamily = HanziSerifFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Měitiān xuéxí Zhōngwén",
                        style = MaterialTheme.typography.titleSmall,
                        color = JadePrimary
                    )
                    Text(
                        text = "Study Chinese every day — clear characters and comfortable reading on any screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fine-tune Slider + A- / A+ Stepper Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "OVERALL FONT SCALE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { onStepScale(-0.05f) },
                        enabled = currentScale > AppDisplayPrefs.MIN_FONT_SCALE + 0.005f,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("font_size_decrease_button")
                    ) {
                        Icon(Icons.Rounded.TextDecrease, contentDescription = "Decrease font size")
                    }

                    Slider(
                        value = currentScale,
                        onValueChange = onScaleChange,
                        valueRange = AppDisplayPrefs.MIN_FONT_SCALE..AppDisplayPrefs.MAX_FONT_SCALE,
                        steps = 14,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("font_size_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = JadePrimary,
                            activeTrackColor = JadePrimary
                        )
                    )

                    FilledTonalIconButton(
                        onClick = { onStepScale(0.05f) },
                        enabled = currentScale < AppDisplayPrefs.MAX_FONT_SCALE - 0.005f,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("font_size_increase_button")
                    ) {
                        Icon(Icons.Rounded.TextIncrease, contentDescription = "Increase font size")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FONT_PRESETS.forEach { preset ->
                        val selected = kotlin.math.abs(currentScale - preset.scale) < 0.025f
                        FilterChip(
                            selected = selected,
                            onClick = { onScaleChange(preset.scale) },
                            label = {
                                Text("${preset.label} (${(preset.scale * 100).roundToInt()}%)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("font_preset_${(preset.scale * 100).roundToInt()}")
                        )
                    }
                }
            }

            // Extra Reading Comfort: App Theme Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPEARANCE THEME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val selected = themeMode == mode
                        val icon = when (mode) {
                            AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                            AppThemeMode.LIGHT -> Icons.Rounded.LightMode
                            AppThemeMode.DARK -> Icons.Rounded.DarkMode
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onThemeModeChange(mode) },
                            leadingIcon = {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(mode.label) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_${mode.id}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            // Extra Reading Comfort: Default Mandarin Speech Speed
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANDARIN AUDIO SPEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { onTestAudio(audioSpeed) },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Test speech speed",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Test Voice", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0.65f to "0.65x Slow",
                        0.85f to "0.85x Clear",
                        1.0f to "1.0x Normal"
                    ).forEach { (speed, label) ->
                        val selected = kotlin.math.abs(audioSpeed - speed) < 0.06f
                        FilterChip(
                            selected = selected,
                            onClick = { onAudioSpeedChange(speed) },
                            label = { Text(label, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
