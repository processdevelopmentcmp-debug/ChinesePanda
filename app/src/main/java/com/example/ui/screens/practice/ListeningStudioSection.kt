package com.example.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.MandarinDrillsData
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ListeningStudioSection(viewModel: PandaViewModel) {
    val speed by viewModel.audioSpeed.collectAsStateWithLifecycle()
    val questions = HskCurriculumData.listeningQuestions

    var examMode by remember { mutableStateOf(false) }
    var minimalPairMode by remember { mutableStateOf(false) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var replaysUsed by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(300) }
    val missedWordKeys = remember { mutableStateListOf<String>() }

    // Official Exam Mode Countdown Timer
    LaunchedEffect(examMode, minimalPairMode, finished) {
        if (examMode && !minimalPairMode && !finished) {
            while (secondsLeft > 0 && examMode && !minimalPairMode && !finished) {
                delay(1000L)
                secondsLeft--
            }
            if (secondsLeft == 0) finished = true
        }
    }

    fun resetTest(newExamMode: Boolean) {
        minimalPairMode = false
        examMode = newExamMode
        currentIdx = 0
        selectedOption = null
        replaysUsed = 0
        correctCount = 0
        finished = false
        secondsLeft = 300
        missedWordKeys.clear()
    }

    val q = questions[currentIdx]
    val maxReplays = if (examMode) 2 else Int.MAX_VALUE
    val canPlay = replaysUsed < maxReplays

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode & Speed Settings Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = !examMode && !minimalPairMode,
                        onClick = { resetTest(false) },
                        label = { Text("Practice", style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = examMode && !minimalPairMode,
                        onClick = { resetTest(true) },
                        label = { Text("Exam", style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = minimalPairMode,
                        onClick = { minimalPairMode = true },
                        label = { Text("Minimal Pairs", style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("listening_minimal_pairs_tab")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Speed:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.75f to "0.75x", 1.0f to "1.0x", 1.15f to "1.15x").forEach { (rate, lbl) ->
                            FilterChip(
                                selected = speed == rate,
                                onClick = { viewModel.setAudioSpeed(rate) },
                                label = { Text(lbl) }
                            )
                        }
                    }
                }
            }
        }

        if (minimalPairMode) {
            MinimalPairListeningQuizPane(viewModel = viewModel)
            return
        }

        if (finished) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, JadePrimary)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("HSK 3.0 Listening Complete!", style = MaterialTheme.typography.headlineMedium, color = JadePrimary)
                    Text(
                        text = "Score: $correctCount / ${questions.size} (${(correctCount * 100) / questions.size}%)",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (missedWordKeys.isNotEmpty()) {
                        PandaActionButton(
                            text = "Send ${missedWordKeys.distinct().size} Missed Words to SM-2",
                            onClick = { viewModel.addMissedWordsToSrs(missedWordKeys.toList()) },
                            containerColor = IndigoAccent,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    OutlinedButton(
                        onClick = { resetTest(examMode) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Retake Listening Test")
                    }
                }
            }
            return
        }

        // Active Question Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        HskLevelPill(level = q.hskLevel)
                        Text(
                            text = "Q${currentIdx + 1}/${questions.size}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    if (examMode) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, tint = CoralVermilion, modifier = Modifier.size(16.dp))
                            Text(
                                text = "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                                style = MaterialTheme.typography.labelLarge,
                                color = CoralVermilion
                            )
                        }
                    } else {
                        Text(
                            text = "Unlimited Replays",
                            style = MaterialTheme.typography.labelSmall,
                            color = JadePrimary
                        )
                    }
                }

                Text(
                    text = q.type.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = IndigoAccent
                )

                // Big Audio Play Button
                Surface(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .clickable(enabled = canPlay) {
                            replaysUsed++
                            viewModel.speakChinese(q.audioScript)
                        }
                        .testTag("play_listening_audio_button"),
                    shape = CircleShape,
                    color = if (canPlay) JadePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Play listening audio",
                            tint = if (canPlay) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Text(
                    text = if (examMode) "Replays left: ${(maxReplays - replaysUsed).coerceAtLeast(0)} / $maxReplays" else "Tap speaker to play Mandarin clip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = q.promptText,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                // Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    q.options.forEachIndexed { idx, opt ->
                        val answered = selectedOption != null
                        val isRight = idx == q.correctIndex
                        val isChosen = selectedOption == idx

                        val bg = when {
                            answered && isRight -> JadePrimary.copy(alpha = 0.15f)
                            answered && isChosen && !isRight -> CoralVermilion.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val border = when {
                            answered && isRight -> JadePrimary
                            answered && isChosen && !isRight -> CoralVermilion
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !answered) {
                                    selectedOption = idx
                                    if (isRight) {
                                        correctCount++
                                        viewModel.awardXp(15L)
                                    } else {
                                        missedWordKeys.add(q.targetWordKey)
                                    }
                                }
                                .testTag("listening_option_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            color = bg,
                            border = BorderStroke(if (answered && (isRight || isChosen)) 2.dp else 1.dp, border)
                        ) {
                            Text(
                                text = "${('A' + idx)}. $opt",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Reveal Transcript After Answer
                if (selectedOption != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Transcript:", style = MaterialTheme.typography.labelSmall, color = JadePrimary)
                            Text(q.audioScript, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(q.audioPinyin, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    PandaActionButton(
                        text = if (currentIdx == questions.lastIndex) "See Test Score" else "Next Question",
                        onClick = {
                            if (currentIdx == questions.lastIndex) {
                                finished = true
                            } else {
                                currentIdx++
                                selectedOption = null
                                replaysUsed = 0
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalPairListeningQuizPane(viewModel: PandaViewModel) {
    val pairs = MandarinDrillsData.minimalPairQuizzes
    val coroutineScope = rememberCoroutineScope()

    var currentIdx by remember { mutableIntStateOf(0) }
    var targetIsRight by remember(currentIdx) { mutableStateOf((0..1).random() == 1) }
    var chosenRight by remember(currentIdx) { mutableStateOf<Boolean?>(null) }
    var streak by remember { mutableIntStateOf(0) }
    var totalCorrect by remember { mutableIntStateOf(0) }

    val pair = pairs[currentIdx]
    val targetHanzi = if (targetIsRight) pair.rightHanzi else pair.leftHanzi
    val targetPinyin = if (targetIsRight) pair.rightPinyin else pair.leftPinyin
    val answered = chosenRight != null
    val wasCorrect = chosenRight == targetIsRight

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("minimal_pair_quiz_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, IndigoAccent.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = IndigoAccent.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = pair.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Pair ${currentIdx + 1}/${pairs.size} · Streak $streak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ToneColorLegendRow()

            Text(
                text = "Listen carefully, then tap which Mandarin word you heard!",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )

            // Play Target Word Button
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.speakChinese(targetHanzi) }
                    .testTag("minimal_pair_play_target_button"),
                shape = CircleShape,
                color = IndigoAccent,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Play target word",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.speakChinese(pair.leftHanzi)
                        delay(950L)
                        viewModel.speakChinese(pair.rightHanzi)
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("minimal_pair_compare_both_button")
            ) {
                Icon(Icons.Rounded.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hear Both Back-to-Back")
            }

            // Two Side-by-Side Choice Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(false, true).forEach { isRightOption ->
                    val hanzi = if (isRightOption) pair.rightHanzi else pair.leftHanzi
                    val pinyin = if (isRightOption) pair.rightPinyin else pair.leftPinyin
                    val english = if (isRightOption) pair.rightEnglish else pair.leftEnglish

                    val isCorrectCard = isRightOption == targetIsRight
                    val isSelectedCard = chosenRight == isRightOption

                    val bg = when {
                        answered && isCorrectCard -> JadeLightContainer
                        answered && isSelectedCard && !isCorrectCard -> CoralVermilion.copy(alpha = 0.16f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val border = when {
                        answered && isCorrectCard -> JadePrimary
                        answered && isSelectedCard && !isCorrectCard -> CoralVermilion
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                if (!answered) {
                                    chosenRight = isRightOption
                                    if (isCorrectCard) {
                                        streak++
                                        totalCorrect++
                                        viewModel.awardXp(12L, null, "Minimal pair nailed: $targetHanzi ($targetPinyin)")
                                    } else {
                                        streak = 0
                                    }
                                } else {
                                    viewModel.speakChinese(hanzi)
                                }
                            }
                            .testTag(if (isRightOption) "minimal_pair_option_right" else "minimal_pair_option_left"),
                        shape = RoundedCornerShape(18.dp),
                        color = bg,
                        border = BorderStroke(if (answered && (isCorrectCard || isSelectedCard)) 2.dp else 1.dp, border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ToneColoredHanziPinyinBlock(
                                hanzi = hanzi,
                                pinyin = pinyin,
                                hanziFontSize = 36.sp,
                                showPinyin = true,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = "\"$english\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (answered) {
                                Text(
                                    text = "Tap to hear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoAccent
                                )
                            }
                        }
                    }
                }
            }

            if (answered) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (wasCorrect) JadeLightContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (wasCorrect) JadePrimary else CoralVermilion,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (wasCorrect) {
                                    "Spot on! You heard $targetHanzi ($targetPinyin)"
                                } else {
                                    "Target was $targetHanzi ($targetPinyin)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = pair.distinctionTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                PandaActionButton(
                    text = if (currentIdx == pairs.lastIndex) "Restart Minimal Pairs ($totalCorrect/${pairs.size})" else "Next Minimal Pair",
                    onClick = {
                        if (currentIdx == pairs.lastIndex) {
                            currentIdx = 0
                            totalCorrect = 0
                        } else {
                            currentIdx++
                        }
                        chosenRight = null
                    },
                    containerColor = JadePrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("minimal_pair_next_button")
                )
            }
        }
    }
}

