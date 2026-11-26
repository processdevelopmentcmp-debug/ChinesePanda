package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HskCatalogSyncState
import com.example.data.local.HskLevelSummary
import com.example.model.TopicCategory
import com.example.model.UserProfile
import com.example.model.VocabWord
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant
import com.example.viewmodel.LearnTabMode
import java.text.NumberFormat
import java.util.Locale

private fun formatInt(n: Int): String = NumberFormat.getIntegerInstance(Locale.US).format(n)

@Composable
fun LearnMasteryHeroCard(
    profile: UserProfile,
    totalCatalogWords: Int,
    syncState: HskCatalogSyncState,
    completedVocabLessonsCount: Int,
    completedCoreUnitsCount: Int,
    totalCoreUnitsCount: Int,
    hasQuickStartLesson: Boolean,
    onContinueNextLesson: () -> Unit,
    onAiBonusDrill: () -> Unit,
    onSyncCatalog: () -> Unit,
    onRefillHearts: () -> Unit
) {
    val totalLessonsCompleted = completedVocabLessonsCount + completedCoreUnitsCount
    val estimatedWordsLearned = completedVocabLessonsCount * 10 + completedCoreUnitsCount * 6

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(JadeDeep, JadePrimary, Color(0xFF007F6B))
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "HSK 3.0 MASTER COURSE · ${formatInt(totalCatalogWords)} WORDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldBright,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (profile.hearts < 5L) {
                    TextButton(onClick = onRefillHearts) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Refill hearts",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Refill", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text(
                text = "Learn Every HSK 3.0 Word",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Master all Levels 1–9 step by step: 10-word lessons with native audio, listening drills, Hanzi recall & match blitz — organized HSK by HSK and Topic by Topic.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )

            // Mini stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroStatChip(
                    value = "$totalLessonsCompleted",
                    label = "Lessons Done",
                    modifier = Modifier.weight(1f)
                )
                HeroStatChip(
                    value = "~${formatInt(estimatedWordsLearned)}",
                    label = "Words Trained",
                    modifier = Modifier.weight(1f)
                )
                HeroStatChip(
                    value = "$completedCoreUnitsCount/$totalCoreUnitsCount",
                    label = "Core Units",
                    modifier = Modifier.weight(1f)
                )
            }

            // Live 11k sync status chip when syncing or not yet fully cached
            when (syncState) {
                is HskCatalogSyncState.Syncing -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GoldBright,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "${syncState.status} (${syncState.progressPercent}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                is HskCatalogSyncState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSyncCatalog() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Sync, contentDescription = "Retry sync", tint = GoldBright, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Tap to sync all 11,000+ HSK 3.0 words offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                is HskCatalogSyncState.Ready -> {
                    if (!syncState.fullCatalogLoaded) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSyncCatalog() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Sync, contentDescription = "Sync catalog", tint = GoldBright, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Starter deck (${syncState.totalWords} words) · Tap to download all 11,000+",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = GoldBright, modifier = Modifier.size(15.dp))
                            Text(
                                text = "All ${formatInt(syncState.totalWords)} HSK 3.0 words ready offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                HskCatalogSyncState.Idle -> Unit
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasQuickStartLesson) {
                    PandaActionButton(
                        text = "Start Next Lesson",
                        onClick = onContinueNextLesson,
                        containerColor = GoldBright,
                        contentColor = Color(0xFF2B1C00),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("learn_quick_start_button"),
                        leadingIcon = {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
                PandaActionButton(
                    text = "AI Drill",
                    onClick = onAiBonusDrill,
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("ai_bonus_drill_button"),
                    leadingIcon = {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun HeroStatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = GoldBright
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LearnModeSegmentedBar(
    selectedMode: LearnTabMode,
    onSelectMode: (LearnTabMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(LearnTabMode.entries, key = { it.name }) { mode ->
                val active = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectMode(mode) }
                        .testTag("learn_mode_${mode.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) JadePrimary else Color.Transparent,
                    tonalElevation = if (active) 2.dp else 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HskLevelCourseSelectorRow(
    summaries: List<HskLevelSummary>,
    selectedLevel: String,
    includeAllOption: Boolean,
    completedLessonIds: Set<String>,
    lessonIdPrefixBuilder: (levelKey: String) -> String,
    onSelectLevel: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        if (includeAllOption) {
            val allSelected = selectedLevel == "ALL"
            val totalWords = summaries.sumOf { it.activeWordCount }
            item(key = "ALL") {
                Surface(
                    modifier = Modifier
                        .width(126.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectLevel("ALL") }
                        .testTag("learn_hsk_card_all"),
                    shape = RoundedCornerShape(18.dp),
                    color = if (allSelected) JadePrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (allSelected) 2.dp else 1.dp,
                        if (allSelected) GoldBright else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "All HSK",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (allSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Levels 1–9",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (allSelected) GoldBright else JadePrimary
                        )
                        Text(
                            text = "${formatInt(totalWords)} words",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (allSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(summaries, key = { it.meta.level }) { summary ->
            val levelKey = if (summary.meta.level >= 7) "7+" else summary.meta.level.toString()
            val selected = selectedLevel == levelKey
            val wordCount = summary.activeWordCount
            val totalLessons = ((wordCount + 9) / 10).coerceAtLeast(if (wordCount > 0) 1 else 0)
            val prefix = lessonIdPrefixBuilder(levelKey)
            val doneCount = completedLessonIds.count { it.startsWith(prefix) }
            val progress = if (totalLessons > 0) (doneCount.toFloat() / totalLessons).coerceIn(0f, 1f) else 0f

            Surface(
                modifier = Modifier
                    .width(142.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectLevel(levelKey) }
                    .testTag("learn_hsk_card_$levelKey"),
                shape = RoundedCornerShape(18.dp),
                color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                tonalElevation = if (selected) 3.dp else 1.dp,
                border = BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) GoldBright else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
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
                            text = summary.meta.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${totalLessons}L",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) GoldBright else JadePrimary
                        )
                    }
                    Text(
                        text = summary.band.displayName.substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Color.White.copy(alpha = 0.9f) else JadeVibrant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatInt(wordCount)} words · $doneCount done",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (selected) GoldBright else JadePrimary,
                        trackColor = if (selected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LearnChapterSelectorRow(
    totalWords: Int,
    selectedChapterIndex: Int,
    onSelectChapter: (Int) -> Unit
) {
    val totalChapters = ((totalWords + 99) / 100).coerceAtLeast(1)
    if (totalChapters <= 1) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "CHAPTERS (100 WORDS / 10 LESSONS EACH)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(totalChapters) { chapterIdx ->
                val startWord = chapterIdx * 100 + 1
                val endWord = ((chapterIdx + 1) * 100).coerceAtMost(totalWords)
                val selected = chapterIdx == selectedChapterIndex
                FilterChip(
                    selected = selected,
                    onClick = { onSelectChapter(chapterIdx) },
                    label = {
                        Text(
                            text = "Ch.${chapterIdx + 1} ($startWord–$endWord)",
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JadePrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("learn_chapter_$chapterIdx")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabLessonSteppingCard(
    lessonNumber: Int,
    wordStartNumber: Int,
    wordEndNumber: Int,
    hskBadge: String,
    topicSubtitle: String,
    words: List<VocabWord>,
    completed: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onStartLesson: () -> Unit,
    onTapHanzi: (String) -> Unit
) {
    val borderColor = when {
        completed -> JadePrimary
        highlighted -> GoldAccent
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    }
    val xpReward = (25 + words.size * 2).coerceAtMost(50)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onStartLesson() }
            .testTag("vocab_lesson_card_$lessonNumber"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (highlighted) 3.dp else 1.dp,
        border = BorderStroke(if (highlighted || completed) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                completed -> JadePrimary
                                highlighted -> GoldBright
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Completed", tint = Color.White)
                    } else {
                        Text(
                            text = "$lessonNumber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (highlighted) Color(0xFF2B1C00) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = hskBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Words $wordStartNumber–$wordEndNumber · +$xpReward XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Lesson $lessonNumber · ${words.size} Words",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = topicSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tappable Hanzi preview chips (tap any word to hear native Mandarin pronunciation)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                words.take(6).forEach { w ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTapHanzi(w.hanzi) }
                    ) {
                        Text(
                            text = w.hanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (words.size > 6) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = "+${words.size - 6} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Learn → Listen → Quiz → Match",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (completed) MaterialTheme.colorScheme.primaryContainer else JadePrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (completed) JadeDeep else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (completed) "Replay" else "Learn",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (completed) JadeDeep else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCourseCard(
    topic: TopicCategory,
    wordCount: Int,
    completedCount: Int,
    onOpenTopic: () -> Unit
) {
    val totalLessons = ((wordCount + 9) / 10).coerceAtLeast(if (wordCount > 0) 1 else 0)
    val progress = if (totalLessons > 0) (completedCount.toFloat() / totalLessons).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onOpenTopic() }
            .testTag("learn_topic_card_${topic.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = topic.nameZh.take(2),
                        fontFamily = HanziSerifFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = topic.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${topic.nameZh} · ${formatInt(wordCount)} words · $totalLessons lessons",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = JadePrimary.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open topic lessons",
                        tint = JadePrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = JadePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$completedCount/$totalLessons",
                    style = MaterialTheme.typography.labelSmall,
                    color = JadePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
