package com.example.ui.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ActiveMockExamSession
import com.example.model.HskMockExamMode
import com.example.model.HskMockLevel
import com.example.model.MockExamMistakeItem
import com.example.model.MockExamQuestion
import com.example.model.MockExamReport
import com.example.model.MockExamSection
import com.example.model.MockQuestionAnswer
import com.example.model.MockQuestionKind
import com.example.model.PastMockExamRecord
import com.example.ui.components.PandaActionButton
import com.example.ui.components.PandaMascotAvatar
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralOnContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import kotlinx.coroutines.delay

@Composable
fun HskMockExamStudioSection(viewModel: PandaViewModel) {
    val activeSession by viewModel.mockExamSession.collectAsStateWithLifecycle()
    val report by viewModel.mockExamReport.collectAsStateWithLifecycle()
    val isGrading by viewModel.isGradingMockExam.collectAsStateWithLifecycle()
    val pastScores by viewModel.pastMockExams.collectAsStateWithLifecycle()

    when {
        isGrading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.5f)),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PandaMascotAvatar(size = 64.dp, animated = true)
                        CircularProgressIndicator(color = JadePrimary)
                        Text(
                            text = "Grading Your HSK 3.0 Mock Exam...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Calculating scaled Listening, Reading, and Writing scores out of 300.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        report != null -> {
            MockExamReportScreen(
                report = report!!,
                onSpeak = { viewModel.speakChinese(it) },
                onAddMissedToAnki = { words -> viewModel.addMockExamMissedWordsToAnki(words) },
                onRetakeSame = {
                    val lvl = report!!.level
                    val mode = report!!.mode
                    viewModel.exitHskMockExam()
                    viewModel.startHskMockExam(lvl, mode)
                },
                onBackToSetup = { viewModel.exitHskMockExam() }
            )
        }
        activeSession != null -> {
            MockExamLiveRunner(
                session = activeSession!!,
                onSpeak = { viewModel.speakChinese(it) },
                onSubmitExam = { answers, elapsedSec ->
                    viewModel.submitHskMockExam(answers, elapsedSec)
                },
                onExitExam = { viewModel.exitHskMockExam() }
            )
        }
        else -> {
            MockExamSetupScreen(
                pastScores = pastScores,
                onStartExam = { level, mode -> viewModel.startHskMockExam(level, mode) }
            )
        }
    }
}

@Composable
private fun MockExamSetupScreen(
    pastScores: List<PastMockExamRecord>,
    onStartExam: (HskMockLevel, HskMockExamMode) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(HskMockLevel.HSK_1) }
    var selectedMode by remember { mutableStateOf(HskMockExamMode.QUICK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Official HSK 3.0 Exam Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(JadePrimary, JadeDeep)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Assignment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "OFFICIAL-STYLE HSK 3.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldBright,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "HSK Mock Exam Simulator",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Listening (100) + Reading (100) + Writing (100) = 300 pts · Pass at 180+",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Target Level Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Select Target HSK Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HskMockLevel.entries.forEach { lvl ->
                        val selected = selectedLevel == lvl
                        FilterChip(
                            selected = selected,
                            onClick = { selectedLevel = lvl },
                            label = { Text(lvl.label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("mock_level_${lvl.id}")
                        )
                    }
                }
                Text(
                    text = if (selectedLevel.targetNumericLevel <= 3) {
                        "Writing section uses scrambled Mandarin sentence tiles${if (selectedLevel.allowPinyinToggle) " · Optional Pinyin toggle in Reading" else " · Pinyin-free Reading"}."
                    } else {
                        "Writing section challenges you to write original Mandarin sentences using 2 target ${selectedLevel.label} words (AI graded with offline fallback)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Exam Mode Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "2. Select Exam Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HskMockExamMode.entries.forEach { mode ->
                        val selected = selectedMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedMode = mode }
                                .testTag("mock_mode_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) JadeLightContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) JadeDeep else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "L:${mode.listeningCount} · R:${mode.readingCount} · W:${mode.writingCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Start Exam CTA
        PandaActionButton(
            text = "Start ${selectedLevel.label} ${selectedMode.label} (${selectedMode.totalQuestions} Qs)",
            onClick = { onStartExam(selectedLevel, selectedMode) },
            containerColor = JadePrimary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_mock_exam_button"),
            leadingIcon = {
                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        )

        // Past Exam Scores History
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("past_mock_exam_history"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Past Exam Scores",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (pastScores.isEmpty()) {
                    Text(
                        text = "No mock exams completed yet. Take your first timed HSK 3.0 mock test above to track your score out of 300!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    pastScores.take(8).forEach { rec ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${rec.levelLabel} · ${rec.modeLabel}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${rec.dateLabel} · L:${rec.listeningScore} R:${rec.readingScore} W:${rec.writingScore}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${rec.totalScore}/300",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (rec.passed) JadePrimary else CoralVermilion
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (rec.passed) JadeLightContainer else CoralContainer
                                    ) {
                                        Text(
                                            text = if (rec.passed) "PASS" else "FAIL",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (rec.passed) JadeDeep else CoralOnContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockExamLiveRunner(
    session: ActiveMockExamSession,
    onSpeak: (String) -> Unit,
    onSubmitExam: (Map<String, MockQuestionAnswer>, Int) -> Unit,
    onExitExam: () -> Unit
) {
    var currentIdx by remember(session.examId) { mutableIntStateOf(0) }
    var remainingSeconds by remember(session.examId) { mutableIntStateOf(session.durationSeconds) }
    var showPinyinForLowerLevel by remember(session.examId) { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var submitted by remember(session.examId) { mutableStateOf(false) }

    val answersMap = remember(session.examId) { mutableStateMapOf<String, MockQuestionAnswer>() }

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    fun triggerSubmit() {
        if (submitted) return
        submitted = true
        val elapsed = (session.durationSeconds - remainingSeconds).coerceAtLeast(1)
        onSubmitExam(answersMap.toMap(), elapsed)
    }

    // Live Countdown Timer — auto-submits cleanly at 00:00
    LaunchedEffect(session.examId, submitted) {
        while (remainingSeconds > 0 && !submitted) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds <= 0 && !submitted) {
            triggerSubmit()
        }
    }

    val question = session.questions[currentIdx.coerceIn(0, session.questions.lastIndex)]
    val currentAnswer = answersMap[question.id] ?: MockQuestionAnswer()
    val answeredCount = session.questions.count { q ->
        val a = answersMap[q.id]
        when (q.kind) {
            MockQuestionKind.WRITING_TILE_ORDER -> !a?.assembledTileIndices.isNullOrEmpty()
            MockQuestionKind.WRITING_FREE_SENTENCE -> !a?.typedSentence.isNullOrBlank()
            else -> a?.selectedOptionIndex != null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exam Top Bar: Timer, Section Badge, Progress, Exit
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Section Badge
                    val sectionColor = when (question.section) {
                        MockExamSection.LISTENING -> IndigoAccent
                        MockExamSection.READING -> JadePrimary
                        MockExamSection.WRITING -> GoldAccent
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = sectionColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${question.section.labelEn} ${question.section.labelZh}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = sectionColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Live Countdown Timer
                    val urgent = remainingSeconds <= 60
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (urgent) CoralContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("mock_exam_timer")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = "Remaining time",
                                tint = if (urgent) CoralVermilion else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (urgent) CoralVermilion else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Progress + Exit Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentIdx + 1} / ${session.questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showExitDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mock_exam_exit_button")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Exit exam")
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { (currentIdx + 1).toFloat() / session.questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = JadePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Active Question Card (No Instant Answers!)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mock_exam_question_card"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
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
                    Text(
                        text = "${session.level.label} · Q${currentIdx + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (question.section == MockExamSection.READING && session.level.allowPinyinToggle) {
                        FilterChip(
                            selected = showPinyinForLowerLevel,
                            onClick = { showPinyinForLowerLevel = !showPinyinForLowerLevel },
                            label = { Text(if (showPinyinForLowerLevel) "Pinyin: ON" else "Pinyin: OFF", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("mock_reading_pinyin_toggle")
                        )
                    }
                }

                Text(
                    text = question.promptText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (question.kind) {
                    MockQuestionKind.LISTENING_AUDIO_MCQ -> {
                        val replaysLeft = (2 - currentAnswer.replaysUsed).coerceAtLeast(0)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = replaysLeft > 0) {
                                        answersMap[question.id] = currentAnswer.copy(
                                            replaysUsed = currentAnswer.replaysUsed + 1
                                        )
                                        onSpeak(question.audioScript)
                                    }
                                    .testTag("mock_listening_play_button"),
                                shape = CircleShape,
                                color = if (replaysLeft > 0) IndigoAccent else MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Play listening audio",
                                        tint = if (replaysLeft > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Replays left: $replaysLeft",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (replaysLeft > 0) IndigoAccent else CoralVermilion
                            )
                        }
                        MockMcqOptionsList(
                            options = question.options,
                            selectedIndex = currentAnswer.selectedOptionIndex,
                            onSelect = { idx ->
                                answersMap[question.id] = currentAnswer.copy(selectedOptionIndex = idx)
                            }
                        )
                    }

                    MockQuestionKind.READING_HANZI_MEANING,
                    MockQuestionKind.READING_COMPREHENSION,
                    MockQuestionKind.READING_CLOZE -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = question.hanziPrompt,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = if (question.kind == MockQuestionKind.READING_HANZI_MEANING) 36.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (session.level.allowPinyinToggle && showPinyinForLowerLevel && question.pinyinPrompt.isNotBlank()) {
                                    Text(
                                        text = question.pinyinPrompt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JadePrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        MockMcqOptionsList(
                            options = question.options,
                            selectedIndex = currentAnswer.selectedOptionIndex,
                            onSelect = { idx ->
                                answersMap[question.id] = currentAnswer.copy(selectedOptionIndex = idx)
                            }
                        )
                    }

                    MockQuestionKind.WRITING_TILE_ORDER -> {
                        val placed = currentAnswer.assembledTileIndices
                        val remaining = question.scrambledTiles.indices.filter { it !in placed }

                        // Answer Drop Zone
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
                                if (placed.isEmpty()) {
                                    Text(
                                        text = "Tap the Chinese word tiles below in correct order...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        placed.forEach { tileIdx ->
                                            val (zh, py) = question.scrambledTiles[tileIdx]
                                            MockWordTileChip(
                                                hanzi = zh,
                                                pinyin = if (session.level.allowPinyinToggle) py else null,
                                                selected = true,
                                                onClick = {
                                                    answersMap[question.id] = currentAnswer.copy(
                                                        assembledTileIndices = placed.filterNot { it == tileIdx }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Remaining Tile Pool
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            remaining.forEach { tileIdx ->
                                val (zh, py) = question.scrambledTiles[tileIdx]
                                MockWordTileChip(
                                    hanzi = zh,
                                    pinyin = if (session.level.allowPinyinToggle) py else null,
                                    selected = false,
                                    onClick = {
                                        answersMap[question.id] = currentAnswer.copy(
                                            assembledTileIndices = placed + tileIdx
                                        )
                                    }
                                )
                            }
                        }

                        if (placed.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    answersMap[question.id] = currentAnswer.copy(assembledTileIndices = emptyList())
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Clear Tiles")
                            }
                        }
                    }

                    MockQuestionKind.WRITING_FREE_SENTENCE -> {
                        // Target 2 HSK Words Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            question.targetWords.forEach { word ->
                                val included = currentAnswer.typedSentence.contains(word.hanzi)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (included) JadeLightContainer else IndigoContainer.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, if (included) JadePrimary else IndigoAccent.copy(alpha = 0.45f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = word.hanzi,
                                            fontFamily = HanziSerifFontFamily,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (included) JadeDeep else IndigoAccent
                                        )
                                        Text(
                                            text = word.english,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = currentAnswer.typedSentence,
                            onValueChange = { typed ->
                                answersMap[question.id] = currentAnswer.copy(typedSentence = typed)
                            },
                            label = { Text("Type your Mandarin sentence here") },
                            placeholder = { Text("Use both target words in one natural sentence...") },
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mock_writing_sentence_input")
                        )
                    }
                }
            }
        }

        // Bottom Navigation Row (Previous / Next / Submit Exam)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (currentIdx > 0) currentIdx-- },
                enabled = currentIdx > 0,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Previous")
            }

            if (currentIdx < session.questions.lastIndex) {
                Button(
                    onClick = { currentIdx++ },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JadePrimary, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("mock_exam_next_button")
                ) {
                    Text("Next", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = { showSubmitConfirmDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralVermilion, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("mock_exam_submit_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Submit Exam", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "Answered $answeredCount of ${session.questions.size} questions · Answers are graded after submission.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit HSK Mock Exam?") },
            text = { Text("Your current exam progress will not be saved if you exit before submitting.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExitExam()
                }) {
                    Text("Exit Exam", color = CoralVermilion, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue Exam")
                }
            }
        )
    }

    if (showSubmitConfirmDialog) {
        val unanswered = session.questions.size - answeredCount
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit HSK Mock Exam?") },
            text = {
                Text(
                    if (unanswered > 0) {
                        "You still have $unanswered unanswered question(s). Submit now for your official score out of 300?"
                    } else {
                        "All ${session.questions.size} questions answered! Ready to grade your official HSK Mock Exam?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitConfirmDialog = false
                    triggerSubmit()
                }) {
                    Text("Submit & Grade", color = JadePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Review Answers")
                }
            }
        )
    }
}

@Composable
private fun MockMcqOptionsList(
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { idx, opt ->
            val isSelected = selectedIndex == idx
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(idx) }
                    .testTag("mock_mcq_option_$idx"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) JadeLightContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) JadePrimary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${'A' + idx}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) JadeDeep else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MockWordTileChip(
    hanzi: String,
    pinyin: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hanzi,
                fontFamily = HanziSerifFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (!pinyin.isNullOrBlank()) {
                Text(
                    text = pinyin,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.White.copy(alpha = 0.85f) else JadePrimary
                )
            }
        }
    }
}

@Composable
private fun MockExamReportScreen(
    report: MockExamReport,
    onSpeak: (String) -> Unit,
    onAddMissedToAnki: (List<com.example.model.VocabWord>) -> Unit,
    onRetakeSame: () -> Unit,
    onBackToSetup: () -> Unit
) {
    var addedMissedToAnki by remember(report.examId) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Official Scaled Score Certificate Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mock_exam_score_report_card"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, if (report.passed) JadePrimary else CoralVermilion)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PandaMascotAvatar(size = 58.dp, animated = report.passed)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (report.passed) JadePrimary else CoralVermilion
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (report.passed) "PASS · ${report.level.label}" else "NEEDS PRACTICE · ${report.level.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = if (report.passed) {
                                "太棒了! Official passing score reached (180+)."
                            } else {
                                "Keep going! Target 180/300 to pass ${report.level.label}."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Giant Scaled Score out of 300
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${report.totalScore}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (report.passed) JadePrimary else CoralVermilion
                    )
                    Text(
                        text = "/ 300",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 3 Section Subscore Bars (/100 each)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionScorePill("Listening 听力", report.listeningScore, IndigoAccent, Modifier.weight(1f))
                    SectionScorePill("Reading 阅读", report.readingScore, JadePrimary, Modifier.weight(1f))
                    SectionScorePill("Writing 书写", report.writingScore, GoldAccent, Modifier.weight(1f))
                }

                Text(
                    text = "+${report.xpEarned} XP & ${report.minutesLogged} min logged to your Daily Learning Tracker",
                    style = MaterialTheme.typography.labelSmall,
                    color = JadePrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Weakness Recovery CTA: Push Missed Words directly to SM-2 Anki Deck
        if (report.missedVocabWords.isNotEmpty()) {
            PandaActionButton(
                text = if (addedMissedToAnki) {
                    "Added ${report.missedVocabWords.size} Missed Words to Anki ✓"
                } else {
                    "Add All Missed Words to Anki Review (${report.missedVocabWords.size})"
                },
                onClick = {
                    if (!addedMissedToAnki) {
                        addedMissedToAnki = true
                        onAddMissedToAnki(report.missedVocabWords)
                    }
                },
                enabled = !addedMissedToAnki,
                containerColor = IndigoAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_add_missed_to_anki_button"),
                leadingIcon = {
                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }

        // Review Mistakes Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
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
                    Text(
                        text = "Review Mistakes (${report.mistakes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    ToneColorLegendRow()
                }

                if (report.mistakes.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = JadeLightContainer
                    ) {
                        Text(
                            text = "Flawless score! Zero mistakes across Listening, Reading, and Writing!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = JadeDeep,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    report.mistakes.forEach { mistake ->
                        MistakeReviewCard(mistake = mistake, onSpeak = onSpeak)
                    }
                }
            }
        }

        // Action Buttons: Retake / Setup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBackToSetup,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("Exam Setup", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onRetakeSame,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary, contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Mock", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SectionScorePill(
    label: String,
    score: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MistakeReviewCard(
    mistake: MockExamMistakeItem,
    onSpeak: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.35f))
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
                    text = "Q${mistake.questionNumber} · ${mistake.section.labelEn} ${mistake.section.labelZh}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoralVermilion
                )
                if (mistake.correctHanzi.isNotBlank()) {
                    IconButton(
                        onClick = { onSpeak(mistake.correctHanzi) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Hear correct Chinese",
                            tint = JadePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (mistake.promptSummary.isNotBlank()) {
                Text(
                    text = mistake.promptSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CoralContainer.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "Your answer: ${mistake.userAnswerDisplay}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralOnContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = JadeLightContainer.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Correct answer:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = JadeDeep
                    )
                    if (mistake.correctHanzi.isNotBlank()) {
                        ToneColoredHanziPinyinBlock(
                            hanzi = mistake.correctHanzi,
                            pinyin = mistake.correctPinyin,
                            hanziFontSize = 20.sp
                        )
                    }
                    if (mistake.correctEnglish.isNotBlank()) {
                        Text(
                            text = mistake.correctEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = JadeDeep
                        )
                    }
                }
            }

            if (mistake.feedbackNote.isNotBlank()) {
                Text(
                    text = mistake.feedbackNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
