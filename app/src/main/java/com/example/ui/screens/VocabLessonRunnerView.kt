package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CharacterAnatomy
import com.example.model.VocabWord
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant
import com.example.viewmodel.ActiveVocabLesson

private enum class VocabLessonStage(val stepIndex: Int, val label: String) {
    INTRO(0, "1. Learn Words"),
    LISTEN_QUIZ(1, "2. Listening"),
    RECALL_QUIZ(2, "3. Recall"),
    MATCH_BLITZ(3, "4. Match Pairs"),
    COMPLETE(4, "Complete")
}

private data class GeneratedQuizItem(
    val target: VocabWord,
    val reverseToHanzi: Boolean,
    val options: List<VocabWord>,
    val correctIndex: Int
)

/**
 * 4-Stage HelloChinese / SuperChinese Interactive Vocabulary Lesson Runner.
 * Works seamlessly for ANY 10-word lesson across all 11,092 HSK 3.0 words (Levels 1–9).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabLessonRunnerView(
    lesson: ActiveVocabLesson,
    wordAnatomyMap: Map<String, List<CharacterAnatomy>>,
    onSpeak: (String, Float?) -> Unit,
    onOpenWordBreakdown: (VocabWord) -> Unit,
    onSaveWordToSrs: (VocabWord) -> Unit,
    onSaveAllToSrs: (List<VocabWord>) -> Unit,
    onPracticeWrite: (VocabWord) -> Unit,
    onWrongAnswer: () -> Unit,
    onComplete: (earnedXp: Int) -> Unit,
    onExit: () -> Unit
) {
    val words = lesson.words
    var stage by remember(lesson.lessonId) { mutableStateOf(VocabLessonStage.INTRO) }
    var cardIndex by remember(lesson.lessonId, stage) { mutableIntStateOf(0) }
    var showPinyin by remember(lesson.lessonId) { mutableStateOf(true) }
    var savedAllToSrs by remember(lesson.lessonId) { mutableStateOf(false) }

    var totalChecks by remember(lesson.lessonId) { mutableIntStateOf(0) }
    var correctChecks by remember(lesson.lessonId) { mutableIntStateOf(0) }

    // Build deterministic Listening & Recall quizzes from the batch + real same-level SQLite distractors
    val listeningItems = remember(lesson.lessonId) {
        val pool = (words + lesson.distractors).distinctBy { it.hanzi }
        words.take(5).map { target ->
            val wrongs = pool.filter { it.hanzi != target.hanzi }.shuffled().take(3)
            val opts = (wrongs + target).shuffled()
            GeneratedQuizItem(
                target = target,
                reverseToHanzi = true,
                options = opts,
                correctIndex = opts.indexOfFirst { it.hanzi == target.hanzi }.coerceAtLeast(0)
            )
        }
    }

    val recallItems = remember(lesson.lessonId) {
        val pool = (words + lesson.distractors).distinctBy { it.hanzi }
        val batch = if (words.size > 5) words.drop(5) + words.take(2) else words
        batch.take(6).mapIndexed { idx, target ->
            val wrongs = pool.filter { it.hanzi != target.hanzi }.shuffled().take(3)
            val opts = (wrongs + target).shuffled()
            GeneratedQuizItem(
                target = target,
                reverseToHanzi = idx % 2 == 1,
                options = opts,
                correctIndex = opts.indexOfFirst { it.hanzi == target.hanzi }.coerceAtLeast(0)
            )
        }
    }

    // Overall progress across the 4 active learning stages
    val stageProgress = when (stage) {
        VocabLessonStage.INTRO -> 0.05f + 0.25f * ((cardIndex + 1).toFloat() / words.size.coerceAtLeast(1))
        VocabLessonStage.LISTEN_QUIZ -> 0.30f + 0.25f * ((cardIndex + 1).toFloat() / listeningItems.size.coerceAtLeast(1))
        VocabLessonStage.RECALL_QUIZ -> 0.55f + 0.25f * ((cardIndex + 1).toFloat() / recallItems.size.coerceAtLeast(1))
        VocabLessonStage.MATCH_BLITZ -> 0.88f
        VocabLessonStage.COMPLETE -> 1f
    }.coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Lesson Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier.testTag("vocab_lesson_exit_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Exit lesson")
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stage.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = JadePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { stageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = JadeVibrant,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (stage != VocabLessonStage.COMPLETE) {
                IconButton(onClick = { showPinyin = !showPinyin }) {
                    Icon(
                        imageVector = if (showPinyin) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (showPinyin) "Hide Pinyin" else "Show Pinyin",
                        tint = if (showPinyin) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stage Pills Strip
        if (stage != VocabLessonStage.COMPLETE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    VocabLessonStage.INTRO to "Learn",
                    VocabLessonStage.LISTEN_QUIZ to "Listen",
                    VocabLessonStage.RECALL_QUIZ to "Quiz",
                    VocabLessonStage.MATCH_BLITZ to "Match"
                ).forEach { (s, shortLabel) ->
                    val isCurrent = stage == s
                    val isDone = stage.stepIndex > s.stepIndex
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        color = when {
                            isCurrent -> JadePrimary
                            isDone -> JadePrimary.copy(alpha = 0.16f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = if (isDone) "✓ $shortLabel" else shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCurrent -> Color.White
                                isDone -> JadePrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        when (stage) {
            VocabLessonStage.INTRO -> {
                val word = words[cardIndex.coerceIn(0, words.lastIndex)]
                LaunchedEffect(word.hanzi) {
                    onSpeak(word.hanzi, 1.0f)
                }
                WordTeachCardStage(
                    word = word,
                    anatomy = wordAnatomyMap[word.hanzi].orEmpty(),
                    index = cardIndex,
                    total = words.size,
                    showPinyin = showPinyin,
                    onSpeakNormal = { onSpeak(word.hanzi, 1.0f) },
                    onSpeakSlow = { onSpeak(word.hanzi, 0.65f) },
                    onSpeakExample = { if (word.exampleHanzi.isNotBlank()) onSpeak(word.exampleHanzi, 0.9f) },
                    onOpenBreakdown = { onOpenWordBreakdown(word) },
                    onSaveSrs = { onSaveWordToSrs(word) },
                    onWrite = { onPracticeWrite(word) },
                    onPrev = { if (cardIndex > 0) cardIndex-- },
                    onNext = {
                        if (cardIndex < words.lastIndex) {
                            cardIndex++
                        } else {
                            stage = if (listeningItems.isNotEmpty()) VocabLessonStage.LISTEN_QUIZ else VocabLessonStage.COMPLETE
                            cardIndex = 0
                        }
                    },
                    onSkipToQuiz = {
                        stage = VocabLessonStage.LISTEN_QUIZ
                        cardIndex = 0
                    }
                )
            }

            VocabLessonStage.LISTEN_QUIZ -> {
                val item = listeningItems[cardIndex.coerceIn(0, listeningItems.lastIndex)]
                LaunchedEffect(stage, cardIndex) {
                    onSpeak(item.target.hanzi, 0.95f)
                }
                QuizDrillStage(
                    badge = "LISTENING DRILL · ${cardIndex + 1}/${listeningItems.size}",
                    prompt = "Tap Listen and pick the Mandarin word you hear:",
                    isListening = true,
                    item = item,
                    showPinyin = showPinyin,
                    onSpeak = { onSpeak(item.target.hanzi, it) },
                    onAnswer = { right ->
                        totalChecks++
                        if (right) correctChecks++ else onWrongAnswer()
                    },
                    onNext = {
                        if (cardIndex < listeningItems.lastIndex) {
                            cardIndex++
                        } else {
                            stage = if (recallItems.isNotEmpty()) VocabLessonStage.RECALL_QUIZ else VocabLessonStage.MATCH_BLITZ
                            cardIndex = 0
                        }
                    }
                )
            }

            VocabLessonStage.RECALL_QUIZ -> {
                val item = recallItems[cardIndex.coerceIn(0, recallItems.lastIndex)]
                QuizDrillStage(
                    badge = "RECALL DRILL · ${cardIndex + 1}/${recallItems.size}",
                    prompt = if (item.reverseToHanzi) {
                        "Choose the Hanzi for this meaning:"
                    } else {
                        "What does this Chinese word mean?"
                    },
                    isListening = false,
                    item = item,
                    showPinyin = showPinyin,
                    onSpeak = { onSpeak(item.target.hanzi, it) },
                    onAnswer = { right ->
                        totalChecks++
                        if (right) correctChecks++ else onWrongAnswer()
                    },
                    onNext = {
                        if (cardIndex < recallItems.lastIndex) {
                            cardIndex++
                        } else {
                            stage = if (words.size >= 4) VocabLessonStage.MATCH_BLITZ else VocabLessonStage.COMPLETE
                            cardIndex = 0
                        }
                    }
                )
            }

            VocabLessonStage.MATCH_BLITZ -> {
                MatchPairsBlitzStage(
                    words = words.take(6),
                    showPinyin = showPinyin,
                    onSpeak = { onSpeak(it, 1.0f) },
                    onCheck = { right ->
                        totalChecks++
                        if (right) correctChecks++ else onWrongAnswer()
                    },
                    onAllMatched = { stage = VocabLessonStage.COMPLETE }
                )
            }

            VocabLessonStage.COMPLETE -> {
                val accuracy = if (totalChecks > 0) (correctChecks * 100 / totalChecks) else 100
                LessonCompleteSummaryView(
                    lesson = lesson,
                    accuracyPercent = accuracy,
                    savedAllToSrs = savedAllToSrs,
                    onSpeak = { onSpeak(it, 1.0f) },
                    onInspectWord = onOpenWordBreakdown,
                    onSaveAll = {
                        savedAllToSrs = true
                        onSaveAllToSrs(words)
                    },
                    onFinish = { onComplete(lesson.xpReward) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordTeachCardStage(
    word: VocabWord,
    anatomy: List<CharacterAnatomy>,
    index: Int,
    total: Int,
    showPinyin: Boolean,
    onSpeakNormal: () -> Unit,
    onSpeakSlow: () -> Unit,
    onSpeakExample: () -> Unit,
    onOpenBreakdown: () -> Unit,
    onSaveSrs: () -> Unit,
    onWrite: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSkipToQuiz: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.45f))
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
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "WORD ${index + 1} OF $total · HSK ${word.hskLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (word.isHsk30NewTerm) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = GoldBright.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "HSK 3.0 NEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = word.hanzi,
                fontFamily = HanziSerifFontFamily,
                fontSize = if (word.hanzi.length <= 2) 52.sp else 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onSpeakNormal() }
            )

            if (showPinyin) {
                Text(
                    text = word.pinyin,
                    style = MaterialTheme.typography.titleLarge,
                    color = JadeVibrant,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = word.english,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (word.partOfSpeech.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = word.partOfSpeech,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Audio Controls (Normal + Slow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = onSpeakNormal,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen normal speed")
                    Spacer(Modifier.width(6.dp))
                    Text("Listen 1.0x")
                }
                OutlinedButton(
                    onClick = onSpeakSlow,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Rounded.SlowMotionVideo, contentDescription = "Listen slow speed")
                    Spacer(Modifier.width(6.dp))
                    Text("Slow 0.65x")
                }
            }

            // Curated Example Sentence if present
            if (word.exampleHanzi.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSpeakExample() },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Play example sentence",
                                tint = JadePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "EXAMPLE IN CONTEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = JadePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = word.exampleHanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (showPinyin && word.examplePinyin.isNotBlank()) {
                            Text(
                                text = word.examplePinyin,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (word.exampleEnglish.isNotBlank()) {
                            Text(
                                text = word.exampleEnglish,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Instant CharacterPop + HanziCraft Radical & Building-Blocks Preview Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenBreakdown() }
                    .testTag("vocab_card_story_radicals_box"),
                shape = RoundedCornerShape(16.dp),
                color = GoldBright.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AccountTree,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "HOW THIS WORD IS MADE",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = JadePrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Story & History",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (anatomy.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            anatomy.forEach { ch ->
                                val partsText = if (ch.components.isNotEmpty()) {
                                    ch.components.joinToString(" + ") { part ->
                                        if (part.meaning.isNotBlank()) "${part.glyph} (${part.meaning})" else part.glyph
                                    }
                                } else {
                                    "Radical ${ch.radical}${if (ch.radicalMeaning.isNotBlank()) " (${ch.radicalMeaning})" else ""}"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = ch.character,
                                            fontFamily = HanziSerifFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = partsText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val hintLine = ch.etymologyHint.ifBlank {
                                            "${ch.formationType} · ${ch.definition.substringBefore(";")}"
                                        }
                                        Text(
                                            text = hintLine,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Tap to explore radicals, phonetic/semantic breakdown, CharacterPop story & word history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions: Save to SM-2 + Practice Writing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onSaveSrs,
                    label = { Text("Save to Review") },
                    leadingIcon = {
                        Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.weight(1f)
                )
                if (word.writable) {
                    FilterChip(
                        selected = false,
                        onClick = onWrite,
                        label = { Text("Write Hanzi") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Navigation Footer
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (index > 0) {
            OutlinedButton(
                onClick = onPrev,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(0.38f)
                    .height(52.dp)
            ) {
                Text("Previous")
            }
        }
        PandaActionButton(
            text = if (index == total - 1) "Start Drills →" else "Next Word (${index + 2}/$total)",
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .testTag("vocab_intro_next_button"),
            containerColor = JadePrimary
        )
    }

    if (index < total - 1) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Already know these? Skip straight to drills",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSkipToQuiz() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun QuizDrillStage(
    badge: String,
    prompt: String,
    isListening: Boolean,
    item: GeneratedQuizItem,
    showPinyin: Boolean,
    onSpeak: (Float) -> Unit,
    onAnswer: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    var selected by remember(item) { mutableStateOf<Int?>(null) }
    val answered = selected != null
    val isCorrect = selected == item.correctIndex

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = prompt,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isListening) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PandaActionButton(
                        text = "Listen",
                        onClick = { onSpeak(1.0f) },
                        containerColor = JadePrimary,
                        leadingIcon = { Icon(Icons.Rounded.Hearing, contentDescription = null) },
                        modifier = Modifier.width(146.dp)
                    )
                    OutlinedButton(
                        onClick = { onSpeak(0.65f) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Rounded.SlowMotionVideo, contentDescription = "Slow audio")
                        Spacer(Modifier.width(4.dp))
                        Text("Slow")
                    }
                }
            } else if (item.reverseToHanzi) {
                Text(
                    text = item.target.english,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = item.target.hanzi,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onSpeak(1.0f) }
                )
                if (showPinyin) {
                    Text(
                        text = item.target.pinyin,
                        style = MaterialTheme.typography.titleMedium,
                        color = JadeVibrant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = { onSpeak(1.0f) },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Play audio")
                    Spacer(Modifier.width(6.dp))
                    Text("Hear Pronunciation")
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item.options.forEachIndexed { idx, opt ->
            val isChosen = selected == idx
            val showRight = answered && idx == item.correctIndex
            val showWrong = answered && isChosen && !isCorrect

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !answered) {
                        selected = idx
                        onAnswer(idx == item.correctIndex)
                        onSpeak(1.0f)
                    }
                    .testTag("vocab_quiz_option_$idx"),
                shape = RoundedCornerShape(16.dp),
                color = when {
                    showRight -> JadePrimary.copy(alpha = 0.16f)
                    showWrong -> CoralVermilion.copy(alpha = 0.16f)
                    else -> MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    if (showRight || showWrong) 2.dp else 1.dp,
                    when {
                        showRight -> JadePrimary
                        showWrong -> CoralVermilion
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${('A' + idx)}.",
                        style = MaterialTheme.typography.labelLarge,
                        color = JadePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (isListening || item.reverseToHanzi) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = opt.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (showPinyin || answered) {
                                Text(
                                    text = opt.pinyin,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = opt.english,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(visible = answered) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isCorrect) "Correct! 太棒了!" else "Answer: ${item.target.hanzi} (${item.target.pinyin})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "${item.target.hanzi} · ${item.target.pinyin} = ${item.target.english}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                PandaActionButton(
                    text = "Continue",
                    onClick = onNext,
                    containerColor = if (isCorrect) JadePrimary else CoralVermilion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_quiz_continue_button")
                )
            }
        }
    }
}

@Composable
private fun MatchPairsBlitzStage(
    words: List<VocabWord>,
    showPinyin: Boolean,
    onSpeak: (String) -> Unit,
    onCheck: (Boolean) -> Unit,
    onAllMatched: () -> Unit
) {
    val leftWords = remember(words) { words.shuffled() }
    val rightWords = remember(words) { words.shuffled() }

    var selectedLeft by remember(words) { mutableStateOf<String?>(null) }
    var matchedHanzi by remember(words) { mutableStateOf<Set<String>>(emptySet()) }
    var wrongPair by remember(words) { mutableStateOf<Pair<String, String>?>(null) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
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
                    text = "Tap matching Hanzi & Meaning pairs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${matchedHanzi.size}/${words.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = JadePrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left column: Hanzi
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    leftWords.forEach { word ->
                        val isMatched = word.hanzi in matchedHanzi
                        val isSelected = selectedLeft == word.hanzi
                        val isWrong = wrongPair?.first == word.hanzi
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isMatched) {
                                    wrongPair = null
                                    selectedLeft = word.hanzi
                                    onSpeak(word.hanzi)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isMatched -> JadePrimary.copy(alpha = 0.14f)
                                isWrong -> CoralVermilion.copy(alpha = 0.16f)
                                isSelected -> GoldBright.copy(alpha = 0.22f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(
                                if (isSelected || isMatched || isWrong) 2.dp else 1.dp,
                                when {
                                    isMatched -> JadePrimary
                                    isWrong -> CoralVermilion
                                    isSelected -> GoldAccent
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = word.hanzi,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMatched) JadePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (showPinyin) {
                                    Text(
                                        text = word.pinyin,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Right column: English meanings
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rightWords.forEach { word ->
                        val isMatched = word.hanzi in matchedHanzi
                        val isWrong = wrongPair?.second == word.hanzi
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isMatched && selectedLeft != null) {
                                    val left = selectedLeft ?: return@clickable
                                    if (left == word.hanzi) {
                                        val nextMatched = matchedHanzi + word.hanzi
                                        matchedHanzi = nextMatched
                                        selectedLeft = null
                                        wrongPair = null
                                        onCheck(true)
                                        if (nextMatched.size == words.size) {
                                            onAllMatched()
                                        }
                                    } else {
                                        wrongPair = left to word.hanzi
                                        selectedLeft = null
                                        onCheck(false)
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isMatched -> JadePrimary.copy(alpha = 0.14f)
                                isWrong -> CoralVermilion.copy(alpha = 0.16f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(
                                if (isMatched || isWrong) 2.dp else 1.dp,
                                when {
                                    isMatched -> JadePrimary
                                    isWrong -> CoralVermilion
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = word.english,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isMatched) JadePrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonCompleteSummaryView(
    lesson: ActiveVocabLesson,
    accuracyPercent: Int,
    savedAllToSrs: Boolean,
    onSpeak: (String) -> Unit,
    onInspectWord: (VocabWord) -> Unit,
    onSaveAll: () -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(JadeDeep, JadePrimary, Color(0xFF008970))))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GoldBright),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = "Lesson complete",
                    tint = Color(0xFF2B1C00),
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "Lesson Mastered! 太棒了!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${lesson.title} · ${lesson.words.size} HSK ${lesson.hskLevel} words",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Text(
                        text = "+${lesson.xpReward} XP",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldBright,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Text(
                        text = "$accuracyPercent% Accuracy",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Words Learned (Tap any for Story & Radicals)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lesson.words.forEach { w ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSpeak(w.hanzi)
                                onInspectWord(w)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = w.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = w.pinyin,
                                style = MaterialTheme.typography.labelSmall,
                                color = JadePrimary
                            )
                        }
                    }
                }
            }
        }
    }

    OutlinedButton(
        onClick = onSaveAll,
        enabled = !savedAllToSrs,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("vocab_lesson_save_all_srs")
    ) {
        Icon(
            imageVector = if (savedAllToSrs) Icons.Rounded.CheckCircle else Icons.Rounded.BookmarkAdd,
            contentDescription = null,
            tint = if (savedAllToSrs) JadePrimary else IndigoAccent
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (savedAllToSrs) "All ${lesson.words.size} Words Added to SM-2 Review" else "Save All ${lesson.words.size} Words to SM-2 Review"
        )
    }

    PandaActionButton(
        text = "Claim +${lesson.xpReward} XP & Continue",
        onClick = onFinish,
        containerColor = JadePrimary,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_lesson_claim_xp")
    )
}
