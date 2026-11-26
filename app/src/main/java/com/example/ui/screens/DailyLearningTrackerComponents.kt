package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.DailyMission
import com.example.data.local.DailyTrackerSnapshot
import com.example.model.VocabWord
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant
import kotlin.math.roundToInt

@Composable
fun DailyTrackerCompactCard(
    snapshot: DailyTrackerSnapshot,
    focusTimerRunning: Boolean,
    focusTimerRemainingSec: Int,
    onOpenFullTracker: () -> Unit
) {
    val today = snapshot.todayLog
    val completedMissions = snapshot.missions.count { it.isComplete }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenFullTracker() }
            .testTag("daily_tracker_compact_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = if (snapshot.isDailyGoalComplete) 1.8.dp else 1.dp,
            color = if (snapshot.isDailyGoalComplete) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DailyGoalDualRing(
                    xpProgress = snapshot.xpProgress,
                    wordsProgress = snapshot.wordsProgress,
                    size = 70.dp,
                    centerText = "${(snapshot.overallDailyProgress * 100f).roundToInt()}%"
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = JadePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.daily_tracker_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (snapshot.isDailyGoalComplete) {
                            "Daily goal hit! ${today.xpEarned}/${snapshot.xpGoal} XP · ${today.wordsLearned}/${snapshot.wordsGoal} words"
                        } else {
                            "${today.xpEarned}/${snapshot.xpGoal} XP · ${today.wordsLearned}/${snapshot.wordsGoal} words · $completedMissions/${snapshot.missions.size} missions"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (focusTimerRunning) {
                        val mins = focusTimerRemainingSec / 60
                        val secs = focusTimerRemainingSec % 60
                        Text(
                            text = "Focus timer running · %02d:%02d left".format(mins, secs),
                            style = MaterialTheme.typography.labelSmall,
                            color = IndigoAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Open daily tracker",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Compact 7-day strip
            if (snapshot.last7Days.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    snapshot.last7Days.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            day.goalReached -> JadePrimary
                                            day.hasActivity -> JadePrimary.copy(alpha = 0.28f)
                                            day.isToday -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.goalReached) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Goal reached",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${day.dayOfMonth}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (day.isToday) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                text = day.dayShortLabel.take(2),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (day.isToday) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordOfTheDaySpotlightCard(
    word: VocabWord,
    onSpeak: (String, Float) -> Unit,
    onOpenBreakdown: (VocabWord) -> Unit,
    onAddSrs: (VocabWord) -> Unit,
    onWriteWord: (VocabWord) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("word_of_the_day_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.word_of_the_day_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "HSK ${word.hskLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = word.hanzi,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.pinyin,
                        style = MaterialTheme.typography.titleSmall,
                        color = JadePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (word.exampleHanzi.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = word.exampleHanzi,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (word.exampleEnglish.isNotBlank()) {
                            Text(
                                text = word.exampleEnglish,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = { onSpeak(word.hanzi, 0.85f) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Listen")
                }
                FilledTonalButton(
                    onClick = { onOpenBreakdown(word) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("wotd_story_button")
                ) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Story & Radicals")
                }
                OutlinedButton(
                    onClick = { onAddSrs(word) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { onWriteWord(word) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Write")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyLearningTrackerSheet(
    snapshot: DailyTrackerSnapshot,
    focusDurationMin: Int,
    focusRemainingSec: Int,
    focusRunning: Boolean,
    onSetFocusMinutes: (Int) -> Unit,
    onToggleFocusTimer: () -> Unit,
    onResetFocusTimer: () -> Unit,
    onSetXpGoal: (Int) -> Unit,
    onSetWordsGoal: (Int) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var chartShowWords by remember { mutableStateOf(false) }
    var selectedDayIndex by remember(snapshot.last7Days.size) {
        mutableIntStateOf((snapshot.last7Days.size - 1).coerceAtLeast(0))
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_tracker_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Today (${snapshot.todayDateKey}) · ${snapshot.totalActiveDays} active day(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${snapshot.currentStreakDays}d · Best ${snapshot.bestStreakDays}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4A3100),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Today's Progress Hero
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(
                    1.2.dp,
                    if (snapshot.isDailyGoalComplete) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DailyGoalDualRing(
                            xpProgress = snapshot.xpProgress,
                            wordsProgress = snapshot.wordsProgress,
                            size = 92.dp,
                            centerText = "${(snapshot.overallDailyProgress * 100f).roundToInt()}%"
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (snapshot.isDailyGoalComplete) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Daily Goal Achieved!",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = JadePrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Today's Learning Rings",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Outer ring (Jade): ${snapshot.todayLog.xpEarned} / ${snapshot.xpGoal} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = JadePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Inner ring (Gold): ${snapshot.todayLog.wordsLearned} / ${snapshot.wordsGoal} words",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 6-Metric Today Grid (2 rows x 3 cols)
                    val t = snapshot.todayLog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrackerMetricTile("${t.xpEarned}", "XP Today", Modifier.weight(1f))
                        TrackerMetricTile("${t.wordsLearned}", "Words", Modifier.weight(1f))
                        TrackerMetricTile("${t.lessonsCompleted}", "Lessons", Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrackerMetricTile("${t.srsReviewed}", "SRS Cards", Modifier.weight(1f))
                        TrackerMetricTile("${t.storiesExplored}", "Stories", Modifier.weight(1f))
                        TrackerMetricTile("${t.studyMinutes}m", "Study Time", Modifier.weight(1f))
                    }
                }
            }

            // Daily Mission Checklist
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DAILY MISSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                snapshot.missions.forEach { mission ->
                    DailyMissionRow(
                        mission = mission,
                        onGo = {
                            onDismiss()
                            onNavigateToTab(mission.targetNavTab)
                        }
                    )
                }
            }

            // Extra Feature: Built-in Focus Study Timer (Pomodoro)
            FocusStudyTimerCard(
                durationMin = focusDurationMin,
                remainingSec = focusRemainingSec,
                running = focusRunning,
                onSelectMinutes = onSetFocusMinutes,
                onToggle = onToggleFocusTimer,
                onReset = onResetFocusTimer
            )

            // Interactive 7-Day Activity Bar Chart
            if (snapshot.last7Days.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LAST 7 DAYS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${snapshot.weeklyXpTotal} XP · ${snapshot.weeklyWordsTotal} words",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = !chartShowWords,
                                    onClick = { chartShowWords = false },
                                    label = { Text("XP") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JadePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = chartShowWords,
                                    onClick = { chartShowWords = true },
                                    label = { Text("Words") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        val maxVal = snapshot.last7Days.maxOfOrNull {
                            if (chartShowWords) it.wordsLearned else it.xpEarned
                        }?.coerceAtLeast(if (chartShowWords) snapshot.wordsGoal else snapshot.xpGoal) ?: 100

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            snapshot.last7Days.forEachIndexed { index, day ->
                                val value = if (chartShowWords) day.wordsLearned else day.xpEarned
                                val fraction = (value.toFloat() / maxVal.coerceAtLeast(1)).coerceIn(0.06f, 1f)
                                val isSelected = index == selectedDayIndex
                                val barColor = when {
                                    chartShowWords && isSelected -> GoldAccent
                                    chartShowWords -> GoldAccent.copy(alpha = 0.55f)
                                    isSelected -> JadePrimary
                                    else -> JadePrimary.copy(alpha = 0.55f)
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedDayIndex = index }
                                        .padding(vertical = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = if (value > 0) "$value" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height((62f * fraction).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                            .background(if (value > 0) barColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = day.dayShortLabel.take(2),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected || day.isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isSelected || day.isToday) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        val selectedDay = snapshot.last7Days.getOrNull(selectedDayIndex)
                        if (selectedDay != null) {
                            Text(
                                text = "${selectedDay.dateKey}: ${selectedDay.xpEarned} XP · ${selectedDay.wordsLearned} words · ${selectedDay.lessonsCompleted} lessons · ${selectedDay.srsReviewed} SRS · ${selectedDay.studyMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 28-Day Study Consistency Heatmap
            if (snapshot.last28Days.size == 28) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "28-DAY CONSISTENCY HEATMAP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "4 weeks",
                                style = MaterialTheme.typography.labelSmall,
                                color = JadePrimary
                            )
                        }
                        snapshot.last28Days.chunked(7).forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                week.forEach { day ->
                                    val cellColor = when {
                                        day.goalReached -> JadePrimary
                                        day.xpEarned >= snapshot.xpGoal / 2 -> JadeVibrant.copy(alpha = 0.7f)
                                        day.hasActivity -> JadePrimary.copy(alpha = 0.30f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.35f)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(cellColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dayOfMonth}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (day.goalReached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Customize Daily Targets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CUSTOMIZE DAILY TARGETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daily XP Target",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 100, 150, 250).forEach { target ->
                        FilterChip(
                            selected = snapshot.xpGoal == target,
                            onClick = { onSetXpGoal(target) },
                            label = { Text("$target XP") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("xp_goal_chip_$target")
                        )
                    }
                }
                Text(
                    text = "Daily Words Target",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 30, 50).forEach { target ->
                        FilterChip(
                            selected = snapshot.wordsGoal == target,
                            onClick = { onSetWordsGoal(target) },
                            label = { Text("$target words") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("words_goal_chip_$target")
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DailyGoalDualRing(
    xpProgress: Float,
    wordsProgress: Float,
    size: Dp,
    centerText: String
) {
    val animXp by animateFloatAsState(
        targetValue = xpProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 550),
        label = "xp_ring"
    )
    val animWords by animateFloatAsState(
        targetValue = wordsProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 550),
        label = "words_ring"
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val outerStroke = size.toPx() * 0.105f
            val innerStroke = size.toPx() * 0.085f
            val outerInset = outerStroke / 2f
            val outerSize = Size(this.size.width - outerStroke, this.size.height - outerStroke)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = JadePrimary,
                startAngle = -90f,
                sweepAngle = 360f * animXp.coerceAtLeast(if (animXp > 0f) 0.04f else 0f),
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            val innerInset = outerStroke + innerStroke * 0.85f
            val innerSize = Size(
                (this.size.width - innerInset * 2f).coerceAtLeast(4f),
                (this.size.height - innerInset * 2f).coerceAtLeast(4f)
            )
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = innerSize,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = GoldAccent,
                startAngle = -90f,
                sweepAngle = 360f * animWords.coerceAtLeast(if (animWords > 0f) 0.04f else 0f),
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = innerSize,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
        }

        Text(
            text = centerText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TrackerMetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyMissionRow(
    mission: DailyMission,
    onGo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (mission.isComplete) JadeLightContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            1.dp,
            if (mission.isComplete) JadePrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (mission.isComplete) JadePrimary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${mission.current.coerceAtMost(mission.target)}/${mission.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mission.isComplete) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = mission.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { mission.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (mission.isComplete) JadePrimary else GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
            if (!mission.isComplete) {
                TextButton(onClick = onGo) {
                    Text("Go", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FocusStudyTimerCard(
    durationMin: Int,
    remainingSec: Int,
    running: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val totalSec = (durationMin * 60).coerceAtLeast(1)
    val progress = 1f - (remainingSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f)
    val mins = remainingSec / 60
    val secs = remainingSec % 60

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            text = "Focus Study Timer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Logs study minutes & awards bonus XP when done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (running) IndigoAccent else MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = IndigoAccent,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5, 10, 15, 25).forEach { m ->
                    FilterChip(
                        selected = durationMin == m,
                        onClick = { onSelectMinutes(m) },
                        label = { Text("${m}m") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("focus_timer_toggle_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (running) "Pause Focus" else "Start Focus")
                }
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("focus_timer_reset_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Reset focus timer", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
