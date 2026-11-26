package com.example.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HskCurriculumData
import com.example.data.MandarinDrillsData
import com.example.model.UiState
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

private enum class SentenceStudioMode(val label: String) {
    TILE_BUILDER("Tile Builder"),
    AI_GRADER("AI Grader"),
    TRANSFORMER("Transformer")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceStudioSection(viewModel: PandaViewModel) {
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
    val gradeState by viewModel.sentenceGradeState.collectAsStateWithLifecycle()

    var activeMode by remember { mutableStateOf(SentenceStudioMode.TILE_BUILDER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SVO Chinese Sentence Formula Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(GoldAccent, GoldBright)))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CHINESE SVO FORMULA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF422800)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf("Subject" to "我", "Time" to "明天", "Place" to "在饭馆", "Verb" to "吃", "Object" to "中国菜")
                        .forEach { (role, ex) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.86f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(role, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2B1C00), maxLines = 1)
                                    Text(ex, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A4212), maxLines = 1)
                                }
                            }
                        }
                }
            }
        }

        // 3-Mode Switcher: Tile Builder | AI Grader | Grammar Transformer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SentenceStudioMode.entries.forEach { mode ->
                FilterChip(
                    selected = activeMode == mode,
                    onClick = { activeMode = mode },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sentence_mode_${mode.name.lowercase()}")
                )
            }
        }

        when (activeMode) {
            SentenceStudioMode.TILE_BUILDER -> GuidedTileBuilderCard(viewModel = viewModel)
            SentenceStudioMode.AI_GRADER -> FreeAiSentenceCard(
                viewModel = viewModel,
                allVocab = allVocab,
                gradeState = gradeState
            )
            SentenceStudioMode.TRANSFORMER -> GrammarPatternTransformerCard(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuidedTileBuilderCard(viewModel: PandaViewModel) {
    val sentences = HskCurriculumData.tileSentences
    var index by remember { mutableIntStateOf(0) }
    var showPinyin by remember { mutableStateOf(true) }
    val item = sentences[index]

    val poolIndices = remember(index) {
        mutableStateListOf<Int>().apply {
            addAll(item.tiles.indices.shuffled())
        }
    }
    val placedIndices = remember(index) { mutableStateListOf<Int>() }
    var checkResult by remember(index) { mutableStateOf<Boolean?>(null) }

    val socraticHintState by viewModel.socraticHintState.collectAsStateWithLifecycle()
    var revealAnswer by remember(index) { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HskLevelPill(level = item.hskLevel)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { showPinyin = !showPinyin }) {
                        Text(if (showPinyin) "Pinyin: ON" else "Pinyin: OFF")
                    }
                    TextButton(onClick = {
                        index = (index + 1) % sentences.size
                        viewModel.clearSocraticHint()
                    }) {
                        Text("Next →")
                    }
                }
            }

            Text(
                text = "\"${item.promptEn}\"",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Drop Zone
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (placedIndices.isEmpty()) {
                        Text(
                            text = "Tap word tiles below in SVO order...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            placedIndices.forEach { tileIdx ->
                                val (zh, py) = item.tiles[tileIdx]
                                TileChip(zh = zh, pinyin = if (showPinyin) py else null, active = true) {
                                    placedIndices.remove(tileIdx)
                                    poolIndices.add(tileIdx)
                                    checkResult = null
                                }
                            }
                        }
                    }
                }
            }

            // Word Bank Pool
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                poolIndices.forEach { tileIdx ->
                    val (zh, py) = item.tiles[tileIdx]
                    TileChip(zh = zh, pinyin = if (showPinyin) py else null, active = false) {
                        poolIndices.remove(tileIdx)
                        placedIndices.add(tileIdx)
                        checkResult = null
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        poolIndices.clear()
                        poolIndices.addAll(item.tiles.indices.shuffled())
                        placedIndices.clear()
                        checkResult = null
                        revealAnswer = false
                        viewModel.clearSocraticHint()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Reset")
                }

                PandaActionButton(
                    text = "Check Order",
                    onClick = {
                        val constructed = placedIndices.joinToString("") { item.tiles[it].first }
                        val ok = constructed == item.correctHanzi
                        checkResult = ok
                        if (ok) {
                            viewModel.clearSocraticHint()
                            viewModel.awardXp(20L, null, "Perfect SVO sentence!")
                            viewModel.speakChinese(item.correctHanzi)
                        } else if (constructed.isNotBlank()) {
                            // Automatically ask Gemini Socratic Mistake Explainer for a 1-sentence hint!
                            viewModel.requestSocraticHint(
                                promptEn = item.promptEn,
                                userOrderHanzi = constructed,
                                expectedHanzi = item.correctHanzi
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("check_tile_sentence_button")
                )
            }

            if (checkResult == true) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Perfect! ${item.correctHanzi}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = item.grammarNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (checkResult == false) {
                // Socratic Mistake Explainer Card (guides retry without spoiling answer immediately)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("socratic_hint_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Socratic Mistake Hint",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoAccent
                                )
                            }
                            TextButton(onClick = { revealAnswer = !revealAnswer }) {
                                Text(if (revealAnswer) "Hide Answer" else "Show Answer")
                            }
                        }

                        when (val hintState = socraticHintState) {
                            is UiState.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = IndigoAccent)
                                    Text("Analyzing your word order...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            is UiState.Success -> {
                                Text(
                                    text = hintState.data,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            is UiState.Error -> {
                                Text(
                                    text = item.grammarNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            null -> Unit
                        }

                        if (revealAnswer) {
                            Text(
                                text = "Target order: ${item.correctHanzi}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = JadePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileChip(
    zh: String,
    pinyin: String?,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (active) JadePrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (active) JadePrimary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = zh,
                fontFamily = HanziSerifFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (!pinyin.isNullOrBlank()) {
                Text(
                    text = pinyin,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FreeAiSentenceCard(
    viewModel: PandaViewModel,
    allVocab: List<com.example.model.VocabWord>,
    gradeState: UiState<com.example.model.AiSentenceGrade>?
) {
    val context = LocalContext.current
    var targetIdx by remember { mutableIntStateOf(allVocab.indexOfFirst { it.hanzi == "扫码" }.coerceAtLeast(0)) }
    val targetWord = allVocab.getOrElse(targetIdx) { allVocab.first() }
    var userSentence by remember { mutableStateOf("") }

    val dictationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.audioManager.startChineseListening(
                onResult = { heard -> userSentence = heard },
                onError = { msg -> viewModel.showMessage(msg) }
            )
        }
    }

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
                Text("Free Creation + AI Grading", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {
                    targetIdx = (0 until allVocab.size).random()
                    viewModel.clearSentenceGrade()
                }) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }

            // Target Word Highlight
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TARGET WORD · HSK ${targetWord.hskLevel}", style = MaterialTheme.typography.labelSmall, color = JadePrimary)
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(targetWord.hanzi, fontFamily = HanziSerifFontFamily, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text(targetWord.pinyin, style = MaterialTheme.typography.titleSmall, color = JadePrimary)
                        }
                        Text(targetWord.english, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.speakChinese(targetWord.hanzi) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen target word", tint = JadePrimary)
                    }
                }
            }

            OutlinedTextField(
                value = userSentence,
                onValueChange = { userSentence = it },
                label = { Text("Write or dictate a Chinese sentence") },
                placeholder = { Text("例如：我用手机扫码付款。") },
                trailingIcon = {
                    IconButton(onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            viewModel.audioManager.startChineseListening(
                                onResult = { heard -> userSentence = heard },
                                onError = { msg -> viewModel.showMessage(msg) }
                            )
                        } else {
                            dictationPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Dictate Chinese")
                    }
                },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("free_sentence_input")
            )

            PandaActionButton(
                text = "Grade with Panda Laoshi",
                onClick = { viewModel.gradeFreeSentence(targetWord, userSentence) },
                enabled = gradeState !is UiState.Loading && userSentence.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grade_sentence_button"),
                leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )

            when (gradeState) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JadePrimary)
                        Spacer(Modifier.width(10.dp))
                        Text("Panda Laoshi is grading your grammar...", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = gradeState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    val g = gradeState.data
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.5.dp, if (g.score >= 70) JadePrimary else CoralVermilion)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${g.score}/100 · ${g.verdict}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (g.score >= 70) JadePrimary else CoralVermilion
                                )
                                IconButton(onClick = { viewModel.speakChinese(g.correctedHanzi) }) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen natural version", tint = JadePrimary)
                                }
                            }
                            Text(
                                text = g.correctedHanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (g.correctedPinyin.isNotBlank()) {
                                Text(g.correctedPinyin, style = MaterialTheme.typography.bodySmall, color = JadePrimary)
                            }
                            Text("Grammar: ${g.grammarAnalysis}", style = MaterialTheme.typography.bodySmall)
                            Text("Native Tip: ${g.nativeTip}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                null -> Unit
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrammarPatternTransformerCard(viewModel: PandaViewModel) {
    val transformState by viewModel.grammarTransformState.collectAsStateWithLifecycle()
    var baseSentence by remember { mutableStateOf(MandarinDrillsData.grammarTransformStarterSentences.first().first) }
    var activePatternLabel by remember { mutableStateOf("") }

    val transformations = remember {
        listOf(
            Triple("Past · 了", "Convert to completed past tense using 了 (le).", JadePrimary),
            Triple("Negative · 没/不", "Convert to natural Mandarin negative using 没 (méi) or 不 (bù).", CoralVermilion),
            Triple("Question · 吗", "Convert into a polite yes/no question using 吗 (ma).", IndigoAccent),
            Triple("Time Before Verb", "Add a time phrase like 明天 or 昨天 in proper Mandarin Subject + Time + Verb order.", GoldAccent),
            Triple("Place Before Verb", "Add a location phrase using 在 (zài) + Place BEFORE the verb.", JadePrimary),
            Triple("把 Structure", "Rewrite using the HSK 把 (bǎ) disposal structure: Subject + 把 + Object + Verb + Result.", IndigoAccent)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grammar_transformer_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, IndigoAccent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = IndigoAccent)
                Column {
                    Text(
                        text = "HSK Grammar Pattern Transformer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pick or type a base sentence and tap a grammar transformation — Gemini shows what moved and why.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Starter Base Sentences
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MandarinDrillsData.grammarTransformStarterSentences.forEach { (zh, en) ->
                    FilterChip(
                        selected = baseSentence == zh,
                        onClick = {
                            baseSentence = zh
                            viewModel.clearGrammarTransform()
                        },
                        label = { Text("$zh ($en)", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = baseSentence,
                onValueChange = { baseSentence = it },
                label = { Text("Base Chinese sentence (e.g. 我吃苹果)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { if (baseSentence.isNotBlank()) viewModel.speakChinese(baseSentence) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen base sentence", tint = JadePrimary)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grammar_transformer_input")
            )

            Text(
                text = "Tap a transformation:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 6 Transformation Action Buttons (2 rows of 3)
            transformations.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (label, instruction, accent) ->
                        val isActive = activePatternLabel == label
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = transformState !is UiState.Loading && baseSentence.isNotBlank()) {
                                    activePatternLabel = label
                                    viewModel.transformGrammarPattern(
                                        baseSentence = baseSentence,
                                        label = label,
                                        instruction = instruction
                                    )
                                }
                                .testTag("transform_btn_${label.take(4).lowercase()}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isActive) accent else accent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color.White else accent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            when (val st = transformState) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = IndigoAccent)
                        Spacer(Modifier.width(10.dp))
                        Text("Transforming \"$baseSentence\" into $activePatternLabel...", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = st.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    val result = st.data
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grammar_transformer_result"),
                        shape = RoundedCornerShape(18.dp),
                        color = JadeLightContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.5.dp, JadePrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.transformationLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JadePrimary
                                )
                                IconButton(onClick = { viewModel.speakChinese(result.transformedHanzi) }) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen transformed sentence", tint = JadePrimary)
                                }
                            }

                            ToneColoredHanziPinyinBlock(
                                hanzi = result.transformedHanzi,
                                pinyin = result.transformedPinyin,
                                hanziFontSize = 24.sp
                            )

                            Text(
                                text = "\"${result.transformedEnglish}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (result.highlightedChanges.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    result.highlightedChanges.forEach { change ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = IndigoAccent.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = change,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = IndigoAccent,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = result.grammarExplanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            ToneColorLegendRow()
                        }
                    }
                }
                null -> Unit
            }
        }
    }
}

