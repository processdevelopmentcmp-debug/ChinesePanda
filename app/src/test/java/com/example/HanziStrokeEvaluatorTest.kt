package com.example

import com.example.model.HanziPoint
import com.example.model.HanziStrokeEvaluator
import com.example.model.HanziStrokeParser
import com.example.model.StrokeFeedbackType
import com.example.model.SvgPathCmd
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HanziStrokeEvaluatorTest {

    @Test
    fun parseHanziWriterJson_parsesStrokesAndNormalizesMedians() {
        val sampleJson = """
            {
              "strokes": [
                "M 150 450 L 850 450 Q 870 430 850 410 Z"
              ],
              "medians": [
                [[150, 450], [500, 450], [850, 450]]
              ]
            }
        """.trimIndent()

        val parsed = HanziStrokeParser.parseHanziWriterJson("一", sampleJson)
        assertNotNull(parsed)
        assertEquals("一", parsed!!.character)
        assertEquals(1, parsed.strokeCount)
        assertEquals(1, parsed.svgStrokePaths.size)
        assertEquals(3, parsed.medians[0].size)

        // Check normalized bounds [0..1] and Y-flip (900 -> 0, -124 -> 1)
        val first = parsed.medians[0].first()
        val last = parsed.medians[0].last()
        assertTrue(first.x in 0.1f..0.2f)
        assertTrue(last.x in 0.8f..0.9f)
        assertEquals(first.y, last.y, 0.001f)
    }

    @Test
    fun parseSvgPathToNormalizedCommands_handlesMoveLineQuadClose() {
        val cmds = HanziStrokeParser.parseSvgPathToNormalizedCommands(
            "M 100 900 L 900 900 Q 950 800 900 700 Z"
        )
        assertEquals(4, cmds.size)
        assertTrue(cmds[0] is SvgPathCmd.MoveTo)
        assertTrue(cmds[1] is SvgPathCmd.LineTo)
        assertTrue(cmds[2] is SvgPathCmd.QuadTo)
        assertTrue(cmds[3] is SvgPathCmd.Close)

        val move = cmds[0] as SvgPathCmd.MoveTo
        assertEquals(100f / 1024f, move.x, 0.002f)
        assertEquals(0f, move.y, 0.002f)
    }

    @Test
    fun evaluateStroke_acceptsAccurateForwardStrokeAsPerfect() {
        val target = listOf(
            HanziPoint(0.15f, 0.50f),
            HanziPoint(0.50f, 0.50f),
            HanziPoint(0.85f, 0.50f)
        )
        val userStroke = listOf(
            HanziPoint(0.16f, 0.51f),
            HanziPoint(0.35f, 0.50f),
            HanziPoint(0.52f, 0.49f),
            HanziPoint(0.70f, 0.50f),
            HanziPoint(0.84f, 0.51f)
        )

        val result = HanziStrokeEvaluator.evaluateStroke(userStroke, target, lenient = false)
        assertTrue("Expected accurate stroke to be accepted", result.isAccepted)
        assertEquals(StrokeFeedbackType.PERFECT, result.feedbackType)
        assertTrue("Expected score >= 88, got ${result.accuracyScore}", result.accuracyScore >= 88)
    }

    @Test
    fun evaluateStroke_rejectsReversedStrokeDirection() {
        val target = listOf(
            HanziPoint(0.15f, 0.50f),
            HanziPoint(0.85f, 0.50f)
        )
        val reversedUser = listOf(
            HanziPoint(0.84f, 0.50f),
            HanziPoint(0.50f, 0.50f),
            HanziPoint(0.16f, 0.50f)
        )

        val result = HanziStrokeEvaluator.evaluateStroke(reversedUser, target, lenient = false)
        assertFalse(result.isAccepted)
        assertEquals(StrokeFeedbackType.WRONG_DIRECTION, result.feedbackType)
    }

    @Test
    fun evaluateStroke_rejectsWrongStartPoint() {
        val target = listOf(
            HanziPoint(0.50f, 0.15f),
            HanziPoint(0.50f, 0.85f)
        )
        val farStartUser = listOf(
            HanziPoint(0.20f, 0.15f),
            HanziPoint(0.20f, 0.85f)
        )

        val result = HanziStrokeEvaluator.evaluateStroke(farStartUser, target, lenient = false)
        assertFalse(result.isAccepted)
        assertEquals(StrokeFeedbackType.WRONG_START, result.feedbackType)
    }

    @Test
    fun evaluateStroke_rejectsTooShortTap() {
        val target = listOf(
            HanziPoint(0.15f, 0.50f),
            HanziPoint(0.85f, 0.50f)
        )
        val tinyTap = listOf(
            HanziPoint(0.15f, 0.50f),
            HanziPoint(0.17f, 0.50f)
        )

        val result = HanziStrokeEvaluator.evaluateStroke(tinyTap, target, lenient = false)
        assertFalse(result.isAccepted)
        assertEquals(StrokeFeedbackType.TOO_SHORT, result.feedbackType)
    }

    @Test
    fun evaluateStroke_rejectsOffPathStroke() {
        val target = listOf(
            HanziPoint(0.20f, 0.20f),
            HanziPoint(0.80f, 0.20f)
        )
        // Starts near the right start, goes roughly right, then veers far down off the path
        val offPathUser = listOf(
            HanziPoint(0.22f, 0.22f),
            HanziPoint(0.35f, 0.70f),
            HanziPoint(0.65f, 0.85f),
            HanziPoint(0.80f, 0.55f)
        )

        val result = HanziStrokeEvaluator.evaluateStroke(offPathUser, target, lenient = false)
        assertFalse(result.isAccepted)
        assertEquals(StrokeFeedbackType.OFF_PATH, result.feedbackType)
    }
}
