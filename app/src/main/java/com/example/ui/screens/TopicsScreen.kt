package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.HskCatalogSyncState
import com.example.model.TopicCategory
import com.example.model.VocabWord
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import java.text.NumberFormat

@Composable
fun TopicsScreen(viewModel: PandaViewModel) {
    val totalVocabCount by viewModel.totalVocabCount.collectAsStateWithLifecycle()
    val catalogSyncState by viewModel.catalogSyncState.collectAsStateWithLifecycle()
    val hskTopics by viewModel.hskTopics.collectAsStateWithLifecycle()
    val levelSummaries by viewModel.hskLevelSummaries.collectAsStateWithLifecycle()
    val topicWordCounts by viewModel.topicWordCounts.collectAsStateWithLifecycle()
    val vocabFilter by viewModel.vocabFilter.collectAsStateWithLifecycle()
    val filteredVocab by viewModel.filteredVocab.collectAsStateWithLifecycle()
    val filteredTotalCount by viewModel.filteredTotalCount.collectAsStateWithLifecycle()
    val isGeneratingPack by viewModel.isGeneratingWordPack.collectAsStateWithLifecycle()

    var selectedTopic by remember { mutableStateOf<TopicCategory?>(null) }
    var showAiGenerator by remember { mutableStateOf(false) }
    var showCsvImporter by remember { mutableStateOf(false) }

    val levelFilter = vocabFilter.levelFilter
    val searchQuery = vocabFilter.searchQuery
    val writableOnly = vocabFilter.writableOnly
    val hsk30NewOnly = vocabFilter.hsk30NewOnly
    val formattedTotal = remember(totalVocabCount) { NumberFormat.getIntegerInstance().format(totalVocabCount) }

    if (selectedTopic != null) {
        BackHandler {
            selectedTopic = null
            viewModel.setVocabTopicFilter(null)
        }
        TopicDetailView(
            topic = selectedTopic!!,
            words = filteredVocab,
            totalMatchingCount = filteredTotalCount,
            onLoadMore = { viewModel.loadMoreFilteredVocab() },
            onBack = {
                selectedTopic = null
                viewModel.setVocabTopicFilter(null)
            },
            onSpeak = { viewModel.speakChinese(it) },
            onInspectWord = { viewModel.openWordBreakdown(it) },
            onWrite = { viewModel.jumpToWriteWord(it) },
            onAddSrs = { viewModel.addWordToSrs(it) },
            onLaunchAiLesson = {
                viewModel.launchTopicLessonInAiTeacher(selectedTopic!!.nameEn, selectedTopic!!.minHsk)
            },
            onLaunchListening = { viewModel.jumpToListeningPractice() },
            onLaunchSentence = { viewModel.jumpToSentencePractice() }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HSK 3.0 Standard Syllabus Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF005B48), JadeDeep, JadePrimary)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "OFFICIAL HSK 3.0 · 11,000-WORD ROADMAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldBright
                    )
                    Text(
                        text = "Topic-by-Topic Mastery",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Explore $formattedTotal HSK 3.0 words offline across Levels 1–9 and 14 real-life topics, generate custom AI packs, or bulk import CSV lists.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    // Full 11,000+ Word HSK 3.0 Offline Catalog Sync Indicator
                    when (val sync = catalogSyncState) {
                        is HskCatalogSyncState.Syncing -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.14f)
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
                                        Text(
                                            text = sync.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${sync.progressPercent}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (sync.progressPercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = GoldBright,
                                        trackColor = Color.White.copy(alpha = 0.22f)
                                    )
                                }
                            }
                        }

                        is HskCatalogSyncState.Ready -> {
                            if (sync.fullCatalogLoaded) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Full HSK 3.0 dictionary ($formattedTotal words) cached offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.syncFullHsk30Catalog(force = true) }
                                        .testTag("sync_full_hsk30_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Download Full 11,000+ HSK 3.0 Dictionary",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        is HskCatalogSyncState.Error -> {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.syncFullHsk30Catalog(force = true) }
                                    .testTag("retry_sync_hsk30_button"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Offline (${sync.currentWords} words) · Tap to sync full 11,000+ HSK 3.0 list",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        HskCatalogSyncState.Idle -> Unit
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PandaActionButton(
                            text = "AI Wordpack",
                            onClick = { showAiGenerator = true },
                            containerColor = GoldBright,
                            contentColor = Color(0xFF2B1C00),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_ai_wordpack_button"),
                            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        OutlinedButton(
                            onClick = { showCsvImporter = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("open_csv_importer_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CSV Import", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // HSK 3.0 9-Level Roadmap Strip (driven by HskVocabularyRepository)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levelSummaries.forEach { summary ->
                    val meta = summary.meta
                    val filterKey = if (meta.level >= 7) "7+" else meta.level.toString()
                    val selected = levelFilter == filterKey
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setVocabLevelFilter(if (selected) "ALL" else filterKey)
                            }
                            .testTag("hsk_level_chip_$filterKey"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = meta.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = meta.band,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${summary.activeWordCount} in deck · ${meta.cumulative}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Search & Writable Toggle
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setVocabSearchQuery(it) },
                        placeholder = { Text("Search Hanzi, Pinyin, English, or grammar...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setVocabSearchQuery("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = JadePrimary)
                            Text(
                                text = if (levelFilter == "ALL") "All Levels (${NumberFormat.getIntegerInstance().format(filteredTotalCount)} words)" else "HSK $levelFilter (${NumberFormat.getIntegerInstance().format(filteredTotalCount)} words)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = hsk30NewOnly,
                                onClick = { viewModel.setVocabHsk30NewOnly(!hsk30NewOnly) },
                                label = { Text("HSK 3.0 New", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("filter_hsk30_new_chip")
                            )
                            Text("Writable", style = MaterialTheme.typography.labelSmall)
                            Switch(
                                checked = writableOnly,
                                onCheckedChange = { viewModel.setVocabWritableOnly(it) }
                            )
                        }
                    }
                }
            }
        }

        // Direct paginated vocabulary list when searching, filtering by level, or showing HSK 3.0 New terms
        if (searchQuery.isNotBlank() || hsk30NewOnly || levelFilter != "ALL" || writableOnly) {
            item {
                Text(
                    text = "Showing ${filteredVocab.size} of ${NumberFormat.getIntegerInstance().format(filteredTotalCount)} matching words",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(filteredVocab, key = { it.key }) { word ->
                VocabWordCard(
                    word = word,
                    onSpeak = { viewModel.speakChinese(word.hanzi) },
                    onInspect = { viewModel.openWordBreakdown(word) },
                    onWrite = { viewModel.jumpToWriteWord(word) },
                    onAddSrs = { viewModel.addWordToSrs(word) }
                )
            }
            if (filteredVocab.size < filteredTotalCount) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.loadMoreFilteredVocab() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_more_vocab_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Load 60 more words (${filteredTotalCount - filteredVocab.size} remaining)")
                    }
                }
            }
        } else {
            // 14 Real-Life Topics Grid (2 columns per row)
            val rows = hskTopics.chunked(2)
            items(rows) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { topic ->
                        val count = topicWordCounts[topic.id] ?: 0
                        TopicGridCard(
                            topic = topic,
                            wordCount = count,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTopic = topic
                                viewModel.setVocabTopicFilter(topic.id)
                            }
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showAiGenerator) {
        AiWordPackDialog(
            isGenerating = isGeneratingPack,
            onGenerate = { level, topicId, theme ->
                viewModel.generateAiWordPack(level, topicId, theme) {
                    showAiGenerator = false
                }
            },
            onDismiss = { if (!isGeneratingPack) showAiGenerator = false }
        )
    }

    if (showCsvImporter) {
        CsvImporterDialog(
            onImport = { csv ->
                viewModel.importCsvWords(csv) {
                    showCsvImporter = false
                }
            },
            onDismiss = { showCsvImporter = false }
        )
    }
}

@Composable
private fun TopicGridCard(
    topic: TopicCategory,
    wordCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("topic_card_${topic.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = topic.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "$wordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = topic.nameZh,
                fontFamily = HanziSerifFontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = JadeDeep
            )
            Text(
                text = topic.nameEn,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun TopicDetailView(
    topic: TopicCategory,
    words: List<VocabWord>,
    totalMatchingCount: Int,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onInspectWord: (VocabWord) -> Unit,
    onWrite: (VocabWord) -> Unit,
    onAddSrs: (VocabWord) -> Unit,
    onLaunchAiLesson: () -> Unit,
    onLaunchListening: () -> Unit,
    onLaunchSentence: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to topics")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${topic.nameZh} · ${topic.nameEn}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Showing ${words.size} of ${NumberFormat.getIntegerInstance().format(totalMatchingCount)} words in topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Action Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PandaActionButton(
                    text = "AI Topic Lesson",
                    onClick = onLaunchAiLesson,
                    containerColor = JadePrimary,
                    leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                PandaActionButton(
                    text = "Listening",
                    onClick = onLaunchListening,
                    containerColor = IndigoAccent,
                    leadingIcon = { Icon(Icons.Rounded.Headphones, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                PandaActionButton(
                    text = "Sentences",
                    onClick = onLaunchSentence,
                    containerColor = GoldBright,
                    contentColor = Color(0xFF2B1C00)
                )
            }
        }

        if (words.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "No words match the active HSK level filter in this topic yet. Clear the level filter or generate an AI Wordpack!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(words, key = { it.key }) { word ->
                VocabWordCard(
                    word = word,
                    onSpeak = { onSpeak(word.hanzi) },
                    onInspect = { onInspectWord(word) },
                    onWrite = { onWrite(word) },
                    onAddSrs = { onAddSrs(word) }
                )
            }
            if (words.size < totalMatchingCount) {
                item {
                    OutlinedButton(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_more_topic_vocab_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Load 60 more words (${totalMatchingCount - words.size} remaining)")
                    }
                }
            }
        }
    }
}

@Composable
fun VocabWordCard(
    word: VocabWord,
    onSpeak: () -> Unit,
    onInspect: () -> Unit = {},
    onWrite: () -> Unit,
    onAddSrs: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = word.hanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = word.pinyin,
                            style = MaterialTheme.typography.titleSmall,
                            color = JadePrimary
                        )
                    }
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (word.partOfSpeech.isNotBlank() || word.isHsk30NewTerm) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (word.partOfSpeech.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = word.partOfSpeech,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (word.isHsk30NewTerm) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldContainer
                                ) {
                                    Text(
                                        text = "HSK 3.0 New",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldOnContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                HskLevelPill(level = word.hskLevel)
            }

            if (word.exampleHanzi.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = word.exampleHanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (word.examplePinyin.isNotBlank()) {
                            Text(word.examplePinyin, style = MaterialTheme.typography.labelSmall, color = JadePrimary)
                        }
                        if (word.exampleEnglish.isNotBlank()) {
                            Text(word.exampleEnglish, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSpeak) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Listen")
                }
                TextButton(
                    onClick = onInspect,
                    modifier = Modifier.testTag("vocab_story_button_${word.key}")
                ) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = "Story & Radicals", tint = JadePrimary, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Story")
                }
                TextButton(onClick = onWrite) {
                    Icon(Icons.Rounded.Brush, contentDescription = "Write", modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Write")
                }
                TextButton(onClick = onAddSrs) {
                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = "Add to SM-2", modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("SM-2")
                }
            }
        }
    }
}

@Composable
private fun AiWordPackDialog(
    isGenerating: Boolean,
    onGenerate: (Int, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLevel by remember { mutableIntStateOf(4) }
    var themeText by remember { mutableStateOf("Mobile Payments & Smart Travel") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = JadePrimary) },
        title = { Text("AI HSK 3.0 Topic Pack") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Generate 10 authentic HSK 3.0 vocabulary words (with Pinyin & example sentences) and save them to your cloud library.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Select HSK Level (1–9):", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..9).forEach { lvl ->
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl },
                            label = { Text("HSK $lvl") }
                        )
                    }
                }
                OutlinedTextField(
                    value = themeText,
                    onValueChange = { themeText = it },
                    label = { Text("Topic Theme") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isGenerating) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Panda Laoshi is generating 10 words...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isGenerating && themeText.isNotBlank(),
                onClick = { onGenerate(selectedLevel, 10, themeText) }
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(enabled = !isGenerating, onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CsvImporterDialog(
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var csvInput by remember {
        mutableStateOf("外卖,wài mài,Food delivery,3,5\n地铁站,dì tiě zhàn,Subway station,2,8")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = JadePrimary) },
        title = { Text("Bulk CSV Word Importer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Format per line: hanzi,pinyin,english,hskLevel(1-9),topicId(1-14)",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = csvInput,
                    onValueChange = { csvInput = it },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(csvInput) }) { Text("Import to Cloud") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
