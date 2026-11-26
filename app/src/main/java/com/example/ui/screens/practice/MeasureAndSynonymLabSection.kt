package com.example.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MeasureAndSynonymData
import com.example.model.LabHskBand
import com.example.model.MeasureDrillQuestion
import com.example.model.MeasureLabMainTab
import com.example.model.MeasureSubMode
import com.example.model.MeasureWordEntry
import com.example.model.SynonymBattleItem
import com.example.model.SynonymQuizPrompt
import com.example.model.SynonymSide
import com.example.model.SynonymSubMode
import com.example.model.UiState
import com.example.model.VocabWord
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.components.buildToneColoredHanziString
import com.example.ui.components.buildToneColoredPinyinString
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@Composable
fun MeasureAndSynonymLabSection(
    viewModel: PandaViewModel
) {
    var activeTab by rememberSaveable { mutableStateOf(MeasureLabMainTab.MEASURE_WORDS) }
    var selectedBand by rememberSaveable { mutableStateOf(LabHskBand.ALL) }
    var measureSubMode by rememberSaveable { mutableStateOf(MeasureSubMode.CHEAT_SHEET) }
    var synonymSubMode by rememberSaveable { mutableStateOf(SynonymSubMode.VS_CARDS) }

    val extraExamplesMap by viewModel.synonymExtraExamples.collectAsStateWithLifecycle()
    val savedAnkiIds = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measure_synonym_lab_root")
    ) {
        // Header + Top Segmented Mode Switcher + HSK Filter Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compact Hero Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(JadeDeep, JadePrimary, IndigoAccent.copy(alpha = 0.9f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Balance,
                                    contentDescription = "Measure and Synonyms Battle Lab",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Measure Words & Synonyms Lab",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "量词视觉逻辑 · 近义词 PK 辨析",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Segmented Main Tab Switcher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MeasureLabMainTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeTab = tab }
                                .testTag("lab_main_tab_${tab.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (selected) 2.dp else 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // HSK Band Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabHskBand.entries.forEach { band ->
                    val selected = selectedBand == band
                    Surface(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedBand = band }
                            .testTag("lab_hsk_chip_${band.name.lowercase()}"),
                        shape = RoundedCornerShape(50),
                        color = if (selected) IndigoAccent else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) IndigoAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = band.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Sub-view Switcher Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeTab == MeasureLabMainTab.MEASURE_WORDS) {
                    MeasureSubMode.entries.forEach { sub ->
                        val selected = measureSubMode == sub
                        val icon = if (sub == MeasureSubMode.CHEAT_SHEET) Icons.Rounded.Style else Icons.Rounded.Bolt
                        SubModePillButton(
                            label = sub.label,
                            icon = icon,
                            selected = selected,
                            accentColor = JadePrimary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("measure_submode_${sub.name.lowercase()}"),
                            onClick = { measureSubMode = sub }
                        )
                    }
                } else {
                    SynonymSubMode.entries.forEach { sub ->
                        val selected = synonymSubMode == sub
                        val icon = if (sub == SynonymSubMode.VS_CARDS) Icons.Rounded.Balance else Icons.Rounded.Bolt
                        SubModePillButton(
                            label = sub.label,
                            icon = icon,
                            selected = selected,
                            accentColor = CoralVermilion,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("synonym_submode_${sub.name.lowercase()}"),
                            onClick = { synonymSubMode = sub }
                        )
                    }
                }
            }
        }

        // Main Content Area
        when (activeTab) {
            MeasureLabMainTab.MEASURE_WORDS -> {
                when (measureSubMode) {
                    MeasureSubMode.CHEAT_SHEET -> MeasureVisualCheatSheet(
                        band = selectedBand,
                        savedIds = savedAnkiIds,
                        onSpeak = { viewModel.speakChinese(it) },
                        onSaveToAnki = { entry ->
                            viewModel.saveMeasureOrSynonymItemsToAnki(
                                words = listOf(entry.toVocabWord()),
                                sourceLabel = "measure word"
                            )
                            if (entry.id !in savedAnkiIds) savedAnkiIds.add(entry.id)
                        }
                    )
                    MeasureSubMode.SPEED_DRILL -> MeasureSpeedDrillArena(
                        band = selectedBand,
                        onSpeak = { viewModel.speakChinese(it) },
                        onCompleteRound = { score, total ->
                            viewModel.completeMeasureOrSynonymRound(
                                modeLabel = "Measure Words Speed Drill",
                                correctCount = score,
                                totalCount = total,
                                minutesStudied = 2
                            )
                        },
                        onSendMissedToAnki = { missed ->
                            viewModel.saveMeasureOrSynonymItemsToAnki(
                                words = missed.map { it.toVocabWord() },
                                sourceLabel = "missed measure word"
                            )
                        }
                    )
                }
            }
            MeasureLabMainTab.SYNONYM_BATTLE -> {
                when (synonymSubMode) {
                    SynonymSubMode.VS_CARDS -> SynonymVsCardsList(
                        band = selectedBand,
                        extraExamplesMap = extraExamplesMap,
                        savedIds = savedAnkiIds,
                        onSpeak = { viewModel.speakChinese(it) },
                        onRequestMoreExamples = { battle ->
                            viewModel.requestSynonymExtraExamples(battle)
                        },
                        onSavePairToAnki = { battle ->
                            viewModel.saveMeasureOrSynonymItemsToAnki(
                                words = battle.sides.map { it.toVocabWord(battle.hskLevel, battle.id) },
                                sourceLabel = "near-synonym"
                            )
                            if (battle.id !in savedAnkiIds) savedAnkiIds.add(battle.id)
                        }
                    )
                    SynonymSubMode.BATTLE_QUIZ -> SynonymBattleQuizArena(
                        band = selectedBand,
                        onSpeak = { viewModel.speakChinese(it) },
                        onCompleteRound = { score, total ->
                            viewModel.completeMeasureOrSynonymRound(
                                modeLabel = "Near-Synonyms PK Quiz",
                                correctCount = score,
                                totalCount = total,
                                minutesStudied = 2
                            )
                        },
                        onSendMissedToAnki = { missedWords ->
                            viewModel.saveMeasureOrSynonymItemsToAnki(
                                words = missedWords,
                                sourceLabel = "missed near-synonym"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubModePillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// TAB 1A: MEASURE WORDS VISUAL CHEAT SHEET
// ============================================================================

@Composable
private fun MeasureVisualCheatSheet(
    band: LabHskBand,
    savedIds: List<String>,
    onSpeak: (String) -> Unit,
    onSaveToAnki: (MeasureWordEntry) -> Unit
) {
    val entries = remember(band) { MeasureAndSynonymData.measureWordsForBand(band) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measure_cheat_sheet_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entries.size} Essential Classifiers (${band.label})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any phrase for audio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ToneColorLegendRow()
            }
        }

        items(entries, key = { it.id }) { entry ->
            val isSaved = entry.id in savedIds
            MeasureCheatSheetCard(
                entry = entry,
                isSaved = isSaved,
                onSpeak = onSpeak,
                onSaveToAnki = { onSaveToAnki(entry) }
            )
        }
    }
}

@Composable
private fun MeasureCheatSheetCard(
    entry: MeasureWordEntry,
    isSaved: Boolean,
    onSpeak: (String) -> Unit,
    onSaveToAnki: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("measure_card_${entry.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Big Classifier + Category + Save to Anki
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSpeak(entry.hanzi) },
                    shape = RoundedCornerShape(16.dp),
                    color = JadePrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ToneColoredHanziPinyinBlock(
                            hanzi = entry.hanzi,
                            pinyin = entry.pinyin,
                            hanziFontSize = 28.sp,
                            showPinyin = true,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoAccent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "HSK ${entry.hskLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoAccent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "${entry.shapeGlyph} ${entry.visualCategory}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { onSpeak(entry.hanzi) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("speak_measure_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Hear measure word ${entry.hanzi}",
                        tint = JadePrimary
                    )
                }
            }

            // Visual Logic Rule Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = entry.visualLogicRule,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 3 Example Noun Phrases with Audio Playback
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entry.examples.forEachIndexed { idx, ex ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSpeak(ex.fullHanzi) }
                            .testTag("measure_example_${entry.id}_$idx"),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = buildToneColoredHanziString(
                                            hanzi = ex.fullHanzi,
                                            pinyin = ex.fullPinyin,
                                            defaultColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        fontFamily = HanziSerifFontFamily,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = buildToneColoredPinyinString(ex.fullPinyin),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = ex.english,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Speak ${ex.fullHanzi}",
                                tint = JadePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // One-tap Save to Anki Button
            OutlinedButton(
                onClick = onSaveToAnki,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("save_measure_anki_${entry.id}"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSaved) JadePrimary else MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                    contentDescription = null,
                    tint = if (isSaved) JadePrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "Saved to Anki SM-2 ✓" else "Save '${entry.hanzi}' to Anki",
                    color = if (isSaved) JadePrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// TAB 1B: MEASURE WORDS SPEED DRILL
// ============================================================================

@Composable
private fun MeasureSpeedDrillArena(
    band: LabHskBand,
    onSpeak: (String) -> Unit,
    onCompleteRound: (score: Int, total: Int) -> Unit,
    onSendMissedToAnki: (List<MeasureWordEntry>) -> Unit
) {
    var roundSeed by remember(band) { mutableIntStateOf(0) }
    val questions = remember(band, roundSeed) {
        MeasureAndSynonymData.buildSpeedDrillRound(band = band, count = 10)
    }
    var currentIndex by remember(band, roundSeed) { mutableIntStateOf(0) }
    var selectedEntryId by remember(band, roundSeed, currentIndex) { mutableStateOf<String?>(null) }
    var score by remember(band, roundSeed) { mutableIntStateOf(0) }
    var streak by remember(band, roundSeed) { mutableIntStateOf(0) }
    var bestStreak by remember(band, roundSeed) { mutableIntStateOf(0) }
    var finished by remember(band, roundSeed) { mutableStateOf(false) }
    val missedEntries = remember(band, roundSeed) { mutableStateListOf<MeasureWordEntry>() }
    var missedSentToAnki by remember(band, roundSeed) { mutableStateOf(false) }

    if (questions.isEmpty()) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measure_speed_drill_arena"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (finished) {
            item {
                DrillSummaryCard(
                    title = "Speed Drill Complete!",
                    subtitle = "Measure Words · ${band.label}",
                    score = score,
                    total = questions.size,
                    bestStreak = bestStreak,
                    missedLabels = missedEntries.distinctBy { it.id }.map { "${it.hanzi} (${it.pinyin}) — ${it.visualCategory}" },
                    missedSentToAnki = missedSentToAnki,
                    sendAnkiTag = "measure_send_missed_anki",
                    restartTag = "measure_drill_restart",
                    onSendMissed = {
                        onSendMissedToAnki(missedEntries.distinctBy { it.id })
                        missedSentToAnki = true
                    },
                    onRestart = { roundSeed++ }
                )
            }
        } else {
            val question = questions[currentIndex]
            val answered = selectedEntryId != null
            val isCorrect = selectedEntryId == question.correctEntry.id

            item {
                // HUD Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} / ${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = JadePrimary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "Score $score",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = JadePrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (streak >= 2) CoralVermilion.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = if (streak >= 2) CoralVermilion else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${streak}x",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (streak >= 2) CoralVermilion else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (currentIndex + if (answered) 1 else 0).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = JadePrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            item {
                // Prompt Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pick the right measure word",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!answered) {
                            Text(
                                text = question.promptBlankHanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            ToneColoredHanziPinyinBlock(
                                hanzi = question.fullHanzi,
                                pinyin = question.fullPinyin,
                                hanziFontSize = 34.sp,
                                showPinyin = true
                            )
                        }

                        Text(
                            text = "\"${question.english}\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = {
                                onSpeak(if (answered) question.fullHanzi else question.nounOnlyHanzi)
                            },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("measure_drill_audio_btn"),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Play audio",
                                tint = JadePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (answered) "Hear full phrase" else "Hear noun (${question.nounOnlyHanzi})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                // 2x2 Option Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.optionEntries.chunked(2).forEachIndexed { rowIdx, rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowOptions.forEachIndexed { colIdx, option ->
                                val optIndex = rowIdx * 2 + colIdx
                                val isThisCorrect = option.id == question.correctEntry.id
                                val isThisSelected = option.id == selectedEntryId

                                val bgColor = when {
                                    !answered -> MaterialTheme.colorScheme.surface
                                    isThisCorrect -> JadePrimary.copy(alpha = 0.16f)
                                    isThisSelected -> CoralVermilion.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }
                                val borderColor = when {
                                    !answered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    isThisCorrect -> JadePrimary
                                    isThisSelected -> CoralVermilion
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 76.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(enabled = !answered) {
                                            selectedEntryId = option.id
                                            if (option.id == question.correctEntry.id) {
                                                score++
                                                streak++
                                                if (streak > bestStreak) bestStreak = streak
                                                onSpeak(question.fullHanzi)
                                            } else {
                                                streak = 0
                                                missedEntries.add(question.correctEntry)
                                            }
                                        }
                                        .testTag("measure_drill_option_$optIndex"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = bgColor,
                                    border = BorderStroke(if (answered && (isThisCorrect || isThisSelected)) 2.dp else 1.dp, borderColor)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = buildToneColoredHanziString(
                                                    hanzi = option.hanzi,
                                                    pinyin = option.pinyin,
                                                    defaultColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                fontFamily = HanziSerifFontFamily,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = buildToneColoredPinyinString(option.pinyin),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (answered) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCorrect) JadePrimary.copy(alpha = 0.12f) else CoralVermilion.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isCorrect) JadePrimary else CoralVermilion)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) JadePrimary else CoralVermilion
                                )
                                Text(
                                    text = if (isCorrect) "Spot on! ${question.fullHanzi}" else "Correct answer: ${question.correctEntry.hanzi} (${question.fullHanzi})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) JadePrimary else CoralVermilion
                                )
                            }
                            Text(
                                text = question.whyItFits,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < questions.size) {
                                        currentIndex++
                                        selectedEntryId = null
                                    } else {
                                        finished = true
                                        onCompleteRound(score, questions.size)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("measure_drill_next_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary)
                            ) {
                                Text(
                                    text = if (currentIndex + 1 < questions.size) "Next Question" else "Finish Round & Claim XP",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2A: CONFUSABLE NEAR-SYNONYMS VS CARDS
// ============================================================================

@Composable
private fun SynonymVsCardsList(
    band: LabHskBand,
    extraExamplesMap: Map<String, UiState<String>>,
    savedIds: List<String>,
    onSpeak: (String) -> Unit,
    onRequestMoreExamples: (SynonymBattleItem) -> Unit,
    onSavePairToAnki: (SynonymBattleItem) -> Unit
) {
    val battles = remember(band) { MeasureAndSynonymData.synonymBattlesForBand(band) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("synonym_vs_cards_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${battles.size} Classic Near-Synonym Battles (${band.label})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                ToneColorLegendRow()
            }
        }

        items(battles, key = { it.id }) { battle ->
            SynonymVsBattleCard(
                battle = battle,
                extraExamplesState = extraExamplesMap[battle.id],
                isSaved = battle.id in savedIds,
                onSpeak = onSpeak,
                onRequestMoreExamples = { onRequestMoreExamples(battle) },
                onSavePairToAnki = { onSavePairToAnki(battle) }
            )
        }
    }
}

@Composable
private fun SynonymVsBattleCard(
    battle: SynonymBattleItem,
    extraExamplesState: UiState<String>?,
    isSaved: Boolean,
    onSpeak: (String) -> Unit,
    onRequestMoreExamples: () -> Unit,
    onSavePairToAnki: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("synonym_card_${battle.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Title + HSK Badge + Save to Anki icon button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CoralVermilion.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "HSK ${battle.hskLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralVermilion,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = battle.title,
                        fontFamily = HanziSerifFontFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onSavePairToAnki,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("save_synonym_anki_${battle.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                        contentDescription = "Save ${battle.title} to Anki",
                        tint = if (isSaved) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Core Rule Difference Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = IndigoAccent.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = IndigoAccent,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = battle.coreDifference,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Side-by-Side Comparison Panels (responsive with BoxWithConstraints)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val useSideBySide = battle.sides.size == 2 && maxWidth >= 320.dp
                if (useSideBySide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        battle.sides.forEachIndexed { idx, side ->
                            val accent = if (idx == 0) JadePrimary else CoralVermilion
                            SynonymSidePanel(
                                side = side,
                                accentColor = accent,
                                onSpeak = onSpeak,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        battle.sides.forEachIndexed { idx, side ->
                            val accent = when (idx) {
                                0 -> JadePrimary
                                1 -> CoralVermilion
                                else -> IndigoAccent
                            }
                            SynonymSidePanel(
                                side = side,
                                accentColor = accent,
                                onSpeak = onSpeak,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Common Mistake Callout
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CoralVermilion.copy(alpha = 0.09f),
                border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = CoralVermilion,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Common Mistake",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CoralVermilion
                        )
                    }
                    Text(
                        text = "❌ ${battle.commonMistakeWrong}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralVermilion
                    )
                    Text(
                        text = "✅ ${battle.commonMistakeRight}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = JadePrimary
                    )
                    Text(
                        text = battle.commonMistakeNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Ask Panda Laoshi for 2 More Examples (Gemini AI + Graceful Offline Fallback)
            when (extraExamplesState) {
                is UiState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = JadeLightContainer.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = JadePrimary
                            )
                            Text(
                                text = "Panda Laoshi is crafting 2 fresh contrast examples...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = JadePrimary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = JadePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Panda Laoshi's Extra Examples",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = JadePrimary
                                )
                            }
                            Text(
                                text = extraExamplesState.data,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                else -> Unit
            }

            OutlinedButton(
                onClick = onRequestMoreExamples,
                enabled = extraExamplesState !is UiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("ask_laoshi_synonym_${battle.id}"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = JadePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (extraExamplesState is UiState.Success) {
                        "Ask Panda Laoshi for 2 Different Examples"
                    } else {
                        "Ask Panda Laoshi for 2 More Examples"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = JadePrimary
                )
            }
        }
    }
}

@Composable
private fun SynonymSidePanel(
    side: SynonymSide,
    accentColor: Color,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToneColoredHanziPinyinBlock(
                    hanzi = side.hanzi,
                    pinyin = side.pinyin,
                    hanziFontSize = 22.sp,
                    showPinyin = true
                )
                IconButton(
                    onClick = { onSpeak(side.hanzi) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Pronounce ${side.hanzi}",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = side.coreMeaning,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = side.usageRule,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Tappable Tone-Colored Example Sentence
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSpeak(side.exampleHanzi) },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = buildToneColoredHanziString(
                                hanzi = side.exampleHanzi,
                                pinyin = side.examplePinyin,
                                defaultColor = MaterialTheme.colorScheme.onSurface
                            ),
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Hear sentence ${side.exampleHanzi}",
                            tint = accentColor,
                            modifier = Modifier
                                .padding(start = 4.dp, top = 2.dp)
                                .size(16.dp)
                        )
                    }
                    Text(
                        text = buildToneColoredPinyinString(side.examplePinyin),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = side.exampleEnglish,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 2B: NEAR-SYNONYMS BATTLE QUIZ (近义词 PK)
// ============================================================================

@Composable
private fun SynonymBattleQuizArena(
    band: LabHskBand,
    onSpeak: (String) -> Unit,
    onCompleteRound: (score: Int, total: Int) -> Unit,
    onSendMissedToAnki: (List<VocabWord>) -> Unit
) {
    var roundSeed by remember(band) { mutableIntStateOf(0) }
    val questions = remember(band, roundSeed) {
        MeasureAndSynonymData.buildSynonymBattleQuiz(band = band, count = 10)
    }
    var currentIndex by remember(band, roundSeed) { mutableIntStateOf(0) }
    var selectedOption by remember(band, roundSeed, currentIndex) { mutableStateOf<String?>(null) }
    var score by remember(band, roundSeed) { mutableIntStateOf(0) }
    var streak by remember(band, roundSeed) { mutableIntStateOf(0) }
    var bestStreak by remember(band, roundSeed) { mutableIntStateOf(0) }
    var finished by remember(band, roundSeed) { mutableStateOf(false) }
    val missedPrompts = remember(band, roundSeed) { mutableStateListOf<SynonymQuizPrompt>() }
    var missedSentToAnki by remember(band, roundSeed) { mutableStateOf(false) }

    if (questions.isEmpty()) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("synonym_battle_quiz_arena"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (finished) {
            item {
                val distinctMissedBattles = missedPrompts.distinctBy { it.battleId }
                DrillSummaryCard(
                    title = "Synonym Battle Complete!",
                    subtitle = "近义词 PK · ${band.label}",
                    score = score,
                    total = questions.size,
                    bestStreak = bestStreak,
                    missedLabels = distinctMissedBattles.map { "${it.battleTitle} (HSK ${it.hskLevel})" },
                    missedSentToAnki = missedSentToAnki,
                    sendAnkiTag = "synonym_send_missed_anki",
                    restartTag = "synonym_quiz_restart",
                    onSendMissed = {
                        val vocabToSave = distinctMissedBattles.flatMap { prompt ->
                            prompt.targetSides.map { side ->
                                side.toVocabWord(prompt.hskLevel, prompt.battleId)
                            }
                        }
                        onSendMissedToAnki(vocabToSave)
                        missedSentToAnki = true
                    },
                    onRestart = { roundSeed++ }
                )
            }
        } else {
            val prompt = questions[currentIndex]
            val answered = selectedOption != null
            val isCorrect = selectedOption == prompt.correctOption

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battle ${currentIndex + 1} / ${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CoralVermilion.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${prompt.battleTitle} · HSK ${prompt.hskLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralVermilion,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (streak >= 2) GoldAccent.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = if (streak >= 2) CoralVermilion else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${streak}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (currentIndex + if (answered) 1 else 0).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = CoralVermilion,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Choose the natural Mandarin word",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!answered) {
                            Text(
                                text = prompt.sentenceWithBlank,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = buildToneColoredHanziString(
                                        hanzi = prompt.fullHanzi,
                                        pinyin = prompt.fullPinyin,
                                        defaultColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 34.sp
                                )
                                Text(
                                    text = buildToneColoredPinyinString(prompt.fullPinyin),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Text(
                            text = "\"${prompt.english}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (answered) {
                            OutlinedButton(
                                onClick = { onSpeak(prompt.fullHanzi) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("synonym_quiz_audio_btn"),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Hear full sentence",
                                    tint = JadePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Listen to native sentence", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                // PK Option Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    prompt.options.forEachIndexed { idx, opt ->
                        val isWinner = opt == prompt.correctOption
                        val isPicked = opt == selectedOption
                        val sidePinyin = prompt.targetSides.firstOrNull { it.hanzi == opt }?.pinyin.orEmpty()

                        val bgColor = when {
                            !answered -> MaterialTheme.colorScheme.surface
                            isWinner -> JadePrimary.copy(alpha = 0.16f)
                            isPicked -> CoralVermilion.copy(alpha = 0.16f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                        val borderColor = when {
                            !answered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            isWinner -> JadePrimary
                            isPicked -> CoralVermilion
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !answered) {
                                    selectedOption = opt
                                    if (opt == prompt.correctOption) {
                                        score++
                                        streak++
                                        if (streak > bestStreak) bestStreak = streak
                                        onSpeak(prompt.fullHanzi)
                                    } else {
                                        streak = 0
                                        missedPrompts.add(prompt)
                                    }
                                }
                                .testTag("synonym_quiz_option_$idx"),
                            shape = RoundedCornerShape(16.dp),
                            color = bgColor,
                            border = BorderStroke(
                                if (answered && (isWinner || isPicked)) 2.dp else 1.dp,
                                borderColor
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (sidePinyin.isNotBlank()) {
                                        buildToneColoredHanziString(opt, sidePinyin, MaterialTheme.colorScheme.onSurface)
                                    } else {
                                        androidx.compose.ui.text.AnnotatedString(opt)
                                    },
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (sidePinyin.isNotBlank()) {
                                    Text(
                                        text = buildToneColoredPinyinString(sidePinyin),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (answered) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCorrect) JadePrimary.copy(alpha = 0.12f) else CoralVermilion.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isCorrect) JadePrimary else CoralVermilion)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) JadePrimary else CoralVermilion
                                )
                                Text(
                                    text = if (isCorrect) "Winner: ${prompt.correctOption} ✓" else "Winner is ${prompt.correctOption}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) JadePrimary else CoralVermilion
                                )
                            }
                            Text(
                                text = prompt.grammarReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < questions.size) {
                                        currentIndex++
                                        selectedOption = null
                                    } else {
                                        finished = true
                                        onCompleteRound(score, questions.size)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("synonym_quiz_next_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary)
                            ) {
                                Text(
                                    text = if (currentIndex + 1 < questions.size) "Next Battle" else "Finish Battle & Claim XP",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SHARED ROUND COMPLETION SUMMARY CARD
// ============================================================================

@Composable
private fun DrillSummaryCard(
    title: String,
    subtitle: String,
    score: Int,
    total: Int,
    bestStreak: Int,
    missedLabels: List<String>,
    missedSentToAnki: Boolean,
    sendAnkiTag: String,
    restartTag: String,
    onSendMissed: () -> Unit,
    onRestart: () -> Unit
) {
    val accuracy = if (total > 0) (score * 100) / total else 0
    val xpEstimate = (score * 4 + 10).coerceIn(10, 60)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.5f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = JadePrimary.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = JadePrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatChip(
                    label = "Score",
                    value = "$score / $total",
                    accent = JadePrimary,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatChip(
                    label = "Accuracy",
                    value = "$accuracy%",
                    accent = IndigoAccent,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatChip(
                    label = "Best Streak",
                    value = "${bestStreak}x",
                    accent = CoralVermilion,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = GoldAccent.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "+$xpEstimate XP logged to Daily Learning Tracker!",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            if (missedLabels.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = CoralVermilion.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tricky Items to Reinforce (${missedLabels.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CoralVermilion
                        )
                        missedLabels.forEach { label ->
                            Text(
                                text = "• $label",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = onSendMissed,
                            enabled = !missedSentToAnki,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag(sendAnkiTag),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVermilion)
                        ) {
                            Icon(
                                imageVector = if (missedSentToAnki) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (missedSentToAnki) "Sent to Anki SM-2 ✓" else "Send Missed to Anki",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag(restartTag),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Play New Round",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SummaryStatChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
