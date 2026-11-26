package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HanziStrokeRepository
import com.example.model.HanziPoint
import com.example.model.HanziStrokeData
import com.example.model.HanziStrokeEvaluator
import com.example.model.HanziStrokeParser
import com.example.model.StrokeEvaluationResult
import com.example.model.SvgPathCmd
import com.example.model.WritingPracticeMode
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
import com.example.ui.theme.JadeVibrant
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private val RicePaperBg = Color(0xFFFFFCF5)
private val SumiInk = Color(0xFF102A24)

@Composable
fun InteractiveHanziWriter(
    character: String,
    modifier: Modifier = Modifier,
    initialMode: WritingPracticeMode = WritingPracticeMode.GUIDED,
    onCharacterCompleted: (char: String, accuracyPercent: Int, isQuizMode: Boolean) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val strokeRepo = remember(context) { HanziStrokeRepository.getInstance(context) }

    var strokeData by remember(character) { mutableStateOf<HanziStrokeData?>(null) }
    var isLoadingStrokes by remember(character) { mutableStateOf(true) }

    var mode by remember { mutableStateOf(initialMode) }
    var showGuideOutline by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Animation playback state (Watch mode)
    var isPlayingAnimation by remember(character, mode) {
        mutableStateOf(mode == WritingPracticeMode.ANIMATE)
    }
    var animStrokeIndex by remember(character, mode) { mutableIntStateOf(0) }
    val animStrokeProgress = remember(character) { Animatable(0f) }

    // Guided / Quiz practice state
    var completedStrokeCount by remember(character, mode) { mutableIntStateOf(0) }
    var consecutiveMistakes by remember(character, mode) { mutableIntStateOf(0) }
    var totalMistakes by remember(character, mode) { mutableIntStateOf(0) }
    var isCharFinished by remember(character, mode) { mutableStateOf(false) }
    var lastEvaluation by remember(character, mode) { mutableStateOf<StrokeEvaluationResult?>(null) }
    val strokeScores = remember(character, mode) { mutableStateListOf<Int>() }
    val freehandStrokes = remember(character, mode) { mutableStateListOf<List<Offset>>() }

    var activeUserStroke by remember(character, mode) { mutableStateOf<List<Offset>>(emptyList()) }
    var rejectedFlashStroke by remember(character, mode) { mutableStateOf<List<Offset>?>(null) }
    var canvasPixelSize by remember { mutableStateOf(IntSize(1, 1)) }

    // Hint animation trigger in Guided / Quiz mode
    var hintPlayCounter by remember(character, mode) { mutableIntStateOf(0) }
    val hintStrokeProgress = remember(character) { Animatable(0f) }
    var isShowingHint by remember(character, mode) { mutableStateOf(false) }

    // Load Make Me a Hanzi stroke data asynchronously
    LaunchedEffect(character) {
        isLoadingStrokes = true
        strokeData = strokeRepo.getStrokeData(character)
        isLoadingStrokes = false
    }

    // Parse SVG paths once per loaded character
    val parsedSvgStrokes = remember(strokeData) {
        strokeData?.svgStrokePaths?.map { HanziStrokeParser.parseSvgPathToNormalizedCommands(it) }.orEmpty()
    }

    // Watch Mode Stroke-by-Stroke Sequence Animator
    LaunchedEffect(strokeData, mode, isPlayingAnimation, animStrokeIndex, playbackSpeed) {
        val data = strokeData ?: return@LaunchedEffect
        if (mode != WritingPracticeMode.ANIMATE || data.strokeCount == 0) return@LaunchedEffect

        if (isPlayingAnimation) {
            var idx = animStrokeIndex.coerceIn(0, data.strokeCount - 1)
            while (isPlayingAnimation && idx < data.strokeCount) {
                animStrokeIndex = idx
                animStrokeProgress.snapTo(0f)
                val durationMs = (760f / playbackSpeed).roundToInt().coerceIn(260, 1600)
                animStrokeProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
                )
                delay((220f / playbackSpeed).toLong().coerceIn(90L, 400L))
                idx++
            }
            if (isPlayingAnimation) {
                delay(700L)
                animStrokeIndex = 0
                animStrokeProgress.snapTo(0f)
            }
        }
    }

    // Clear rejected error stroke flash automatically
    LaunchedEffect(rejectedFlashStroke) {
        if (rejectedFlashStroke != null) {
            delay(560L)
            rejectedFlashStroke = null
        }
    }

    // Hint stroke brush animation for Guided / Quiz modes
    LaunchedEffect(hintPlayCounter) {
        if (hintPlayCounter > 0 && strokeData != null && !isCharFinished) {
            isShowingHint = true
            hintStrokeProgress.snapTo(0f)
            hintStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 680, easing = LinearEasing)
            )
            delay(260L)
            isShowingHint = false
        }
    }

    fun resetPractice() {
        completedStrokeCount = 0
        consecutiveMistakes = 0
        totalMistakes = 0
        isCharFinished = false
        lastEvaluation = null
        strokeScores.clear()
        freehandStrokes.clear()
        activeUserStroke = emptyList()
        rejectedFlashStroke = null
        isShowingHint = false
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 3-Mode Interactive Switcher: Watch (Animated) | Guided | Quiz
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WritingPracticeMode.entries.forEach { targetMode ->
                val selected = mode == targetMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            mode = targetMode
                            if (targetMode == WritingPracticeMode.ANIMATE) {
                                animStrokeIndex = 0
                                isPlayingAnimation = true
                            } else {
                                isPlayingAnimation = false
                                resetPractice()
                            }
                        }
                        .testTag("write_mode_${targetMode.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.dp,
                        if (selected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = targetMode.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Stroke Sequence Progress Bar
        val totalStrokes = strokeData?.strokeCount ?: 0
        if (totalStrokes > 0) {
            val currentIdxDisplay = when (mode) {
                WritingPracticeMode.ANIMATE -> (animStrokeIndex + 1).coerceAtMost(totalStrokes)
                else -> completedStrokeCount.coerceAtMost(totalStrokes)
            }
            val barFraction = when (mode) {
                WritingPracticeMode.ANIMATE -> ((animStrokeIndex + animStrokeProgress.value) / totalStrokes.toFloat()).coerceIn(0f, 1f)
                else -> (completedStrokeCount.toFloat() / totalStrokes.toFloat()).coerceIn(0f, 1f)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            mode == WritingPracticeMode.ANIMATE -> "Stroke Order Animation · $currentIdxDisplay / $totalStrokes"
                            isCharFinished -> "All $totalStrokes strokes mastered!"
                            else -> "Draw Stroke ${completedStrokeCount + 1} of $totalStrokes"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCharFinished) JadePrimary else MaterialTheme.colorScheme.onSurface
                    )
                    if (strokeScores.isNotEmpty() && mode != WritingPracticeMode.ANIMATE) {
                        val avg = strokeScores.average().roundToInt()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = JadeLightContainer
                        ) {
                            Text(
                                text = "Accuracy $avg%",
                                style = MaterialTheme.typography.labelSmall,
                                color = JadeDeep,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { barFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (isCharFinished) JadePrimary else GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Authentic Mi-Zi-Ge Interactive Stroke Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(RicePaperBg)
                .onSizeChanged { canvasPixelSize = it }
                .testTag("mizige_writing_canvas"),
            contentAlignment = Alignment.Center
        ) {
            val data = strokeData

            // Faint serif fallback outline only if SVG paths aren't available
            if ((data == null || parsedSvgStrokes.isEmpty()) &&
                ((mode == WritingPracticeMode.GUIDED && showGuideOutline) || mode == WritingPracticeMode.ANIMATE)
            ) {
                Text(
                    text = character,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 196.sp,
                    color = JadeDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.16f)
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(character, mode, data, completedStrokeCount, isCharFinished) {
                        if (mode == WritingPracticeMode.ANIMATE || isCharFinished) return@pointerInput
                        detectDragGestures(
                            onDragStart = { offset ->
                                activeUserStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activeUserStroke = activeUserStroke + change.position
                            },
                            onDragEnd = {
                                val drawn = activeUserStroke
                                activeUserStroke = emptyList()
                                if (drawn.size < 2) return@detectDragGestures

                                if (data != null && completedStrokeCount < data.strokeCount) {
                                    val w = canvasPixelSize.width.coerceAtLeast(1).toFloat()
                                    val h = canvasPixelSize.height.coerceAtLeast(1).toFloat()
                                    val normalized = drawn.map {
                                        HanziPoint((it.x / w).coerceIn(0f, 1f), (it.y / h).coerceIn(0f, 1f))
                                    }
                                    val targetMedian = data.medians[completedStrokeCount]
                                    val eval = HanziStrokeEvaluator.evaluateStroke(
                                        userStroke = normalized,
                                        targetMedian = targetMedian,
                                        lenient = (mode == WritingPracticeMode.GUIDED)
                                    )
                                    lastEvaluation = eval
                                    if (eval.isAccepted) {
                                        strokeScores.add(eval.accuracyScore)
                                        completedStrokeCount++
                                        consecutiveMistakes = 0
                                        if (completedStrokeCount >= data.strokeCount) {
                                            isCharFinished = true
                                            val penalty = (totalMistakes * 3).coerceAtMost(20)
                                            val finalScore = (strokeScores.average().roundToInt() - penalty).coerceIn(60, 100)
                                            onCharacterCompleted(
                                                character,
                                                finalScore,
                                                mode == WritingPracticeMode.QUIZ
                                            )
                                        }
                                    } else {
                                        rejectedFlashStroke = drawn
                                        consecutiveMistakes++
                                        totalMistakes++
                                        if (consecutiveMistakes >= 2) {
                                            hintPlayCounter++
                                        }
                                    }
                                } else {
                                    freehandStrokes.add(drawn)
                                }
                            },
                            onDragCancel = {
                                activeUserStroke = emptyList()
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Vermilion Mi-Zi-Ge Grid
                drawMiZiGeGrid(w, h)

                if (data != null) {
                    val hasSvg = parsedSvgStrokes.size == data.strokeCount

                    // 2. Ghost Reference Outline (hidden in Quiz mode unless finished)
                    val shouldDrawGhost = when (mode) {
                        WritingPracticeMode.ANIMATE -> true
                        WritingPracticeMode.GUIDED -> showGuideOutline
                        WritingPracticeMode.QUIZ -> isCharFinished
                    }
                    if (shouldDrawGhost) {
                        if (hasSvg) {
                            parsedSvgStrokes.forEach { cmds ->
                                drawPath(
                                    path = buildScaledSvgPath(cmds, w, h),
                                    color = Color(0xFF94A3B8).copy(alpha = 0.25f)
                                )
                            }
                        } else {
                            data.medians.forEach { median ->
                                drawNormalizedMedian(
                                    median = median,
                                    w = w,
                                    h = h,
                                    color = Color(0xFF94A3B8).copy(alpha = 0.28f),
                                    strokeWidth = w * 0.055f
                                )
                            }
                        }
                    }

                    // 3. Mode-Specific Stroke Layers
                    if (mode == WritingPracticeMode.ANIMATE) {
                        // Already animated strokes in Sumi ink
                        for (i in 0 until animStrokeIndex.coerceAtMost(data.strokeCount)) {
                            if (hasSvg) {
                                drawPath(buildScaledSvgPath(parsedSvgStrokes[i], w, h), color = SumiInk)
                            } else {
                                drawNormalizedMedian(data.medians[i], w, h, SumiInk, w * 0.062f)
                            }
                        }
                        // Currently animating stroke with glowing Jade brush & direction arrow
                        val activeMedian = data.medians.getOrNull(animStrokeIndex)
                        if (activeMedian != null) {
                            if (hasSvg) {
                                drawPath(
                                    path = buildScaledSvgPath(parsedSvgStrokes[animStrokeIndex], w, h),
                                    color = JadePrimary.copy(alpha = 0.24f)
                                )
                            }
                            drawPartialAnimatedMedian(
                                median = activeMedian,
                                fraction = animStrokeProgress.value,
                                w = w,
                                h = h,
                                color = JadePrimary,
                                brushTipColor = GoldBright,
                                strokeWidth = w * 0.066f
                            )
                            drawStartDot(activeMedian.first(), w, h, CoralVermilion)
                        }
                    } else {
                        // Completed user strokes rendered cleanly in Sumi Ink
                        for (i in 0 until completedStrokeCount.coerceAtMost(data.strokeCount)) {
                            if (hasSvg) {
                                drawPath(buildScaledSvgPath(parsedSvgStrokes[i], w, h), color = SumiInk)
                            } else {
                                drawNormalizedMedian(data.medians[i], w, h, SumiInk, w * 0.062f)
                            }
                        }

                        // Next expected stroke guide dot (in Guided mode)
                        if (!isCharFinished && completedStrokeCount < data.strokeCount) {
                            val nextMedian = data.medians[completedStrokeCount]
                            if (mode == WritingPracticeMode.GUIDED && showGuideOutline) {
                                if (hasSvg) {
                                    drawPath(
                                        path = buildScaledSvgPath(parsedSvgStrokes[completedStrokeCount], w, h),
                                        color = JadePrimary.copy(alpha = 0.22f)
                                    )
                                }
                                drawStartDot(nextMedian.first(), w, h, JadePrimary)
                            }

                            // Animated Gold hint brush on demand or after 2 mistakes
                            if (isShowingHint) {
                                drawPartialAnimatedMedian(
                                    median = nextMedian,
                                    fraction = hintStrokeProgress.value,
                                    w = w,
                                    h = h,
                                    color = GoldAccent,
                                    brushTipColor = CoralVermilion,
                                    strokeWidth = w * 0.064f
                                )
                                drawStartDot(nextMedian.first(), w, h, CoralVermilion)
                            }
                        }
                    }
                } else {
                    // Freehand fallback rendering
                    freehandStrokes.forEach { pts ->
                        drawRawUserStroke(pts, SumiInk, w * 0.06f)
                    }
                }

                // 4. Rejected Stroke Coral Flash
                rejectedFlashStroke?.let { badPts ->
                    drawRawUserStroke(badPts, CoralVermilion.copy(alpha = 0.85f), w * 0.056f)
                }

                // 5. Live Ink Stroke Currently Being Drawn by User
                if (activeUserStroke.size > 1) {
                    drawRawUserStroke(activeUserStroke, JadeDeep, w * 0.062f)
                }
            }

            // Loading indicator while fetching stroke data
            if (isLoadingStrokes) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = JadePrimary)
                        Text("Loading stroke order...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Top-right badge (Stroke number in Watch / Quiz status / Completed badge)
            if (isCharFinished && mode != WritingPracticeMode.ANIMATE) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = JadePrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Done!", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (data != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    val pillText = if (mode == WritingPracticeMode.ANIMATE) {
                        "#${(animStrokeIndex + 1).coerceAtMost(data.strokeCount)}/${data.strokeCount}"
                    } else {
                        "$completedStrokeCount/${data.strokeCount}"
                    }
                    Text(
                        text = pillText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Real-Time Canvas Feedback Banner
        AnimatedVisibility(visible = lastEvaluation != null && mode != WritingPracticeMode.ANIMATE) {
            val eval = lastEvaluation
            if (eval != null) {
                val bg = if (eval.isAccepted) JadeLightContainer else CoralContainer
                val fg = if (eval.isAccepted) JadeDeep else CoralOnContainer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stroke_feedback_banner"),
                    shape = RoundedCornerShape(14.dp),
                    color = bg,
                    border = BorderStroke(1.dp, if (eval.isAccepted) JadePrimary.copy(alpha = 0.45f) else CoralVermilion.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (eval.isAccepted) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = if (eval.isAccepted) JadePrimary else CoralVermilion,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isCharFinished) {
                                "Mastered '$character'! Avg accuracy ${strokeScores.average().roundToInt()}% · Tap Practice Again or pick the next character."
                            } else {
                                eval.feedbackMessage
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = fg
                        )
                    }
                }
            }
        }

        // Contextual Controls Bar
        if (mode == WritingPracticeMode.ANIMATE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            isPlayingAnimation = false
                            val maxIdx = ((strokeData?.strokeCount ?: 1) - 1).coerceAtLeast(0)
                            animStrokeIndex = (animStrokeIndex - 1).coerceIn(0, maxIdx)
                        },
                        modifier = Modifier.testTag("stroke_anim_prev_button")
                    ) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous stroke")
                    }
                    FilledTonalButton(
                        onClick = { isPlayingAnimation = !isPlayingAnimation },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("stroke_anim_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingAnimation) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isPlayingAnimation) "Pause" else "Play")
                    }
                    IconButton(
                        onClick = {
                            isPlayingAnimation = false
                            val maxIdx = ((strokeData?.strokeCount ?: 1) - 1).coerceAtLeast(0)
                            animStrokeIndex = (animStrokeIndex + 1).coerceIn(0, maxIdx)
                        },
                        modifier = Modifier.testTag("stroke_anim_next_button")
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next stroke")
                    }
                }

                // Speed Selector Pill (0.6× | 1× | 1.6×)
                Surface(
                    onClick = {
                        playbackSpeed = when {
                            playbackSpeed < 0.8f -> 1.0f
                            playbackSpeed < 1.3f -> 1.6f
                            else -> 0.6f
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = GoldContainer,
                    modifier = Modifier.testTag("stroke_anim_speed_button")
                ) {
                    val speedLabel = when {
                        playbackSpeed < 0.8f -> "0.6× Slow"
                        playbackSpeed < 1.3f -> "1.0×"
                        else -> "1.6× Fast"
                    }
                    Text(
                        text = speedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldOnContainer,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode == WritingPracticeMode.GUIDED) {
                    OutlinedButton(
                        onClick = { showGuideOutline = !showGuideOutline },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (showGuideOutline) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle guide",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (showGuideOutline) "Hide Guide" else "Guide", style = MaterialTheme.typography.labelMedium)
                    }
                }

                FilledTonalButton(
                    onClick = { hintPlayCounter++ },
                    enabled = strokeData != null && !isCharFinished,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stroke_hint_button")
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Stroke Hint", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = {
                        if (strokeData != null) {
                            if (completedStrokeCount > 0) {
                                completedStrokeCount--
                                if (strokeScores.isNotEmpty()) strokeScores.removeAt(strokeScores.lastIndex)
                                isCharFinished = false
                            }
                        } else if (freehandStrokes.isNotEmpty()) {
                            freehandStrokes.removeAt(freehandStrokes.lastIndex)
                        }
                    },
                    enabled = completedStrokeCount > 0 || freehandStrokes.isNotEmpty()
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo stroke")
                }

                IconButton(
                    onClick = { resetPractice() },
                    modifier = Modifier.testTag("stroke_reset_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Restart character")
                }
            }
        }
    }
}

private fun DrawScope.drawMiZiGeGrid(w: Float, h: Float) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
    val gridColor = CoralVermilion.copy(alpha = 0.34f)
    drawRect(color = CoralVermilion.copy(alpha = 0.85f), style = Stroke(width = 6f))
    drawLine(gridColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 2.4f, pathEffect = dash)
    drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 2.4f, pathEffect = dash)
    drawLine(gridColor.copy(alpha = 0.22f), Offset(0f, 0f), Offset(w, h), strokeWidth = 2f, pathEffect = dash)
    drawLine(gridColor.copy(alpha = 0.22f), Offset(w, 0f), Offset(0f, h), strokeWidth = 2f, pathEffect = dash)
}

private fun buildScaledSvgPath(commands: List<SvgPathCmd>, w: Float, h: Float): Path {
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

private fun DrawScope.drawNormalizedMedian(
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

private fun DrawScope.drawPartialAnimatedMedian(
    median: List<HanziPoint>,
    fraction: Float,
    w: Float,
    h: Float,
    color: Color,
    brushTipColor: Color,
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

    // Animated glowing brush tip & directional arrowhead
    drawCircle(color = brushTipColor, radius = strokeWidth * 0.45f, center = tip)
    val angle = atan2((tip.y - prevTip.y).toDouble(), (tip.x - prevTip.x).toDouble())
    val arrowLen = strokeWidth * 0.72f
    val leftWing = Offset(
        tip.x - (arrowLen * cos(angle - 0.55)).toFloat(),
        tip.y - (arrowLen * sin(angle - 0.55)).toFloat()
    )
    val rightWing = Offset(
        tip.x - (arrowLen * cos(angle + 0.55)).toFloat(),
        tip.y - (arrowLen * sin(angle + 0.55)).toFloat()
    )
    drawLine(Color.White, tip, leftWing, strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(Color.White, tip, rightWing, strokeWidth = 3.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawStartDot(point: HanziPoint, w: Float, h: Float, color: Color) {
    val center = Offset(point.x * w, point.y * h)
    drawCircle(color = Color.White, radius = w * 0.032f, center = center)
    drawCircle(color = color, radius = w * 0.023f, center = center)
}

private fun DrawScope.drawRawUserStroke(points: List<Offset>, color: Color, strokeWidth: Float) {
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
