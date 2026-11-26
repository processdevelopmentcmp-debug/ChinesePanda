package com.example.model


import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class HanziPoint(
    val x: Float, // Normalized 0f..1f left-to-right
    val y: Float  // Normalized 0f..1f top-to-bottom
)

/**
 * Complete stroke-order geometry for a single Chinese character in normalized [0f..1f] coordinates.
 */
data class HanziStrokeData(
    val character: String,
    val svgStrokePaths: List<String>,          // Raw Make Me a Hanzi SVG path strings (1024x1024, Y-up from -124..900)
    val medians: List<List<HanziPoint>>,       // Ordered stroke medians normalized to 0f..1f (Y-down for Compose Canvas)
    val isOfflineFallback: Boolean = false
) {
    val strokeCount: Int get() = medians.size
}

enum class WritingPracticeMode(val label: String) {
    ANIMATE("Watch"),
    GUIDED("Guided"),
    QUIZ("Quiz")
}

enum class StrokeFeedbackType {
    PERFECT,
    GOOD,
    WRONG_DIRECTION,
    WRONG_START,
    OFF_PATH,
    TOO_SHORT
}

data class StrokeEvaluationResult(
    val isAccepted: Boolean,
    val accuracyScore: Int, // 0..100
    val feedbackType: StrokeFeedbackType,
    val feedbackMessage: String
)

sealed interface SvgPathCmd {
    data class MoveTo(val x: Float, val y: Float) : SvgPathCmd
    data class LineTo(val x: Float, val y: Float) : SvgPathCmd
    data class QuadTo(val cx: Float, val cy: Float, val x: Float, val y: Float) : SvgPathCmd
    data class CubicTo(
        val c1x: Float,
        val c1y: Float,
        val c2x: Float,
        val c2y: Float,
        val x: Float,
        val y: Float
    ) : SvgPathCmd
    data object Close : SvgPathCmd
}

/**
 * Pure Kotlin parser for Make Me a Hanzi / hanzi-writer-data JSON and SVG path commands.
 */
object HanziStrokeParser {

    /**
     * Converts Make Me a Hanzi coordinate space (x in 0..1024, y in -124..900 Y-up)
     * into normalized [0f..1f] Y-down Compose Canvas coordinates.
     */
    fun normalizeMmahPoint(rawX: Float, rawY: Float): HanziPoint {
        val nx = (rawX / 1024f).coerceIn(0f, 1f)
        val ny = ((900f - rawY) / 1024f).coerceIn(0f, 1f)
        return HanziPoint(nx, ny)
    }

    private val STROKE_STRING_REGEX = Regex("\"((?:\\\\.|[^\"\\\\])*)\"")
    private val NUMBER_PAIR_REGEX = Regex("\\[\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*]")

    /**
     * Dependency-free pure-Kotlin parser for Make Me a Hanzi / hanzi-writer-data JSON:
     * `{ "strokes": ["M ... Z", ...], "medians": [[[x, y], ...], ...] }`.
     */
    fun parseHanziWriterJson(character: String, jsonText: String): HanziStrokeData? {
        return runCatching {
            val strokesBlock = extractJsonArrayContent(jsonText, "\"strokes\"") ?: return null
            val mediansBlock = extractJsonArrayContent(jsonText, "\"medians\"") ?: return null

            val svgStrokes = STROKE_STRING_REGEX.findAll(strokesBlock)
                .map { it.groupValues[1].trim() }
                .filter { it.isNotEmpty() }
                .toList()

            val medians = splitTopLevelArrays(mediansBlock).mapNotNull { strokeArr ->
                val pts = NUMBER_PAIR_REGEX.findAll(strokeArr).mapNotNull { match ->
                    val rx = match.groupValues[1].toFloatOrNull() ?: return@mapNotNull null
                    val ry = match.groupValues[2].toFloatOrNull() ?: return@mapNotNull null
                    normalizeMmahPoint(rx, ry)
                }.toList()
                when {
                    pts.size >= 2 -> pts
                    pts.size == 1 -> listOf(
                        pts[0],
                        HanziPoint((pts[0].x + 0.02f).coerceAtMost(1f), (pts[0].y + 0.02f).coerceAtMost(1f))
                    )
                    else -> null
                }
            }

            if (medians.isEmpty()) return null
            HanziStrokeData(
                character = character,
                svgStrokePaths = svgStrokes,
                medians = medians,
                isOfflineFallback = false
            )
        }.getOrNull()
    }

    private fun extractJsonArrayContent(json: String, keyToken: String): String? {
        val keyIdx = json.indexOf(keyToken)
        if (keyIdx < 0) return null
        val openIdx = json.indexOf('[', keyIdx + keyToken.length)
        if (openIdx < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in openIdx until json.length) {
            val c = json[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\' && inString) {
                escaped = true
                continue
            }
            if (c == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (c == '[') depth++
                else if (c == ']') {
                    depth--
                    if (depth == 0) {
                        return json.substring(openIdx + 1, i)
                    }
                }
            }
        }
        return null
    }

    private fun splitTopLevelArrays(content: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var start = -1
        for (i in content.indices) {
            when (content[i]) {
                '[' -> {
                    if (depth == 0) start = i
                    depth++
                }
                ']' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        result.add(content.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return result
    }

    private val SVG_TOKEN_REGEX = Regex("[MLQCZmlqcz]|-?\\d+(?:\\.\\d+)?")

    /**
     * Parses Make Me a Hanzi SVG path strings (M, L, Q, C, Z) into normalized [0f..1f] commands.
     */
    fun parseSvgPathToNormalizedCommands(svgPath: String): List<SvgPathCmd> {
        val tokens = SVG_TOKEN_REGEX.findAll(svgPath).map { it.value }.toList()
        if (tokens.isEmpty()) return emptyList()

        val commands = mutableListOf<SvgPathCmd>()
        var idx = 0
        var lastCmd = 'M'

        fun nextFloat(): Float? {
            if (idx >= tokens.size) return null
            val parsed = tokens[idx].toFloatOrNull() ?: return null
            idx++
            return parsed
        }

        while (idx < tokens.size) {
            val tok = tokens[idx]
            if (tok.length == 1 && tok[0].isLetter()) {
                lastCmd = tok[0]
                idx++
                if (lastCmd == 'Z' || lastCmd == 'z') {
                    commands.add(SvgPathCmd.Close)
                }
                continue
            }

            when (lastCmd) {
                'M', 'm' -> {
                    val x = nextFloat() ?: break
                    val y = nextFloat() ?: break
                    val p = normalizeMmahPoint(x, y)
                    commands.add(SvgPathCmd.MoveTo(p.x, p.y))
                    lastCmd = 'L'
                }
                'L', 'l' -> {
                    val x = nextFloat() ?: break
                    val y = nextFloat() ?: break
                    val p = normalizeMmahPoint(x, y)
                    commands.add(SvgPathCmd.LineTo(p.x, p.y))
                }
                'Q', 'q' -> {
                    val cx = nextFloat() ?: break
                    val cy = nextFloat() ?: break
                    val x = nextFloat() ?: break
                    val y = nextFloat() ?: break
                    val cp = normalizeMmahPoint(cx, cy)
                    val ep = normalizeMmahPoint(x, y)
                    commands.add(SvgPathCmd.QuadTo(cp.x, cp.y, ep.x, ep.y))
                }
                'C', 'c' -> {
                    val c1x = nextFloat() ?: break
                    val c1y = nextFloat() ?: break
                    val c2x = nextFloat() ?: break
                    val c2y = nextFloat() ?: break
                    val x = nextFloat() ?: break
                    val y = nextFloat() ?: break
                    val cp1 = normalizeMmahPoint(c1x, c1y)
                    val cp2 = normalizeMmahPoint(c2x, c2y)
                    val ep = normalizeMmahPoint(x, y)
                    commands.add(SvgPathCmd.CubicTo(cp1.x, cp1.y, cp2.x, cp2.y, ep.x, ep.y))
                }
                else -> idx++
            }
        }
        return commands
    }
}

/**
 * Pure Kotlin geometric evaluator that scores a user-drawn stroke against the target
 * Chinese stroke median in normalized [0f..1f] canvas coordinates.
 */
object HanziStrokeEvaluator {

    private const val SAMPLE_COUNT = 16

    fun pathLength(points: List<HanziPoint>): Float {
        if (points.size < 2) return 0f
        var sum = 0f
        for (i in 1 until points.size) {
            sum += hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
        }
        return sum
    }

    fun resample(points: List<HanziPoint>, count: Int = SAMPLE_COUNT): List<HanziPoint> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1 || count <= 1) return List(count.coerceAtLeast(1)) { points.first() }
        val totalLen = pathLength(points)
        if (totalLen < 1e-4f) return List(count) { points.first() }

        val step = totalLen / (count - 1)
        val result = mutableListOf(points.first())
        var accumulated = 0f
        var prev = points.first()
        var i = 1

        while (i < points.size && result.size < count) {
            val curr = points[i]
            val segLen = hypot(curr.x - prev.x, curr.y - prev.y)
            if (accumulated + segLen >= step && segLen > 1e-5f) {
                val t = ((step - accumulated) / segLen).coerceIn(0f, 1f)
                val nx = prev.x + t * (curr.x - prev.x)
                val ny = prev.y + t * (curr.y - prev.y)
                val interpolated = HanziPoint(nx, ny)
                result.add(interpolated)
                prev = interpolated
                accumulated = 0f
            } else {
                accumulated += segLen
                prev = curr
                i++
            }
        }
        while (result.size < count) {
            result.add(points.last())
        }
        return result
    }

    fun directionLabel(median: List<HanziPoint>): String {
        if (median.size < 2) return "along the guide"
        val dx = median.last().x - median.first().x
        val dy = median.last().y - median.first().y
        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        return when {
            angleDeg in -25.0..25.0 -> "left to right"
            angleDeg in 25.0..65.0 -> "down and right"
            angleDeg in 65.0..115.0 -> "top to bottom"
            angleDeg in 115.0..155.0 -> "down and left"
            abs(angleDeg) > 155.0 -> "right to left"
            else -> "along the guide"
        }
    }

    fun evaluateStroke(
        userStroke: List<HanziPoint>,
        targetMedian: List<HanziPoint>,
        lenient: Boolean = false
    ): StrokeEvaluationResult {
        if (userStroke.size < 2 || targetMedian.size < 2) {
            return StrokeEvaluationResult(
                isAccepted = false,
                accuracyScore = 0,
                feedbackType = StrokeFeedbackType.TOO_SHORT,
                feedbackMessage = "Draw a complete stroke on the grid."
            )
        }

        val targetLen = pathLength(targetMedian)
        val userLen = pathLength(userStroke)
        val minRequiredLen = (targetLen * 0.30f).coerceAtLeast(0.035f)
        if (userLen < minRequiredLen) {
            return StrokeEvaluationResult(
                isAccepted = false,
                accuracyScore = 15,
                feedbackType = StrokeFeedbackType.TOO_SHORT,
                feedbackMessage = "Stroke too short — draw all the way ${directionLabel(targetMedian)}."
            )
        }

        val userResampled = resample(userStroke, SAMPLE_COUNT)
        val targetResampled = resample(targetMedian, SAMPLE_COUNT)

        // Overall direction vector cosine similarity
        val udx = userResampled.last().x - userResampled.first().x
        val udy = userResampled.last().y - userResampled.first().y
        val tdx = targetResampled.last().x - targetResampled.first().x
        val tdy = targetResampled.last().y - targetResampled.first().y
        val uMag = sqrt(udx * udx + udy * udy)
        val tMag = sqrt(tdx * tdx + tdy * tdy)

        // Also check forward vs reversed mean point distance (handles hooked/curved strokes like 乙 / 折!)
        var forwardDistSum = 0f
        var reverseDistSum = 0f
        for (i in 0 until SAMPLE_COUNT) {
            val u = userResampled[i]
            val tf = targetResampled[i]
            val tr = targetResampled[SAMPLE_COUNT - 1 - i]
            forwardDistSum += hypot(u.x - tf.x, u.y - tf.y)
            reverseDistSum += hypot(u.x - tr.x, u.y - tr.y)
        }
        val meanForwardDist = forwardDistSum / SAMPLE_COUNT
        val meanReverseDist = reverseDistSum / SAMPLE_COUNT

        val cosSim = if (uMag > 0.03f && tMag > 0.03f) {
            (udx * tdx + udy * tdy) / (uMag * tMag)
        } else {
            1f
        }

        if (meanReverseDist + 0.06f < meanForwardDist || (cosSim < -0.25f && targetLen > 0.14f)) {
            return StrokeEvaluationResult(
                isAccepted = false,
                accuracyScore = 22,
                feedbackType = StrokeFeedbackType.WRONG_DIRECTION,
                feedbackMessage = "Wrong direction — draw ${directionLabel(targetMedian)}."
            )
        }

        val startDist = hypot(
            userResampled.first().x - targetResampled.first().x,
            userResampled.first().y - targetResampled.first().y
        )
        val endDist = hypot(
            userResampled.last().x - targetResampled.last().x,
            userResampled.last().y - targetResampled.last().y
        )

        val maxStartDist = if (lenient) 0.28f else 0.24f
        val maxMeanDist = if (lenient) 0.24f else 0.19f
        val maxEndDist = if (lenient) 0.30f else 0.25f

        if (startDist > maxStartDist) {
            return StrokeEvaluationResult(
                isAccepted = false,
                accuracyScore = ((1f - startDist) * 60f).roundToInt().coerceIn(10, 55),
                feedbackType = StrokeFeedbackType.WRONG_START,
                feedbackMessage = "Check where this stroke begins — start near the numbered dot."
            )
        }

        if (meanForwardDist > maxMeanDist || endDist > maxEndDist) {
            return StrokeEvaluationResult(
                isAccepted = false,
                accuracyScore = ((1f - meanForwardDist * 2.2f) * 75f).roundToInt().coerceIn(15, 65),
                feedbackType = StrokeFeedbackType.OFF_PATH,
                feedbackMessage = "Slightly off path — follow the stroke shape ${directionLabel(targetMedian)}."
            )
        }

        val lengthRatio = (minOf(userLen, targetLen) / maxOf(userLen, targetLen).coerceAtLeast(0.01f))
        val rawScore = (100f - (meanForwardDist * 240f) - ((1f - lengthRatio) * 18f))
            .roundToInt()
            .coerceIn(72, 100)

        return if (rawScore >= 88) {
            StrokeEvaluationResult(
                isAccepted = true,
                accuracyScore = rawScore,
                feedbackType = StrokeFeedbackType.PERFECT,
                feedbackMessage = "Crisp stroke! ($rawScore%)"
            )
        } else {
            StrokeEvaluationResult(
                isAccepted = true,
                accuracyScore = rawScore,
                feedbackType = StrokeFeedbackType.GOOD,
                feedbackMessage = "Good stroke! ($rawScore%)"
            )
        }
    }
}
