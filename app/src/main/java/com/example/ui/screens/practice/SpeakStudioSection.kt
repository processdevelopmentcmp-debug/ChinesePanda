package com.example.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HskCurriculumData
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.SandhiToneTileCard
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.Tone1Red
import com.example.ui.theme.Tone2Green
import com.example.ui.theme.Tone3Blue
import com.example.ui.theme.Tone4Purple
import com.example.viewmodel.PandaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakStudioSection(viewModel: PandaViewModel) {
    val context = LocalContext.current
    val isListening by viewModel.audioManager.isListening.collectAsStateWithLifecycle()
    val speechStatus by viewModel.audioManager.speechStatus.collectAsStateWithLifecycle()

    var selectedIdx by remember { mutableIntStateOf(0) }
    val phrase = HskCurriculumData.sandhiPhrases[selectedIdx]

    var lastTranscript by remember(selectedIdx) { mutableStateOf<String?>(null) }
    var matchScore by remember(selectedIdx) { mutableStateOf<Int?>(null) }
    var speechError by remember(selectedIdx) { mutableStateOf<String?>(null) }

    fun beginRecording() {
        speechError = null
        viewModel.audioManager.startChineseListening(
            onResult = { heard ->
                lastTranscript = heard
                val score = computeHanziMatchScore(phrase.hanzi, heard)
                matchScore = score
                if (score >= 70) {
                    viewModel.awardXp(20L, null, "Tone Sandhi pronunciation $score%")
                }
            },
            onError = { err ->
                speechError = err
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            beginRecording()
        } else {
            speechError = "Microphone permission is required for Mandarin pronunciation scoring."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tone Color Legend
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToneLegendDot("T1 High", Tone1Red)
                ToneLegendDot("T2 Rise", Tone2Green)
                ToneLegendDot("T3 Dip", Tone3Blue)
                ToneLegendDot("T4 Fall", Tone4Purple)
            }
        }

        // Phrase Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HskCurriculumData.sandhiPhrases.forEachIndexed { idx, p ->
                val selected = idx == selectedIdx
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedIdx = idx },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = p.hanzi,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Active Sandhi Coach Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HskLevelPill(level = phrase.hskLevel)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = phrase.ruleTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    phrase.tiles.forEach { tile ->
                        SandhiToneTileCard(tile = tile)
                    }
                }

                Text(
                    text = "\"${phrase.translationEn}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = phrase.ruleDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PandaActionButton(
                        text = "Native Audio",
                        onClick = { viewModel.speakChinese(phrase.hanzi, 0.85f) },
                        containerColor = JadePrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speak_play_native_button"),
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )

                    PandaActionButton(
                        text = if (isListening) "Stop" else "Record",
                        onClick = {
                            if (isListening) {
                                viewModel.audioManager.stopListening()
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    beginRecording()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        containerColor = if (isListening) MaterialTheme.colorScheme.onSurface else CoralVermilion,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speak_record_button"),
                        leadingIcon = {
                            Icon(
                                if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                if (!speechStatus.isNullOrBlank()) {
                    Text(
                        text = speechStatus!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Diagnostic Evaluation Result
        if (lastTranscript != null && matchScore != null) {
            val passed = matchScore!! >= 70
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (passed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Pronunciation Score: ${matchScore}% · ${if (passed) "Natural Mandarin!" else "Try matching the Sandhi pitch"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recognized speech: $lastTranscript",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target: ${phrase.hanzi} (${phrase.tiles.joinToString(" ") { it.spokenPinyin }})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (speechError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = speechError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ToneLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            modifier = Modifier.size(9.dp),
            shape = RoundedCornerShape(50),
            color = color
        ) {}
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

private fun computeHanziMatchScore(target: String, heard: String): Int {
    val cleanTarget = target.filter { !it.isWhitespace() && it != '，' && it != '。' }
    val cleanHeard = heard.filter { !it.isWhitespace() && it != '，' && it != '。' }
    if (cleanTarget.isEmpty() || cleanHeard.isEmpty()) return 0
    if (cleanHeard.contains(cleanTarget)) return 100
    var matched = 0
    cleanTarget.forEach { ch ->
        if (cleanHeard.contains(ch)) matched++
    }
    return ((matched.toFloat() / cleanTarget.length) * 100f).toInt().coerceIn(0, 100)
}
