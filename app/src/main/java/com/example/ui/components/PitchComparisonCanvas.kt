package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ActivePlaybackSource
import com.example.audio.NormalizedPitchPoint
import com.example.audio.PitchPracticeUiState
import com.example.audio.PronunciationPitchReport
import com.example.audio.SyllablePitchFeedback
import com.example.audio.TargetSyllableSpec
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralOnContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.Tone1Red
import com.example.ui.theme.Tone2Green
import com.example.ui.theme.Tone3Blue
import com.example.ui.theme.Tone4Purple
import kotlin.math.roundToInt

private fun pitchToneColor(tone: Int): Color = when (tone) {
    1 -> Tone1Red
    2 -> Tone2Green
    3 -> Tone3Blue
    4 -> Tone4Purple
    else -> Color(0xFF7B8794)
}

@Composable
fun PitchComparisonCanvasCard(
    uiState: PitchPracticeUiState,
    syllables: List<TargetSyllableSpec>,
    modifier: Modifier = Modifier
) {
    val animatedRms by animateFloatAsState(
        targetValue = if (uiState.isRecording) uiState.liveRms else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "mic_rms_meter"
    )

    val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(
            width = if (uiState.isRecording) 2.dp else 1.dp,
            color = if (uiState.isRecording) CoralVermilion else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title + Live Pitch Hz / Native Curve Source Pill
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
                        imageVector = if (uiState.isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.AutoGraph,
                        contentDescription = null,
                        tint = if (uiState.isRecording) CoralVermilion else JadePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pitch Contour Comparison",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Live Hz badge or native audio source badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        uiState.isRecording -> CoralContainer
                        uiState.usedRealNativeWav -> JadeLightContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (uiState.isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CoralVermilion)
                            )
                            Text(
                                text = if (uiState.liveHz > 0f) {
                                    "LIVE ${uiState.liveHz.roundToInt()} Hz"
                                } else {
                                    "LISTENING..."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralOnContainer
                            )
                        } else {
                            Text(
                                text = when {
                                    uiState.isPreparingNative -> "Analyzing native..."
                                    uiState.usedRealNativeWav -> "Native F0 Track"
                                    else -> "Chao 5-Level Guide"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.usedRealNativeWav) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Legend + Live Mic Level Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PitchLegendChip(label = "Native Speaker", color = JadePrimary, dashed = false)
                    PitchLegendChip(label = "Your Voice", color = CoralVermilion, dashed = false)
                }

                if (uiState.isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format("%.1fs", uiState.recordingDurationMs / 1000f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralVermilion
                        )
                        MicLevelBars(rms = animatedRms)
                    }
                }
            }

            // Interactive Dual-Curve Pitch Graph Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .testTag("pitch_comparison_canvas")
            ) {
                // Chao 5-level pitch labels along left gutter
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp, top = 12.dp, bottom = 12.dp)
                        .height(172.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("5 High", "4", "3 Mid", "2", "1 Low").forEach { lbl ->
                        Text(
                            text = lbl,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelTextColor.copy(alpha = 0.78f)
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 38.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    if (w <= 4f || h <= 4f) return@Canvas

                    // 1. Syllable vertical lanes (tinted softly with tone color)
                    val sylCount = syllables.size.coerceAtLeast(1)
                    val laneWidth = w / sylCount
                    for (i in 0 until sylCount) {
                        val tColor = pitchToneColor(syllables.getOrNull(i)?.expectedTone ?: 5)
                        drawRect(
                            color = tColor.copy(alpha = if (i % 2 == 0) 0.055f else 0.025f),
                            topLeft = Offset(i * laneWidth, 0f),
                            size = androidx.compose.ui.geometry.Size(laneWidth, h)
                        )
                        if (i > 0) {
                            drawLine(
                                color = gridLineColor,
                                start = Offset(i * laneWidth, 0f),
                                end = Offset(i * laneWidth, h),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }
                    }

                    // 2. Horizontal 5-level Chao pitch guide lines
                    val chaoLevels = listOf(0.90f, 0.70f, 0.50f, 0.30f, 0.10f)
                    chaoLevels.forEach { lvl ->
                        val y = (1f - lvl) * h
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = if (lvl == 0.50f) 2f else 1.2f,
                            pathEffect = if (lvl == 0.50f) null else PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    }

                    // 3. Native Speaker Target Corridor (±0.11 shaded tolerance band) + Native Curve
                    if (uiState.nativeContour.size >= 2) {
                        drawToleranceRibbon(
                            points = uiState.nativeContour,
                            w = w,
                            h = h,
                            halfBand = 0.105f,
                            color = JadePrimary.copy(alpha = 0.14f)
                        )
                        drawPitchCurve(
                            points = uiState.nativeContour,
                            w = w,
                            h = h,
                            color = JadePrimary,
                            strokeWidth = 6.5f,
                            dashed = !uiState.usedRealNativeWav
                        )
                    }

                    // 4. User Live or Analyzed Pitch Curve
                    val userPoints = uiState.livePitchPoints
                    if (userPoints.size >= 2) {
                        drawPitchCurve(
                            points = userPoints,
                            w = w,
                            h = h,
                            color = CoralVermilion,
                            strokeWidth = 8.5f,
                            dashed = false
                        )
                    }

                    // 5. Live Pitch Cursor Dot during recording
                    if (uiState.isRecording) {
                        val lastVoiced = userPoints.lastOrNull { it.isVoiced }
                        if (lastVoiced != null) {
                            val cx = lastVoiced.t * w
                            val cy = (1f - lastVoiced.pitchLevel) * h
                            drawCircle(
                                color = CoralVermilion.copy(alpha = 0.25f),
                                radius = 16f + animatedRms * 10f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = CoralVermilion,
                                radius = 8f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // 6. Synchronized Playback Sweeping Playhead
                    if (uiState.activePlayback != ActivePlaybackSource.NONE) {
                        val px = uiState.playbackProgress.coerceIn(0f, 1f) * w
                        val playheadColor = if (uiState.activePlayback == ActivePlaybackSource.NATIVE) {
                            JadePrimary
                        } else {
                            CoralVermilion
                        }
                        drawLine(
                            color = playheadColor,
                            start = Offset(px, 0f),
                            end = Offset(px, h),
                            strokeWidth = 4f
                        )
                        val activeCurve = if (uiState.activePlayback == ActivePlaybackSource.NATIVE) {
                            uiState.nativeContour
                        } else {
                            userPoints
                        }
                        val nearest = activeCurve.minByOrNull { kotlin.math.abs(it.t - uiState.playbackProgress) }
                        if (nearest != null && nearest.isVoiced) {
                            val cy = (1f - nearest.pitchLevel) * h
                            drawCircle(color = playheadColor.copy(alpha = 0.25f), radius = 14f, center = Offset(px, cy))
                            drawCircle(color = playheadColor, radius = 6.5f, center = Offset(px, cy))
                        }
                    }
                }

                // Helpful empty-state hint overlay before first recording
                if (!uiState.isRecording && uiState.livePitchPoints.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Tap Record & match the green curve",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Syllable Bottom Axis Alignment Strip
            if (syllables.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 38.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    syllables.forEach { syl ->
                        val tCol = pitchToneColor(syl.expectedTone)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = syl.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = tCol
                            )
                            Text(
                                text = "${syl.pinyin} · T${syl.expectedTone}",
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PitchAnalysisReportCard(
    report: PronunciationPitchReport,
    modifier: Modifier = Modifier
) {
    val passed = report.overallScore >= 72
    val accent = when {
        report.overallScore >= 85 -> JadePrimary
        report.overallScore >= 68 -> GoldAccent
        else -> CoralVermilion
    }
    val containerColor = when {
        report.overallScore >= 85 -> JadeLightContainer
        report.overallScore >= 68 -> GoldContainer
        else -> CoralContainer
    }
    val onContainerColor = when {
        report.overallScore >= 85 -> JadePrimary
        report.overallScore >= 68 -> GoldOnContainer
        else -> CoralOnContainer
    }

    AnimatedVisibility(visible = true) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("pitch_report_card"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top score banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                report.overallScore >= 85 -> "Native-Like Pitch Match!"
                                report.overallScore >= 70 -> "Strong Mandarin Tones"
                                report.overallScore > 0 -> "Keep Shaping Your Tones"
                                else -> "Voice Not Detected Clearly"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (report.userMeanHz > 0f) {
                            val hzSummary = buildString {
                                append("Your avg pitch: ${report.userMeanHz.roundToInt()} Hz")
                                if (report.nativeMeanHz > 0f) {
                                    append(" · Native: ${report.nativeMeanHz.roundToInt()} Hz")
                                }
                            }
                            Text(
                                text = hzSummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = containerColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (passed) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = onContainerColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${report.overallScore}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = onContainerColor
                            )
                        }
                    }
                }

                // 3 Sub-score bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricScorePill(
                        label = "Contour",
                        score = report.contourAccuracy,
                        color = JadePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricScorePill(
                        label = "Tones",
                        score = report.syllableAccuracy,
                        color = Tone3Blue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricScorePill(
                        label = "Range",
                        score = report.toneRangeScore,
                        color = Tone4Purple,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Coaching Summary Tip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = containerColor.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (passed) Icons.Rounded.TipsAndUpdates else Icons.Rounded.Info,
                            contentDescription = null,
                            tint = onContainerColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = report.summaryTip,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = onContainerColor
                        )
                    }
                }

                // Per-syllable pitch diagnostics
                if (report.syllableFeedback.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.syllableFeedback.forEach { fb ->
                            SyllableFeedbackChip(feedback = fb)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricScorePill(
    label: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            LinearProgressIndicator(
                progress = { (score / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50)),
                color = color,
                trackColor = color.copy(alpha = 0.18f)
            )
        }
    }
}

@Composable
private fun SyllableFeedbackChip(feedback: SyllablePitchFeedback) {
    val tCol = pitchToneColor(feedback.expectedTone)
    val good = feedback.score >= 72
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (good) JadePrimary.copy(alpha = 0.45f) else tCol.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = feedback.hanzi,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = tCol
                )
                Text(
                    text = feedback.pinyin,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${feedback.score}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (good) JadePrimary else CoralVermilion
                )
            }
            Text(
                text = "Heard: ${feedback.detectedPattern}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PitchLegendChip(label: String, color: Color, dashed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(3.5.dp)
                .clip(RoundedCornerShape(50))
                .background(if (dashed) color.copy(alpha = 0.65f) else color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MicLevelBars(rms: Float) {
    val barMultipliers = listOf(0.45f, 0.85f, 1.0f, 0.7f, 0.4f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        barMultipliers.forEach { mult ->
            val hDp = (4f + 12f * (rms * mult).coerceIn(0f, 1f)).dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(hDp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(listOf(CoralVermilion, GoldAccent))
                    )
            )
        }
    }
}

private fun DrawScope.drawToleranceRibbon(
    points: List<NormalizedPitchPoint>,
    w: Float,
    h: Float,
    halfBand: Float,
    color: Color
) {
    var segStart = 0
    while (segStart < points.size) {
        while (segStart < points.size && !points[segStart].isVoiced) segStart++
        if (segStart >= points.size) break
        var segEnd = segStart
        while (segEnd < points.size && points[segEnd].isVoiced) segEnd++
        if (segEnd - segStart >= 2) {
            val path = Path()
            val first = points[segStart]
            path.moveTo(first.t * w, (1f - (first.pitchLevel + halfBand).coerceIn(0.02f, 0.98f)) * h)
            for (i in segStart + 1 until segEnd) {
                val p = points[i]
                path.lineTo(p.t * w, (1f - (p.pitchLevel + halfBand).coerceIn(0.02f, 0.98f)) * h)
            }
            for (i in segEnd - 1 downTo segStart) {
                val p = points[i]
                path.lineTo(p.t * w, (1f - (p.pitchLevel - halfBand).coerceIn(0.02f, 0.98f)) * h)
            }
            path.close()
            drawPath(path = path, color = color)
        }
        segStart = segEnd + 1
    }
}

private fun DrawScope.drawPitchCurve(
    points: List<NormalizedPitchPoint>,
    w: Float,
    h: Float,
    color: Color,
    strokeWidth: Float,
    dashed: Boolean
) {
    val effect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
    var segStart = 0
    while (segStart < points.size) {
        while (segStart < points.size && !points[segStart].isVoiced) segStart++
        if (segStart >= points.size) break
        var segEnd = segStart
        while (segEnd < points.size && points[segEnd].isVoiced) segEnd++

        if (segEnd - segStart >= 2) {
            val path = Path()
            val p0 = points[segStart]
            path.moveTo(p0.t * w, (1f - p0.pitchLevel) * h)
            for (i in segStart + 1 until segEnd) {
                val prev = points[i - 1]
                val cur = points[i]
                val prevX = prev.t * w
                val prevY = (1f - prev.pitchLevel) * h
                val curX = cur.t * w
                val curY = (1f - cur.pitchLevel) * h
                val midX = (prevX + curX) / 2f
                val midY = (prevY + curY) / 2f
                path.quadraticTo(prevX, prevY, midX, midY)
            }
            val last = points[segEnd - 1]
            path.lineTo(last.t * w, (1f - last.pitchLevel) * h)

            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = effect
                )
            )
        } else if (segEnd - segStart == 1) {
            val pt = points[segStart]
            drawCircle(
                color = color,
                radius = strokeWidth * 0.55f,
                center = Offset(pt.t * w, (1f - pt.pitchLevel) * h)
            )
        }
        segStart = segEnd + 1
    }
}
