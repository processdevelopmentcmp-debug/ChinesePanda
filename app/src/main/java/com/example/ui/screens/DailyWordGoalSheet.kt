package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.DailyWordGoalCalculator
import com.example.data.local.DailyWordGoalUnit
import com.example.data.local.HskLevelSummary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val HSK_GOAL_LEVEL_OPTIONS = listOf(
    "1" to "HSK 1",
    "2" to "HSK 2",
    "3" to "HSK 3",
    "4" to "HSK 4",
    "5" to "HSK 5",
    "6" to "HSK 6",
    "7+" to "HSK 7–9",
    "ALL" to "All HSK"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyWordAcquisitionGoalSheet(
    snapshot: DailyTrackerSnapshot,
    levelSummaries: List<HskLevelSummary>,
    totalCatalogWords: Int,
    totalLearnedWords: Int,
    onSaveGoal: (targetCount: Int, unit: DailyWordGoalUnit, hskLevelKey: String) -> Unit,
    onSaveAndStartPack: (targetCount: Int, unit: DailyWordGoalUnit, hskLevelKey: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTarget by remember(snapshot.wordsGoal) { mutableIntStateOf(snapshot.wordsGoal) }
    var selectedUnit by remember(snapshot.wordGoalUnit) { mutableStateOf(snapshot.wordGoalUnit) }
    var selectedHskKey by remember(snapshot.wordGoalHskLevel) { mutableStateOf(snapshot.wordGoalHskLevel) }

    val numberFmt = remember { NumberFormat.getIntegerInstance(Locale.US) }

    val scopeTotalWords = remember(selectedHskKey, levelSummaries, totalCatalogWords) {
        when (selectedHskKey) {
            "ALL" -> maxOf(totalCatalogWords, 11_092)
            "7+" -> {
                val live = levelSummaries.filter { it.meta.level >= 7 }.sumOf { it.activeWordCount }
                if (live > 0) live else 5_636
            }
            else -> {
                val lvl = selectedHskKey.toIntOrNull() ?: 1
                val summary = levelSummaries.firstOrNull { it.meta.level == lvl }
                val live = summary?.activeWordCount ?: 0
                val fallback = summary?.meta?.wordTarget?.filter { it.isDigit() }?.toIntOrNull() ?: 500
                if (live > 0) live else fallback
            }
        }
    }

    val estimatedRemaining = remember(scopeTotalWords, totalLearnedWords, selectedHskKey) {
        if (selectedHskKey == "ALL") {
            (scopeTotalWords - totalLearnedWords).coerceAtLeast(0)
        } else {
            // Conservative remaining estimate for the selected level
            (scopeTotalWords - (totalLearnedWords / 3).coerceAtMost(scopeTotalWords - 1).coerceAtLeast(0))
                .coerceAtLeast(0)
        }
    }

    val estDays = remember(estimatedRemaining, selectedTarget) {
        DailyWordGoalCalculator.estimateCompletionDays(estimatedRemaining, selectedTarget)
    }
    val estMinutes = remember(selectedTarget, selectedUnit) {
        DailyWordGoalCalculator.estimateDailyMinutes(selectedTarget, selectedUnit)
    }
    val paceLabel = remember(selectedTarget) {
        DailyWordGoalCalculator.paceTierLabel(selectedTarget)
    }

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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.daily_word_goal_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.daily_word_goal_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = JadeLightContainer
                ) {
                    Text(
                        text = paceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = JadeDeep,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Unit Mode Switcher: New HSK Words vs New Hanzi Characters
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "COUNT DAILY PROGRESS BY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailyWordGoalUnit.entries.forEach { unit ->
                        val isSelected = selectedUnit == unit
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedUnit = unit }
                                .testTag("word_goal_unit_${unit.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) JadeLightContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (unit == DailyWordGoalUnit.WORDS) "词 Words" else "字 Hanzi",
                                        fontFamily = HanziSerifFontFamily,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) JadeDeep else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = JadePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = unit.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Big Precision Target Stepper + Slider
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoalStepPill(
                                label = "-5",
                                testTag = "word_goal_minus_5",
                                onClick = {
                                    selectedTarget = (selectedTarget - 5).coerceIn(
                                        DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
                                        DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
                                    )
                                }
                            )
                            GoalIconStepButton(
                                icon = Icons.Rounded.Remove,
                                contentDescription = "Decrease daily goal by 1",
                                testTag = "word_goal_minus_1",
                                onClick = {
                                    selectedTarget = (selectedTarget - 1).coerceIn(
                                        DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
                                        DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
                                    )
                                }
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$selectedTarget",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = JadePrimary,
                                modifier = Modifier.testTag("word_goal_selected_value")
                            )
                            Text(
                                text = "new ${selectedUnit.shortUnit} / day",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoalIconStepButton(
                                icon = Icons.Rounded.Add,
                                contentDescription = "Increase daily goal by 1",
                                testTag = "word_goal_plus_1",
                                onClick = {
                                    selectedTarget = (selectedTarget + 1).coerceIn(
                                        DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
                                        DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
                                    )
                                }
                            )
                            GoalStepPill(
                                label = "+5",
                                testTag = "word_goal_plus_5",
                                onClick = {
                                    selectedTarget = (selectedTarget + 5).coerceIn(
                                        DailyWordGoalCalculator.MIN_DAILY_WORD_GOAL,
                                        DailyWordGoalCalculator.MAX_DAILY_WORD_GOAL
                                    )
                                }
                            )
                        }
                    }

                    Slider(
                        value = selectedTarget.toFloat(),
                        onValueChange = { selectedTarget = it.roundToInt().coerceIn(1, 100) },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JadePrimary,
                            activeTrackColor = JadePrimary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("word_goal_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 / day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("25", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("50", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100 / day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Quick Pace Presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK PACE PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DailyWordGoalCalculator.presets.forEach { preset ->
                        val active = selectedTarget == preset.count
                        Surface(
                            onClick = { selectedTarget = preset.count },
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) GoldAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (active) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("word_goal_preset_${preset.count}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${preset.count} · ${preset.title}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Target HSK 3.0 Focus Level
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TARGET HSK 3.0 FOCUS FOR DAILY PACKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HSK_GOAL_LEVEL_OPTIONS.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedHskKey == key,
                            onClick = { selectedHskKey = key },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("word_goal_hsk_$key")
                        )
                    }
                }
            }

            // Live HSK Mastery Forecast Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, JadePrimary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    JadeLightContainer.copy(alpha = 0.55f),
                                    GoldContainer.copy(alpha = 0.50f)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = JadeDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "HSK MASTERY FORECAST · ${DailyWordGoalCalculator.formatHskScopeLabel(selectedHskKey)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JadeDeep,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ForecastPill(
                            icon = Icons.Rounded.CalendarMonth,
                            value = DailyWordGoalCalculator.formatCompletionHorizon(estDays),
                            label = "${numberFmt.format(scopeTotalWords)} total words",
                            modifier = Modifier.weight(1f)
                        )
                        ForecastPill(
                            icon = Icons.Rounded.Schedule,
                            value = "~$estMinutes min/day",
                            label = "${selectedTarget * 7} ${selectedUnit.shortUnit}/week",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Save Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSaveGoal(selectedTarget, selectedUnit, selectedHskKey)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_daily_word_goal_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JadePrimary)
                ) {
                    Text(
                        text = "Save Daily Goal ($selectedTarget ${selectedUnit.shortUnit}/day)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                FilledTonalButton(
                    onClick = {
                        onSaveAndStartPack(selectedTarget, selectedUnit, selectedHskKey)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_and_start_daily_goal_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Save & Start Today's ${DailyWordGoalCalculator.formatHskScopeLabel(selectedHskKey)} Pack",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun GoalIconStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GoalStepPill(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ForecastPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = JadePrimary, modifier = Modifier.size(14.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }
    }
}
