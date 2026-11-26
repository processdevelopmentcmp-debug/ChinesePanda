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
import com.example.model.UiState
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceStudioSection(viewModel: PandaViewModel) {
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
    val gradeState by viewModel.sentenceGradeState.collectAsStateWithLifecycle()

    var modeA by remember { mutableStateOf(true) }

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

        // Mode A vs Mode B Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = modeA,
                onClick = { modeA = true },
                label = { Text("Mode A · Tile Builder") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !modeA,
                onClick = { modeA = false },
                label = { Text("Mode B · AI Grader") },
                modifier = Modifier.weight(1f)
            )
        }

        if (modeA) {
            GuidedTileBuilderCard(viewModel = viewModel)
        } else {
            FreeAiSentenceCard(
                viewModel = viewModel,
                allVocab = allVocab,
                gradeState = gradeState
            )
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
                    TextButton(onClick = { index = (index + 1) % sentences.size }) {
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
                            viewModel.awardXp(20L, null, "Perfect SVO sentence!")
                            viewModel.speakChinese(item.correctHanzi)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("check_tile_sentence_button")
                )
            }

            if (checkResult != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (checkResult == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (checkResult == true) "Perfect! ${item.correctHanzi}" else "Expected: ${item.correctHanzi}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (checkResult == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = item.grammarNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (checkResult == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
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
