package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HanziStrokeRepository
import com.example.model.HanziPoint
import com.example.model.HanziStrokeData
import com.example.model.HanziStrokeEvaluator
import com.example.model.HanziStrokeParser
import com.example.model.SvgPathCmd
import com.example.ui.components.PandaMascotAvatar
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralOnContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private data class StarterStrokeChar(
    val hanzi: String,
    val pinyin: String,
    val english: String
)

private val STARTER_STROKE_CHARS = listOf(
    StarterStrokeChar("人", "rén", "person"),
    StarterStrokeChar("水", "shuǐ", "water"),
    StarterStrokeChar("手", "shǒu", "hand"),
    StarterStrokeChar("木", "mù", "tree"),
    StarterStrokeChar("火", "huǒ", "fire"),
    StarterStrokeChar("口", "kǒu", "mouth"),
    StarterStrokeChar("土", "tǔ", "earth"),
    StarterStrokeChar("学", "xué", "study"),
    StarterStrokeChar("爱", "ài", "love"),
    StarterStrokeChar("猫", "māo", "cat")
)

private enum class StrokeCanvasMode {
    QUIZ,
    ANIMATE
}

private val RicePaperSurface = Color(0xFFFFFCF5)
private val CalligraphyInk = Color(0xFF102A24)

/**
 * Standalone, modular Interactive Hanzi Stroke-Order Practice Canvas.
 * Powered by official `hanzi-writer-data` stroke paths & medians (9,000+ characters)
 * with built-in offline fallback for all 10 starter characters.
 */
@Composable
fun StrokePracticeScreen(
    viewModel: PandaViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
    val strokeRepo = remember(context) { HanziStrokeRepository.getInstance(context) }

    var activeChar by remember { mutableStateOf(STARTER_STROKE_CHARS.first().hanzi) }
    var customInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Canvas & interaction states
    var strokeData by remember(activeChar) { mutableStateOf<HanziStrokeData?>(null) }
    var isLoadingStrokes by remember(activeChar) { mutableStateOf(true) }
    var canvasMode by remember { mutableStateOf(StrokeCanvasMode.QUIZ) }
    var showOutline by remember { mutableStateOf(true) }
    var isDrawingOnCanvas by remember { mutableStateOf(false) }

    // Animation playback state
    var animPlayTrigger by remember(activeChar) { mutableIntStateOf(0) }
    var animStrokeIndex by remember(activeChar) { mutableIntStateOf(0) }
    val animStrokeProgress = remember(activeChar) { Animatable(0f) }

    // Quiz / tracing state
    var completedStrokes by remember(activeChar) { mutableIntStateOf(0) }
    var wrongAttemptsOnStroke by remember(activeChar) { mutableIntStateOf(0) }
    var totalMistakes by remember(activeChar) { mutableIntStateOf(0) }
    var isQuizCompleted by remember(activeChar) { mutableStateOf(false) }
    var feedbackBanner by remember(activeChar) { mutableStateOf<Pair<Boolean, String>?>(null) }
    val strokeAccuracies = remember(activeChar) { mutableStateListOf<Int>() }

    // Drawing pointer paths
    var activeUserStroke by remember(activeChar) { mutableStateOf<List<Offset>>(emptyList()) }
    var rejectedStrokeFlash by remember(activeChar) { mutableStateOf<List<Offset>?>(null) }
    var canvasPixelSize by remember { mutableStateOf(IntSize(1, 1)) }

    // Auto-hint animation after 2 wrong strokes
    var hintTrigger by remember(activeChar) { mutableIntStateOf(0) }
    var isShowingHint by remember(activeChar) { mutableStateOf(false) }
    val hintStrokeProgress = remember(activeChar) { Animatable(0f) }

    // Resolve display metadata (starter list first, then HSK catalog fallback)
    val starterMeta = remember(activeChar) {
        STARTER_STROKE_CHARS.firstOrNull { it.hanzi == activeChar }
    }
    val catalogMeta = remember(activeChar, allVocab) {
        if (starterMeta != null) null else allVocab.firstOrNull { it.hanzi == activeChar }
    }
    val displayPinyin = starterMeta?.pinyin ?: catalogMeta?.pinyin ?: "Custom Hanzi"
    val displayEnglish = starterMeta?.english ?: catalogMeta?.english ?: "Tap speaker or trace strokes"

    // Load hanzi-writer-data JSON (online CDN + disk cache + local 10-char offline fallback)
    LaunchedEffect(activeChar) {
        isLoadingStrokes = true
        val loaded = strokeRepo.getStrokeData(activeChar)
            ?: buildOfflineStarterStrokeData(activeChar)
        strokeData = loaded
        isLoadingStrokes = false
    }

    val parsedSvgStrokes = remember(strokeData) {
        strokeData?.svgStrokePaths?.map { HanziStrokeParser.parseSvgPathToNormalizedCommands(it) }.orEmpty()
    }

    // Stroke-order animator triggered by "Animate" button
    LaunchedEffect(activeChar, strokeData, canvasMode, animPlayTrigger) {
        val data = strokeData ?: return@LaunchedEffect
        if (canvasMode != StrokeCanvasMode.ANIMATE || data.strokeCount == 0) return@LaunchedEffect

        for (i in 0 until data.strokeCount) {
            if (canvasMode != StrokeCanvasMode.ANIMATE) break
            animStrokeIndex = i
            animStrokeProgress.snapTo(0f)
            animStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 680, easing = LinearEasing)
            )
            delay(200L)
        }
        // Keep completed character visible briefly, then hold at final stroke
        if (canvasMode == StrokeCanvasMode.ANIMATE) {
            animStrokeIndex = data.strokeCount
        }
    }

    // Visual hint brush animation (automatically fires after 2 wrong strokes!)
    LaunchedEffect(hintTrigger) {
        if (hintTrigger > 0 && strokeData != null && !isQuizCompleted) {
            isShowingHint = true
            hintStrokeProgress.snapTo(0f)
            hintStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 680, easing = LinearEasing)
            )
            delay(280L)
            isShowingHint = false
        }
    }

    // Fade out coral flash for incorrect strokes
    LaunchedEffect(rejectedStrokeFlash) {
        if (rejectedStrokeFlash != null) {
            delay(520L)
            rejectedStrokeFlash = null
        }
    }

    fun resetQuiz() {
        canvasMode = StrokeCanvasMode.QUIZ
        completedStrokes = 0
        wrongAttemptsOnStroke = 0
        totalMistakes = 0
        isQuizCompleted = false
        feedbackBanner = null
        strokeAccuracies.clear()
        activeUserStroke = emptyList()
        rejectedStrokeFlash = null
        isShowingHint = false
    }

    fun startAnimation() {
        canvasMode = StrokeCanvasMode.ANIMATE
        animStrokeIndex = 0
        isShowingHint = false
        activeUserStroke = emptyList()
        rejectedStrokeFlash = null
        animPlayTrigger++
    }

    fun applyCustomCharacter(rawText: String) {
        val hanChar = rawText.firstOrNull { ch ->
            Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.HAN
        }
        if (hanChar != null) {
            inputError = null
            activeChar = hanChar.toString()
            customInput = hanChar.toString()
            resetQuiz()
            focusManager.clearFocus()
        } else if (rawText.isNotBlank()) {
            inputError = "Please enter or paste a single Chinese character (Hanzi)."
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState, enabled = !isDrawingOnCanvas)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontal Scrollable Starter Character Selector Bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Starter Characters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("stroke_starter_selector_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STARTER_STROKE_CHARS.forEach { item ->
                    val selected = activeChar == item.hanzi
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                activeChar = item.hanzi
                                customInput = ""
                                inputError = null
                                resetQuiz()
                            }
                            .testTag("starter_char_${item.hanzi}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        tonalElevation = if (selected) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.pinyin,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.9f) else JadePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.english,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Custom Any-Character Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { typed ->
                    customInput = typed
                    val han = typed.firstOrNull {
                        Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN
                    }
                    if (han != null) {
                        inputError = null
                        activeChar = han.toString()
                        resetQuiz()
                    }
                },
                label = { Text("Type or paste any Hanzi") },
                placeholder = { Text("e.g. 龙, 茶, 谢") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { applyCustomCharacter(customInput) }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stroke_custom_char_input")
            )
            FilledTonalButton(
                onClick = { applyCustomCharacter(customInput) },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("stroke_custom_char_go_button")
            ) {
                Text("Load", fontWeight = FontWeight.Bold)
            }
        }
        if (inputError != null) {
            Text(
                text = inputError!!,
                style = MaterialTheme.typography.labelSmall,
                color = CoralVermilion,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Active Character Header (Character + Pinyin + English Meaning above canvas)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = JadeLightContainer
                    ) {
                        Text(
                            text = activeChar,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = JadeDeep,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = displayPinyin,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = JadePrimary
                        )
                        Text(
                            text = displayEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val count = strokeData?.strokeCount ?: 0
                        if (count > 0) {
                            Text(
                                text = "$count stroke${if (count == 1) "" else "s"} · ${if (canvasMode == StrokeCanvasMode.ANIMATE) "Animating" else "Quiz Mode"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { viewModel.speakChinese(activeChar) },
                    modifier = Modifier.testTag("stroke_speak_char_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Hear pronunciation of $activeChar",
                        tint = JadePrimary
                    )
                }
            }
        }

        // Stroke Progress Indicator
        val totalStrokes = strokeData?.strokeCount ?: 0
        if (totalStrokes > 0) {
            val progress = when (canvasMode) {
                StrokeCanvasMode.ANIMATE -> {
                    if (animStrokeIndex >= totalStrokes) 1f
                    else ((animStrokeIndex + animStrokeProgress.value) / totalStrokes.toFloat()).coerceIn(0f, 1f)
                }
                StrokeCanvasMode.QUIZ -> (completedStrokes.toFloat() / totalStrokes.toFloat()).coerceIn(0f, 1f)
            }
            Column(
                modifier = Modifier.width(280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when {
                            canvasMode == StrokeCanvasMode.ANIMATE -> "Watching stroke order..."
                            isQuizCompleted -> "Completed all $totalStrokes strokes!"
                            else -> "Trace stroke ${completedStrokes + 1} of $totalStrokes"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isQuizCompleted) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = JadePrimary
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (isQuizCompleted) JadePrimary else GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 280x280dp Centered Mi-Zi-Ge (米字格) Interactive Stroke Canvas
        // Locks vertical scroll while drawing (`touch-action: none` equivalent)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(RicePaperSurface)
                .onSizeChanged { canvasPixelSize = it }
                .testTag("stroke_practice_280_canvas"),
            contentAlignment = Alignment.Center
        ) {
            val data = strokeData

            // Faint fallback outline if SVG paths weren't returned and outline is ON
            if ((data == null || parsedSvgStrokes.isEmpty()) && showOutline) {
                Text(
                    text = activeChar,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 186.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.24f)
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeChar, canvasMode, data, completedStrokes, isQuizCompleted) {
                        if (canvasMode != StrokeCanvasMode.QUIZ || isQuizCompleted) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isDrawingOnCanvas = true
                            val drawnPoints = mutableListOf(down.position)
                            activeUserStroke = drawnPoints.toList()

                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    change.consume()
                                    if (change.position != drawnPoints.lastOrNull()) {
                                        drawnPoints.add(change.position)
                                        activeUserStroke = drawnPoints.toList()
                                    }
                                }
                            } finally {
                                isDrawingOnCanvas = false
                                val finalStroke = drawnPoints.toList()
                                activeUserStroke = emptyList()

                                if (finalStroke.size >= 2 && data != null && completedStrokes < data.strokeCount) {
                                    val w = canvasPixelSize.width.coerceAtLeast(1).toFloat()
                                    val h = canvasPixelSize.height.coerceAtLeast(1).toFloat()
                                    val normalized = finalStroke.map { pt ->
                                        HanziPoint(
                                            (pt.x / w).coerceIn(0f, 1f),
                                            (pt.y / h).coerceIn(0f, 1f)
                                        )
                                    }
                                    val targetMedian = data.medians[completedStrokes]
                                    val eval = HanziStrokeEvaluator.evaluateStroke(
                                        userStroke = normalized,
                                        targetMedian = targetMedian,
                                        lenient = true
                                    )
                                    if (eval.isAccepted) {
                                        strokeAccuracies.add(eval.accuracyScore)
                                        completedStrokes++
                                        wrongAttemptsOnStroke = 0
                                        if (completedStrokes >= data.strokeCount) {
                                            isQuizCompleted = true
                                            feedbackBanner = null
                                            viewModel.awardXp(15L, null, "Mastered stroke order for $activeChar!")
                                            viewModel.speakChinese(activeChar)
                                        } else {
                                            feedbackBanner = true to "Nice stroke ${completedStrokes}/${data.strokeCount}! Keep going."
                                        }
                                    } else {
                                        rejectedStrokeFlash = finalStroke
                                        wrongAttemptsOnStroke++
                                        totalMistakes++
                                        if (wrongAttemptsOnStroke >= 2) {
                                            hintTrigger++
                                            feedbackBanner = false to "Hint shown! Follow the glowing golden stroke from the red dot."
                                        } else {
                                            feedbackBanner = false to eval.feedbackMessage
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Dashed Cross-and-Diagonal Mi-Zi-Ge (米字格) Grid
                drawMiZiGePracticeSheet(w, h)

                if (data != null) {
                    val hasSvg = parsedSvgStrokes.size == data.strokeCount

                    // 2. Faint Gray Character Outline Guide (controlled by Toggle Outline)
                    if (showOutline) {
                        if (hasSvg) {
                            parsedSvgStrokes.forEach { cmds ->
                                drawPath(
                                    path = buildCanvasSvgPath(cmds, w, h),
                                    color = Color(0xFF94A3B8).copy(alpha = 0.28f)
                                )
                            }
                        } else {
                            data.medians.forEach { median ->
                                drawMedianPath(median, w, h, Color(0xFF94A3B8).copy(alpha = 0.28f), w * 0.056f)
                            }
                        }
                    }

                    // 3. Mode Rendering
                    if (canvasMode == StrokeCanvasMode.ANIMATE) {
                        val doneCount = animStrokeIndex.coerceAtMost(data.strokeCount)
                        for (i in 0 until doneCount) {
                            if (hasSvg) {
                                drawPath(buildCanvasSvgPath(parsedSvgStrokes[i], w, h), color = CalligraphyInk)
                            } else {
                                drawMedianPath(data.medians[i], w, h, CalligraphyInk, w * 0.064f)
                            }
                        }
                        if (animStrokeIndex in 0 until data.strokeCount) {
                            val activeMedian = data.medians[animStrokeIndex]
                            if (hasSvg) {
                                drawPath(
                                    path = buildCanvasSvgPath(parsedSvgStrokes[animStrokeIndex], w, h),
                                    color = JadePrimary.copy(alpha = 0.22f)
                                )
                            }
                            drawAnimatedStrokeBrush(
                                median = activeMedian,
                                fraction = animStrokeProgress.value,
                                w = w,
                                h = h,
                                color = JadePrimary,
                                tipColor = GoldBright,
                                strokeWidth = w * 0.068f
                            )
                            drawStrokeStartMarker(activeMedian.first(), w, h, CoralVermilion)
                        }
                    } else {
                        // Completed user strokes
                        for (i in 0 until completedStrokes.coerceAtMost(data.strokeCount)) {
                            if (hasSvg) {
                                drawPath(buildCanvasSvgPath(parsedSvgStrokes[i], w, h), color = CalligraphyInk)
                            } else {
                                drawMedianPath(data.medians[i], w, h, CalligraphyInk, w * 0.064f)
                            }
                        }

                        // Subtle guide dot on next stroke if outline is visible
                        if (!isQuizCompleted && completedStrokes < data.strokeCount) {
                            val nextMedian = data.medians[completedStrokes]
                            if (showOutline) {
                                drawStrokeStartMarker(nextMedian.first(), w, h, JadePrimary.copy(alpha = 0.7f))
                            }

                            // Auto-triggered visual hint after 2 wrong strokes
                            if (isShowingHint) {
                                drawAnimatedStrokeBrush(
                                    median = nextMedian,
                                    fraction = hintStrokeProgress.value,
                                    w = w,
                                    h = h,
                                    color = GoldAccent,
                                    tipColor = CoralVermilion,
                                    strokeWidth = w * 0.068f
                                )
                                drawStrokeStartMarker(nextMedian.first(), w, h, CoralVermilion)
                            }
                        }
                    }
                }

                // 4. Coral Flash for Wrong Stroke
                rejectedStrokeFlash?.let { badPts ->
                    drawRawInkStroke(badPts, CoralVermilion.copy(alpha = 0.85f), w * 0.058f)
                }

                // 5. Active Finger Drawing Stroke
                if (activeUserStroke.size > 1) {
                    drawRawInkStroke(activeUserStroke, JadeDeep, w * 0.064f)
                }
            }

            if (isLoadingStrokes) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = JadePrimary)
                        Text("Loading strokes...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else if (strokeData == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralContainer
                ) {
                    Text(
                        text = "Stroke data unavailable offline for '$activeChar'.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralOnContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Congratulatory Panda Message on Quiz Completion!
        AnimatedVisibility(
            visible = isQuizCompleted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val avgAcc = if (strokeAccuracies.isNotEmpty()) strokeAccuracies.average().roundToInt() else 100
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stroke_panda_congrats_card"),
                shape = RoundedCornerShape(20.dp),
                color = JadeLightContainer,
                border = BorderStroke(1.5.dp, JadePrimary)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PandaMascotAvatar(size = 52.dp, animated = true)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "太棒了! Panda High-Five! 🐼",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = JadeDeep
                        )
                        Text(
                            text = "You nailed 「$activeChar」 ($displayPinyin) with $avgAcc% accuracy${if (totalMistakes == 0) " and zero mistakes!" else "."}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JadeDeep
                        )
                    }
                }
            }
        }

        // Live Stroke Feedback Banner (during quiz)
        AnimatedVisibility(visible = feedbackBanner != null && !isQuizCompleted) {
            val fb = feedbackBanner
            if (fb != null) {
                val (ok, msg) = fb
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stroke_practice_feedback"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (ok) JadeLightContainer else GoldContainer,
                    border = BorderStroke(1.dp, if (ok) JadePrimary.copy(alpha = 0.5f) else GoldAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = if (ok) JadePrimary else GoldOnContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ok) JadeDeep else GoldOnContainer
                        )
                    }
                }
            }
        }

        // 4 Touch-Friendly Control Buttons (min height 48dp, 2x2 grid)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Animate
                Button(
                    onClick = { startAnimation() },
                    enabled = strokeData != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canvasMode == StrokeCanvasMode.ANIMATE) JadeDeep else JadePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("stroke_btn_animate")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Animate", fontWeight = FontWeight.Bold)
                }

                // 2. Practice / Quiz
                Button(
                    onClick = { resetQuiz() },
                    enabled = strokeData != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canvasMode == StrokeCanvasMode.QUIZ) CoralVermilion else MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("stroke_btn_practice_quiz")
                ) {
                    Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Practice / Quiz", fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3. Toggle Outline
                OutlinedButton(
                    onClick = { showOutline = !showOutline },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("stroke_btn_toggle_outline")
                ) {
                    Icon(
                        imageVector = if (showOutline) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (showOutline) "Toggle Outline (On)" else "Toggle Outline (Off)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // 4. Reset
                OutlinedButton(
                    onClick = { resetQuiz() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.7f)
                        .height(50.dp)
                        .testTag("stroke_btn_reset")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

private fun DrawScope.drawMiZiGePracticeSheet(w: Float, h: Float) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
    val outerBorderColor = CoralVermilion.copy(alpha = 0.78f)
    val crossColor = CoralVermilion.copy(alpha = 0.38f)
    val diagColor = CoralVermilion.copy(alpha = 0.24f)

    // Outer Mi-Zi-Ge square border
    drawRect(color = outerBorderColor, style = Stroke(width = 5f))
    // Dashed vertical & horizontal center cross
    drawLine(crossColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 2.4f, pathEffect = dashEffect)
    drawLine(crossColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 2.4f, pathEffect = dashEffect)
    // Dashed diagonal guidelines (米字格)
    drawLine(diagColor, Offset(0f, 0f), Offset(w, h), strokeWidth = 2f, pathEffect = dashEffect)
    drawLine(diagColor, Offset(w, 0f), Offset(0f, h), strokeWidth = 2f, pathEffect = dashEffect)
}

private fun buildCanvasSvgPath(commands: List<SvgPathCmd>, w: Float, h: Float): Path {
    val path = Path()
    commands.forEach { cmd ->
        when (cmd) {
            is SvgPathCmd.MoveTo -> path.moveTo(cmd.x * w, cmd.y * h)
            is SvgPathCmd.LineTo -> path.lineTo(cmd.x * w, cmd.y * h)
            is SvgPathCmd.QuadTo -> path.quadraticTo(cmd.cx * w, cmd.cy * h, cmd.x * w, cmd.y * h)
            is SvgPathCmd.CubicTo -> path.cubicTo(
                cmd.c1x * w, cmd.c1y * h,
                cmd.c2x * w, cmd.c2y * h,
                cmd.x * w, cmd.y * h
            )
            SvgPathCmd.Close -> path.close()
        }
    }
    return path
}

private fun DrawScope.drawMedianPath(
    median: List<HanziPoint>,
    w: Float,
    h: Float,
    color: Color,
    strokeWidth: Float
) {
    if (median.size < 2) return
    val path = Path().apply {
        moveTo(median.first().x * w, median.first().y * h)
        for (i in 1 until median.size) {
            lineTo(median[i].x * w, median[i].y * h)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawAnimatedStrokeBrush(
    median: List<HanziPoint>,
    fraction: Float,
    w: Float,
    h: Float,
    color: Color,
    tipColor: Color,
    strokeWidth: Float
) {
    if (median.size < 2) return
    val clamped = fraction.coerceIn(0f, 1f)
    val pts = median.map { Offset(it.x * w, it.y * h) }
    val segLengths = FloatArray(pts.size - 1)
    var totalLen = 0f
    for (i in 0 until pts.size - 1) {
        val len = hypot(pts[i + 1].x - pts[i].x, pts[i + 1].y - pts[i].y)
        segLengths[i] = len
        totalLen += len
    }
    if (totalLen < 1f) return

    val targetDist = totalLen * clamped
    val path = Path().apply { moveTo(pts.first().x, pts.first().y) }
    var walked = 0f
    var tip = pts.first()
    var prevTip = pts.first()

    for (i in segLengths.indices) {
        val segLen = segLengths[i]
        if (walked + segLen <= targetDist) {
            prevTip = pts[i]
            tip = pts[i + 1]
            path.lineTo(tip.x, tip.y)
            walked += segLen
        } else {
            val remain = (targetDist - walked).coerceAtLeast(0f)
            val t = if (segLen > 0.001f) (remain / segLen).coerceIn(0f, 1f) else 0f
            prevTip = pts[i]
            tip = Offset(
                pts[i].x + (pts[i + 1].x - pts[i].x) * t,
                pts[i].y + (pts[i + 1].y - pts[i].y) * t
            )
            path.lineTo(tip.x, tip.y)
            break
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    drawCircle(color = tipColor, radius = strokeWidth * 0.44f, center = tip)
    val angle = atan2((tip.y - prevTip.y).toDouble(), (tip.x - prevTip.x).toDouble())
    val arrowLen = strokeWidth * 0.7f
    val leftWing = Offset(
        tip.x - (arrowLen * cos(angle - 0.55)).toFloat(),
        tip.y - (arrowLen * sin(angle - 0.55)).toFloat()
    )
    val rightWing = Offset(
        tip.x - (arrowLen * cos(angle + 0.55)).toFloat(),
        tip.y - (arrowLen * sin(angle + 0.55)).toFloat()
    )
    drawLine(Color.White, tip, leftWing, strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color.White, tip, rightWing, strokeWidth = 3f, cap = StrokeCap.Round)
}

private fun DrawScope.drawStrokeStartMarker(point: HanziPoint, w: Float, h: Float, color: Color) {
    val c = Offset(point.x * w, point.y * h)
    drawCircle(color = Color.White, radius = w * 0.032f, center = c)
    drawCircle(color = color, radius = w * 0.022f, center = c)
}

private fun DrawScope.drawRawInkStroke(points: List<Offset>, color: Color, strokeWidth: Float) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * Self-contained offline stroke medians fallback for all 10 starter characters
 * (used only if device has no network & no cached hanzi-writer-data JSON yet).
 */
private fun buildOfflineStarterStrokeData(charStr: String): HanziStrokeData? {
    fun pt(x: Float, y: Float) = HanziPoint(x, y)
    val mediansMap: Map<String, List<List<HanziPoint>>> = mapOf(
        "水" to listOf(
            listOf(pt(0.50f, 0.14f), pt(0.50f, 0.84f), pt(0.40f, 0.76f)),
            listOf(pt(0.18f, 0.44f), pt(0.38f, 0.41f), pt(0.28f, 0.62f), pt(0.14f, 0.76f)),
            listOf(pt(0.80f, 0.32f), pt(0.66f, 0.44f), pt(0.54f, 0.52f)),
            listOf(pt(0.52f, 0.46f), pt(0.68f, 0.66f), pt(0.88f, 0.78f))
        ),
        "手" to listOf(
            listOf(pt(0.68f, 0.16f), pt(0.48f, 0.22f), pt(0.26f, 0.25f)),
            listOf(pt(0.24f, 0.40f), pt(0.76f, 0.38f)),
            listOf(pt(0.14f, 0.58f), pt(0.86f, 0.56f)),
            listOf(pt(0.50f, 0.24f), pt(0.52f, 0.60f), pt(0.50f, 0.86f), pt(0.38f, 0.78f))
        ),
        "火" to listOf(
            listOf(pt(0.24f, 0.40f), pt(0.30f, 0.54f)),
            listOf(pt(0.80f, 0.36f), pt(0.68f, 0.50f)),
            listOf(pt(0.50f, 0.16f), pt(0.46f, 0.54f), pt(0.18f, 0.84f)),
            listOf(pt(0.50f, 0.48f), pt(0.68f, 0.70f), pt(0.86f, 0.84f))
        ),
        "土" to listOf(
            listOf(pt(0.28f, 0.48f), pt(0.72f, 0.47f)),
            listOf(pt(0.50f, 0.16f), pt(0.50f, 0.80f)),
            listOf(pt(0.16f, 0.80f), pt(0.84f, 0.79f))
        ),
        "学" to listOf(
            listOf(pt(0.28f, 0.16f), pt(0.34f, 0.28f)),
            listOf(pt(0.46f, 0.14f), pt(0.52f, 0.26f)),
            listOf(pt(0.74f, 0.14f), pt(0.64f, 0.28f)),
            listOf(pt(0.20f, 0.34f), pt(0.16f, 0.46f)),
            listOf(pt(0.20f, 0.34f), pt(0.82f, 0.32f), pt(0.74f, 0.42f)),
            listOf(pt(0.34f, 0.48f), pt(0.66f, 0.46f), pt(0.50f, 0.58f)),
            listOf(pt(0.50f, 0.58f), pt(0.52f, 0.88f), pt(0.40f, 0.80f)),
            listOf(pt(0.18f, 0.68f), pt(0.84f, 0.66f))
        ),
        "爱" to listOf(
            listOf(pt(0.68f, 0.14f), pt(0.44f, 0.20f), pt(0.24f, 0.22f)),
            listOf(pt(0.28f, 0.26f), pt(0.34f, 0.34f)),
            listOf(pt(0.48f, 0.24f), pt(0.52f, 0.32f)),
            listOf(pt(0.74f, 0.22f), pt(0.66f, 0.34f)),
            listOf(pt(0.18f, 0.38f), pt(0.14f, 0.48f)),
            listOf(pt(0.18f, 0.38f), pt(0.84f, 0.36f), pt(0.76f, 0.44f)),
            listOf(pt(0.24f, 0.52f), pt(0.76f, 0.50f)),
            listOf(pt(0.46f, 0.44f), pt(0.34f, 0.68f), pt(0.18f, 0.84f)),
            listOf(pt(0.42f, 0.62f), pt(0.68f, 0.60f), pt(0.50f, 0.76f), pt(0.28f, 0.86f)),
            listOf(pt(0.38f, 0.68f), pt(0.64f, 0.78f), pt(0.84f, 0.86f))
        ),
        "猫" to listOf(
            listOf(pt(0.34f, 0.18f), pt(0.14f, 0.42f)),
            listOf(pt(0.18f, 0.24f), pt(0.30f, 0.52f), pt(0.24f, 0.84f), pt(0.16f, 0.76f)),
            listOf(pt(0.28f, 0.48f), pt(0.12f, 0.68f)),
            listOf(pt(0.40f, 0.30f), pt(0.88f, 0.28f)),
            listOf(pt(0.52f, 0.16f), pt(0.54f, 0.38f)),
            listOf(pt(0.74f, 0.16f), pt(0.72f, 0.38f)),
            listOf(pt(0.44f, 0.48f), pt(0.46f, 0.82f)),
            listOf(pt(0.45f, 0.48f), pt(0.84f, 0.46f), pt(0.82f, 0.82f)),
            listOf(pt(0.46f, 0.64f), pt(0.82f, 0.63f)),
            listOf(pt(0.64f, 0.48f), pt(0.64f, 0.80f)),
            listOf(pt(0.46f, 0.80f), pt(0.82f, 0.79f))
        )
    )
    val found = mediansMap[charStr] ?: return null
    return HanziStrokeData(
        character = charStr,
        svgStrokePaths = emptyList(),
        medians = found,
        isOfflineFallback = true
    )
}
