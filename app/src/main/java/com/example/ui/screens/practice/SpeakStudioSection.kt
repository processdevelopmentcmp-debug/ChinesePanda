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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.ActivePlaybackSource
import com.example.audio.PitchAnalysisEngine
import com.example.audio.PitchRecorderController
import com.example.audio.TargetSyllableSpec
import com.example.data.HskCurriculumData
import com.example.data.MandarinDrillsData
import com.example.model.UiState
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.PitchAnalysisReportCard
import com.example.ui.components.PitchComparisonCanvasCard
import com.example.ui.components.SandhiToneTileCard
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.Tone1Red
import com.example.ui.theme.Tone2Green
import com.example.ui.theme.Tone3Blue
import com.example.ui.theme.Tone4Purple
import com.example.viewmodel.PandaViewModel

private enum class SpeakSourceMode(val label: String) {
    SANDHI("Tone Sandhi"),
    TONE_PAIRS("20 Tone Pairs"),
    HSK_VOCAB("HSK Words"),
    AI_SHADOWING("AI Shadowing")
}

private enum class PendingMicAction {
    NONE,
    PITCH_RECORD,
    SPEECH_RECOGNIZER
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakStudioSection(viewModel: PandaViewModel) {
    val context = LocalContext.current
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
    val isListening by viewModel.audioManager.isListening.collectAsStateWithLifecycle()
    val speechStatus by viewModel.audioManager.speechStatus.collectAsStateWithLifecycle()

    val pitchController = remember(context, viewModel.audioManager) {
        PitchRecorderController(context.applicationContext, viewModel.audioManager)
    }
    DisposableEffect(pitchController) {
        onDispose { pitchController.release() }
    }
    val pitchState by pitchController.state.collectAsStateWithLifecycle()

    var sourceMode by remember { mutableStateOf(SpeakSourceMode.SANDHI) }
    var selectedSandhiIdx by remember { mutableIntStateOf(0) }
    var selectedTonePairIdx by remember { mutableIntStateOf(5) } // Start on 2+1 shíjiān 时间
    var selectedVocabIdx by remember { mutableIntStateOf(0) }
    var selectedShadowingIdx by remember { mutableIntStateOf(0) }
    var nativeSpeed by remember { mutableFloatStateOf(0.85f) }
    var pendingMicAction by remember { mutableStateOf(PendingMicAction.NONE) }

    val hskPracticeWords = remember(allVocab) {
        allVocab.filter { it.hanzi.length in 1..4 && it.pinyin.isNotBlank() }.take(24)
            .ifEmpty { HskCurriculumData.builtInVocab.take(16) }
    }

    val activeSandhi = HskCurriculumData.sandhiPhrases[selectedSandhiIdx.coerceIn(0, HskCurriculumData.sandhiPhrases.lastIndex)]
    val activeTonePair = MandarinDrillsData.twentyTonePairs[selectedTonePairIdx.coerceIn(0, MandarinDrillsData.twentyTonePairs.lastIndex)]
    val activeVocab = hskPracticeWords[selectedVocabIdx.coerceIn(0, hskPracticeWords.lastIndex)]
    val activeShadowing = MandarinDrillsData.shadowingScripts[selectedShadowingIdx.coerceIn(0, MandarinDrillsData.shadowingScripts.lastIndex)]
    val shadowingGradeState by viewModel.shadowingGradeState.collectAsStateWithLifecycle()

    val activeHanzi = when (sourceMode) {
        SpeakSourceMode.SANDHI -> activeSandhi.hanzi
        SpeakSourceMode.TONE_PAIRS -> activeTonePair.hanzi
        SpeakSourceMode.HSK_VOCAB -> activeVocab.hanzi
        SpeakSourceMode.AI_SHADOWING -> activeShadowing.hanzi
    }
    val activePinyin = when (sourceMode) {
        SpeakSourceMode.SANDHI -> activeSandhi.tiles.joinToString(" ") { it.spokenPinyin }
        SpeakSourceMode.TONE_PAIRS -> activeTonePair.pinyin
        SpeakSourceMode.HSK_VOCAB -> activeVocab.pinyin
        SpeakSourceMode.AI_SHADOWING -> activeShadowing.pinyin
    }
    val activeEnglish = when (sourceMode) {
        SpeakSourceMode.SANDHI -> activeSandhi.translationEn
        SpeakSourceMode.TONE_PAIRS -> activeTonePair.english
        SpeakSourceMode.HSK_VOCAB -> activeVocab.english
        SpeakSourceMode.AI_SHADOWING -> activeShadowing.english
    }
    val activeHskLevel = when (sourceMode) {
        SpeakSourceMode.SANDHI -> activeSandhi.hskLevel
        SpeakSourceMode.TONE_PAIRS -> 1
        SpeakSourceMode.HSK_VOCAB -> activeVocab.hskLevel
        SpeakSourceMode.AI_SHADOWING -> activeShadowing.hskLevel
    }

    val activeSyllables = remember(sourceMode, selectedSandhiIdx, selectedTonePairIdx, selectedVocabIdx, activeHanzi, activePinyin) {
        when (sourceMode) {
            SpeakSourceMode.SANDHI -> activeSandhi.tiles.map { tile ->
                TargetSyllableSpec(
                    hanzi = tile.char,
                    pinyin = tile.spokenPinyin,
                    expectedTone = tile.spokenTone.coerceIn(1, 5)
                )
            }
            SpeakSourceMode.TONE_PAIRS -> {
                val pyParts = activeTonePair.pinyin.trim().split(Regex("\\s+"))
                val chars = activeTonePair.hanzi.map { it.toString() }
                listOf(
                    TargetSyllableSpec(
                        hanzi = chars.getOrElse(0) { activeTonePair.hanzi },
                        pinyin = pyParts.getOrElse(0) { activeTonePair.pinyin },
                        expectedTone = activeTonePair.tone1.coerceIn(1, 5)
                    ),
                    TargetSyllableSpec(
                        hanzi = chars.getOrElse(1) { "" },
                        pinyin = pyParts.getOrElse(1) { "" },
                        expectedTone = activeTonePair.tone2.coerceIn(1, 5)
                    )
                )
            }
            else -> PitchAnalysisEngine.parseSyllablesFromWord(activeHanzi, activePinyin)
        }
    }

    var lastTranscript by remember(activeHanzi) { mutableStateOf<String?>(null) }
    var matchScore by remember(activeHanzi) { mutableStateOf<Int?>(null) }
    var speechError by remember(activeHanzi) { mutableStateOf<String?>(null) }

    // Prepare native speaker reference contour whenever target phrase or playback speed changes
    LaunchedEffect(sourceMode, activeHanzi, activeSyllables, nativeSpeed) {
        if (sourceMode != SpeakSourceMode.AI_SHADOWING) {
            pitchController.prepareNativeReference(
                hanzi = activeHanzi,
                syllables = activeSyllables,
                speed = nativeSpeed
            )
        }
    }

    fun startPitchRecording() {
        speechError = null
        pitchController.clearError()
        pitchController.startRecording(
            maxDurationMs = (activeSyllables.size * 1100L + 1800L).coerceIn(3200L, 6500L),
            onCompleted = { report ->
                if (report.overallScore > 0) {
                    viewModel.recordPronunciationPracticeComplete(
                        hanzi = activeHanzi,
                        pitchScore = report.overallScore
                    )
                }
            }
        )
    }

    fun startSpeechToTextCheck() {
        speechError = null
        pitchController.stopPlayback()
        if (pitchState.isRecording) pitchController.stopRecordingAndAnalyze()
        viewModel.audioManager.startChineseListening(
            onResult = { heard ->
                lastTranscript = heard
                if (sourceMode == SpeakSourceMode.AI_SHADOWING) {
                    viewModel.gradeShadowingDialogue(
                        targetScript = activeShadowing.hanzi,
                        targetPinyin = activeShadowing.pinyin,
                        recognizedSpeech = heard,
                        hskLevel = activeShadowing.hskLevel
                    )
                } else {
                    val score = computeHanziMatchScore(activeHanzi, heard)
                    matchScore = score
                    if (score >= 70) {
                        viewModel.awardXp(20L, null, "Mandarin speech recognition $score%")
                    }
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
        val action = pendingMicAction
        pendingMicAction = PendingMicAction.NONE
        if (granted) {
            when (action) {
                PendingMicAction.PITCH_RECORD -> startPitchRecording()
                PendingMicAction.SPEECH_RECOGNIZER -> startSpeechToTextCheck()
                PendingMicAction.NONE -> Unit
            }
        } else {
            speechError = "Microphone permission is required to record your voice and analyze Mandarin pitch."
        }
    }

    fun runWithMicPermission(action: PendingMicAction) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            when (action) {
                PendingMicAction.PITCH_RECORD -> startPitchRecording()
                PendingMicAction.SPEECH_RECOGNIZER -> startSpeechToTextCheck()
                PendingMicAction.NONE -> Unit
            }
        } else {
            pendingMicAction = action
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontally Scrollable Mode Switcher + Native Speed Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpeakSourceMode.entries.forEach { mode ->
                val selected = sourceMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { sourceMode = mode },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JadePrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("speak_source_${mode.name.lowercase()}")
                )
            }
            OutlinedButton(
                onClick = { nativeSpeed = if (nativeSpeed < 0.9f) 1.0f else 0.75f },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("speak_speed_toggle")
            ) {
                Icon(Icons.Rounded.Speed, contentDescription = "Toggle native speed", modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (nativeSpeed < 0.9f) "0.75×" else "1.0×",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // VOICE-TO-TEXT AI SHADOWING GRADER MODE
        if (sourceMode == SpeakSourceMode.AI_SHADOWING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MandarinDrillsData.shadowingScripts.forEachIndexed { idx, script ->
                    val selected = idx == selectedShadowingIdx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedShadowingIdx = idx
                                viewModel.clearShadowingGrade()
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) IndigoAccent else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) IndigoAccent else MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                            Text(
                                text = "HSK ${script.hskLevel} · ${script.scenario}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.85f) else IndigoAccent
                            )
                            Text(
                                text = script.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shadowing_dialogue_card"),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, IndigoAccent.copy(alpha = 0.6f))
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
                        HskLevelPill(level = activeShadowing.hskLevel)
                        Text(
                            text = "Voice-to-Text Shadowing Grader",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoAccent
                        )
                    }

                    ToneColoredHanziPinyinBlock(
                        hanzi = activeShadowing.hanzi,
                        pinyin = activeShadowing.pinyin,
                        hanziFontSize = 24.sp
                    )

                    Text(
                        text = "\"${activeShadowing.english}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ToneColorLegendRow()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PandaActionButton(
                            text = "Listen Native",
                            onClick = { viewModel.speakChinese(activeShadowing.hanzi) },
                            containerColor = JadePrimary,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                        PandaActionButton(
                            text = if (isListening) "Listening..." else "Shadow & Grade",
                            onClick = {
                                if (isListening) {
                                    viewModel.audioManager.stopListening()
                                } else {
                                    runWithMicPermission(PendingMicAction.SPEECH_RECOGNIZER)
                                }
                            },
                            containerColor = if (isListening) MaterialTheme.colorScheme.onSurface else IndigoAccent,
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("shadowing_record_button"),
                            leadingIcon = {
                                Icon(
                                    if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (!speechStatus.isNullOrBlank() && isListening) {
                        Text(
                            text = speechStatus!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = IndigoAccent
                        )
                    }

                    when (val grade = shadowingGradeState) {
                        is UiState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = IndigoAccent)
                                Spacer(Modifier.width(10.dp))
                                Text("Gemini is comparing your pronunciation to the script...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is UiState.Error -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = grade.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        is UiState.Success -> {
                            val res = grade.data
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("shadowing_grade_result_card"),
                                shape = RoundedCornerShape(18.dp),
                                color = if (res.fluencyScore >= 75) JadeLightContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(1.5.dp, if (res.fluencyScore >= 75) JadePrimary else CoralVermilion)
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
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = JadePrimary, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "Shadowing Score: ${res.fluencyScore}/100",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (res.fluencyScore >= 75) JadePrimary else CoralVermilion
                                            )
                                        }
                                        Text(
                                            text = res.verdict,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "Heard: \"${res.recognizedSpeech}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (res.flaggedWords.isNotEmpty()) {
                                        Text(
                                            text = "Words / Tones to Polish:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralVermilion
                                        )
                                        res.flaggedWords.forEach { flag ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { viewModel.speakChinese(flag.expectedWord) },
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = CoralVermilion, modifier = Modifier.size(18.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${flag.expectedWord} (${flag.expectedPinyin})",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = flag.issue,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Hear word", tint = JadePrimary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Coach Tip: ${res.coachingTip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        null -> Unit
                    }

                    if (speechError != null) {
                        Text(
                            text = speechError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            return
        }

        // Tone Legend Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToneLegendDot("T1 High", Tone1Red)
                ToneLegendDot("T2 Rise", Tone2Green)
                ToneLegendDot("T3 Dip", Tone3Blue)
                ToneLegendDot("T4 Fall", Tone4Purple)
            }
        }

        // Phrase / Tone-Pair / Word Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (sourceMode) {
                SpeakSourceMode.SANDHI -> {
                    HskCurriculumData.sandhiPhrases.forEachIndexed { idx, p ->
                        val selected = idx == selectedSandhiIdx
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedSandhiIdx = idx },
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
                SpeakSourceMode.TONE_PAIRS -> {
                    MandarinDrillsData.twentyTonePairs.forEachIndexed { idx, tp ->
                        val selected = idx == selectedTonePairIdx
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTonePairIdx = idx }
                                .testTag("tone_pair_pill_${tp.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) IndigoAccent else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) IndigoAccent else MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "T${tp.tone1}+T${tp.tone2}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) Color.White.copy(alpha = 0.9f) else IndigoAccent
                                )
                                Text(
                                    text = tp.hanzi,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                else -> {
                    hskPracticeWords.forEachIndexed { idx, w ->
                        val selected = idx == selectedVocabIdx
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedVocabIdx = idx },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = w.hanzi,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = w.pinyin,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Target Word / Tone Pair / Sandhi Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HskLevelPill(level = activeHskLevel)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = when (sourceMode) {
                                SpeakSourceMode.SANDHI -> activeSandhi.ruleTitle
                                SpeakSourceMode.TONE_PAIRS -> "Tone Pair ${activeTonePair.tone1} + ${activeTonePair.tone2} (of 20)"
                                else -> "HSK 3.0 Pronunciation"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (sourceMode == SpeakSourceMode.SANDHI) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeSandhi.tiles.forEach { tile ->
                            SandhiToneTileCard(tile = tile)
                        }
                    }
                } else {
                    ToneColoredHanziPinyinBlock(
                        hanzi = activeHanzi,
                        pinyin = activePinyin,
                        hanziFontSize = 36.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Text(
                    text = "\"$activeEnglish\"",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (sourceMode == SpeakSourceMode.SANDHI) {
                    Text(
                        text = activeSandhi.ruleDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else if (sourceMode == SpeakSourceMode.TONE_PAIRS) {
                    Text(
                        text = activeTonePair.rhythmTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // REAL-TIME PITCH CONTOUR COMPARISON CANVAS
        PitchComparisonCanvasCard(
            uiState = pitchState,
            syllables = activeSyllables
        )

        // Primary Microphone & Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PandaActionButton(
                text = if (pitchState.activePlayback == ActivePlaybackSource.NATIVE) "Playing..." else "Native Audio",
                onClick = { pitchController.playNativeReference(activeHanzi, nativeSpeed) },
                containerColor = JadePrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("speak_play_native_button"),
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Play native pronunciation",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            PandaActionButton(
                text = if (pitchState.isRecording) "Stop & Score" else "Record Pitch",
                onClick = {
                    if (pitchState.isRecording) {
                        pitchController.stopRecordingAndAnalyze()
                    } else {
                        runWithMicPermission(PendingMicAction.PITCH_RECORD)
                    }
                },
                containerColor = if (pitchState.isRecording) MaterialTheme.colorScheme.onSurface else CoralVermilion,
                modifier = Modifier
                    .weight(1f)
                    .testTag("speak_record_button"),
                leadingIcon = {
                    Icon(
                        if (pitchState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (pitchState.isRecording) "Stop recording" else "Record pronunciation",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Secondary Actions: Replay My Recording + Speech-to-Text Verification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (pitchState.activePlayback == ActivePlaybackSource.USER) {
                        pitchController.stopPlayback()
                    } else {
                        pitchController.playUserRecording()
                    }
                },
                enabled = pitchState.hasUserRecording && !pitchState.isRecording,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("speak_replay_user_button")
            ) {
                Icon(
                    imageVector = if (pitchState.activePlayback == ActivePlaybackSource.USER) {
                        Icons.Rounded.Stop
                    } else {
                        Icons.Rounded.PlayArrow
                    },
                    contentDescription = "Replay your recording",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (pitchState.activePlayback == ActivePlaybackSource.USER) "Stop Replay" else "My Voice",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = {
                    if (isListening) {
                        viewModel.audioManager.stopListening()
                    } else {
                        runWithMicPermission(PendingMicAction.SPEECH_RECOGNIZER)
                    }
                },
                enabled = !pitchState.isRecording,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("speak_asr_check_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.RecordVoiceOver,
                    contentDescription = "Speech recognition check",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isListening) "Stop ASR" else "Word Check",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Acoustic Pitch Comparison Report
        pitchState.report?.let { report ->
            PitchAnalysisReportCard(report = report)
        }

        // Live ASR status if running
        if (!speechStatus.isNullOrBlank() && (isListening || lastTranscript != null)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    Icons.Rounded.Hearing,
                    contentDescription = null,
                    tint = JadePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = speechStatus!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Speech-to-Text Evaluation Result
        if (lastTranscript != null && matchScore != null) {
            val passed = matchScore!! >= 70
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (passed) JadeLightContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Speech Recognition: ${matchScore}% · ${if (passed) "Clear Mandarin!" else "Try enunciating clearly"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recognized: $lastTranscript  (Target: $activeHanzi)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val displayError = pitchState.errorMessage ?: speechError
        if (displayError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = displayError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
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
