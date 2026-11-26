package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HskCurriculumData
import com.example.model.GradedStory
import com.example.model.SrsCardDoc
import com.example.model.UiState
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@Composable
fun ReviewAndStoriesScreen(viewModel: PandaViewModel) {
    val srsState by viewModel.srsCardsState.collectAsStateWithLifecycle()
    val stories by viewModel.allStories.collectAsStateWithLifecycle()
    val isGeneratingStory by viewModel.isGeneratingStory.collectAsStateWithLifecycle()
    val isGeneratingMnemonic by viewModel.isGeneratingMnemonic.collectAsStateWithLifecycle()

    var showSrs by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab Switcher: SM-2 Review vs Graded Stories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = showSrs,
                onClick = { showSrs = true },
                label = { Text("SM-2 Review") },
                leadingIcon = { Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_sm2_review")
            )
            FilterChip(
                selected = !showSrs,
                onClick = { showSrs = false },
                label = { Text("Graded Stories") },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_graded_stories")
            )
        }

        if (showSrs) {
            Sm2ReviewPane(
                srsState = srsState,
                isGeneratingMnemonic = isGeneratingMnemonic,
                onSpeak = { viewModel.speakChinese(it) },
                onRate = { card, q -> viewModel.submitSrsReview(card, q) },
                onRequestMnemonic = { card -> viewModel.requestMnemonicForCard(card) }
            )
        } else {
            GradedStoriesPane(
                stories = stories,
                isGeneratingStory = isGeneratingStory,
                onSpeak = { viewModel.speakChinese(it) },
                onQuizPass = { viewModel.awardXp(20L, null, "Story quiz mastered!") },
                onGenerateStory = { level, topic, onDone ->
                    viewModel.generateAndSaveAiStory(level, topic, onDone)
                }
            )
        }
    }
}

@Composable
private fun Sm2ReviewPane(
    srsState: UiState<List<SrsCardDoc>>,
    isGeneratingMnemonic: Boolean,
    onSpeak: (String) -> Unit,
    onRate: (SrsCardDoc, Int) -> Unit,
    onRequestMnemonic: (SrsCardDoc) -> Unit
) {
    var flipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (srsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JadePrimary)
                }
            }
            is UiState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = srsState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val now = System.currentTimeMillis()
                val allCards = srsState.data
                val dueCards = allCards.filter { it.nextReviewEpochMs <= now }
                    .sortedBy { it.nextReviewEpochMs }
                val activeDeck = dueCards.ifEmpty { allCards.sortedBy { it.nextReviewEpochMs } }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SuperMemo SM-2 Deck", style = MaterialTheme.typography.titleSmall, color = IndigoAccent)
                            Text(
                                text = "${dueCards.size} due now · ${allCards.size} total saved cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Surface(shape = RoundedCornerShape(50), color = IndigoAccent) {
                            Text(
                                text = "${dueCards.size} DUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val card = activeDeck.firstOrNull()
                if (card == null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Your SM-2 Deck is Ready!", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Tap '+ SM-2' on any word in HSK Topics to add flashcards to your cloud review queue.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = BorderStroke(2.dp, JadePrimary.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HskLevelPill(level = card.hskLevel.toInt())
                                Text(
                                    text = "Reps: ${card.repetitions} · Ease: ${"%.2f".format(card.easeFactor)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { onSpeak(card.hanzi) }) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Speak card", tint = JadePrimary)
                                }
                            }

                            Text(
                                text = card.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = card.pinyin,
                                style = MaterialTheme.typography.titleLarge,
                                color = JadePrimary
                            )

                            if (flipped) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = card.english,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        if (card.exampleHanzi.isNotBlank()) {
                                            Text(
                                                text = card.exampleHanzi,
                                                fontFamily = HanziSerifFontFamily,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                // 3-Strike AI Mnemonic Rescue (or manual unlock)
                                if (!card.mnemonicHint.isNullOrBlank()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                                Text("AI Mnemonic Rescue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                            Text(
                                                text = card.mnemonicHint,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onRequestMnemonic(card) },
                                        enabled = !isGeneratingMnemonic,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        if (isGeneratingMnemonic) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Creating AI Mnemonic...")
                                        } else {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("AI Mnemonic Hook")
                                        }
                                    }
                                }

                                Text("Rate recall quality (SM-2):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                // 6 SM-2 Rating Buttons (0..5 in 2 rows of 3)
                                val ratings = listOf(
                                    Triple(0, "0 · Blackout", CoralVermilion),
                                    Triple(1, "1 · Wrong", CoralVermilion.copy(alpha = 0.85f)),
                                    Triple(2, "2 · Hard", GoldAccent),
                                    Triple(3, "3 · Good", JadePrimary.copy(alpha = 0.85f)),
                                    Triple(4, "4 · Easy", JadePrimary),
                                    Triple(5, "5 · Perfect", JadeDeep)
                                )
                                ratings.chunked(3).forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        row.forEach { (q, lbl, col) ->
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        flipped = false
                                                        onRate(card, q)
                                                    }
                                                    .testTag("srs_rate_$q"),
                                                shape = RoundedCornerShape(12.dp),
                                                color = col.copy(alpha = 0.14f),
                                                border = BorderStroke(1.dp, col.copy(alpha = 0.6f))
                                            ) {
                                                Text(
                                                    text = lbl,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = col,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                PandaActionButton(
                                    text = "Show Answer / Flip Card",
                                    onClick = {
                                        flipped = true
                                        onSpeak(card.hanzi)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("srs_flip_button"),
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradedStoriesPane(
    stories: List<GradedStory>,
    isGeneratingStory: Boolean,
    onSpeak: (String) -> Unit,
    onQuizPass: () -> Unit,
    onGenerateStory: (Int, String, (GradedStory) -> Unit) -> Unit
) {
    var selectedStoryId by remember { mutableStateOf(stories.firstOrNull()?.id ?: "") }
    val activeStory = stories.firstOrNull { it.id == selectedStoryId } ?: stories.first()

    var showPinyin by remember { mutableStateOf(true) }
    var showEnglish by remember { mutableStateOf(true) }
    var selectedQuizOpt by remember(activeStory.id) { mutableStateOf<Int?>(null) }
    var showStoryGenDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Story Generator Button + Story Selector Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PandaActionButton(
                    text = "Generate Custom AI Graded Story",
                    onClick = { showStoryGenDialog = true },
                    containerColor = GoldBright,
                    contentColor = Color(0xFF2B1C00),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_ai_story_button"),
                    leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stories.forEach { st ->
                        val selected = st.id == activeStory.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedStoryId = st.id },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = "HSK ${st.hskLevel}${if (st.aiGenerated) " · AI" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) GoldBright else JadePrimary
                                )
                                Text(
                                    text = st.titleZh,
                                    fontFamily = HanziSerifFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Story Reader Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HskLevelPill(level = activeStory.hskLevel)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = showPinyin,
                                onClick = { showPinyin = !showPinyin },
                                label = { Text("Pinyin") }
                            )
                            FilterChip(
                                selected = showEnglish,
                                onClick = { showEnglish = !showEnglish },
                                label = { Text("English") }
                            )
                        }
                    }

                    Text(
                        text = activeStory.titleZh,
                        fontFamily = HanziSerifFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = JadeDeep
                    )
                    Text(
                        text = activeStory.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    activeStory.sentences.forEach { sentence ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSpeak(sentence.zh) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = sentence.zh,
                                        fontFamily = HanziSerifFontFamily,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (showPinyin) {
                                        Text(
                                            text = sentence.pinyin,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = JadePrimary
                                        )
                                    }
                                    if (showEnglish) {
                                        Text(
                                            text = sentence.en,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Listen sentence",
                                    tint = JadePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Comprehension Quiz
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Comprehension Check", style = MaterialTheme.typography.titleSmall, color = JadeDeep)
                            Text(activeStory.quizQuestion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            activeStory.quizOptions.forEachIndexed { idx, opt ->
                                val answered = selectedQuizOpt != null
                                val isRight = idx == activeStory.quizCorrectIndex
                                val isChosen = selectedQuizOpt == idx
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = !answered) {
                                            selectedQuizOpt = idx
                                            if (isRight) onQuizPass()
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        answered && isRight -> JadePrimary.copy(alpha = 0.18f)
                                        answered && isChosen && !isRight -> CoralVermilion.copy(alpha = 0.18f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Text(
                                        text = "${('A' + idx)}. $opt",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStoryGenDialog) {
        var chosenLevel by remember { mutableIntStateOf(3) }
        var chosenTopicIdx by remember { mutableIntStateOf(4) }

        AlertDialog(
            onDismissRequest = { if (!isGeneratingStory) showStoryGenDialog = false },
            icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = JadePrimary) },
            title = { Text("Generate AI Graded Story") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pick an HSK 3.0 level and topic for Panda Laoshi to write a custom story:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..9).forEach { lvl ->
                            FilterChip(
                                selected = chosenLevel == lvl,
                                onClick = { chosenLevel = lvl },
                                label = { Text("HSK $lvl") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HskCurriculumData.topics.forEachIndexed { idx, t ->
                            FilterChip(
                                selected = chosenTopicIdx == idx,
                                onClick = { chosenTopicIdx = idx },
                                label = { Text(t.nameEn) }
                            )
                        }
                    }
                    if (isGeneratingStory) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Writing your HSK $chosenLevel story...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isGeneratingStory,
                    onClick = {
                        val topic = HskCurriculumData.topics[chosenTopicIdx].nameEn
                        onGenerateStory(chosenLevel, topic) { created ->
                            selectedStoryId = created.id
                            showStoryGenDialog = false
                        }
                    }
                ) { Text("Create Story") }
            },
            dismissButton = {
                TextButton(enabled = !isGeneratingStory, onClick = { showStoryGenDialog = false }) { Text("Cancel") }
            }
        )
    }
}
