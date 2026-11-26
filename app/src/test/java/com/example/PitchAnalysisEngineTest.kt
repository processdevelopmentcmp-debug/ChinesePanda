package com.example

import com.example.audio.PitchAnalysisEngine
import com.example.audio.PitchFrame
import com.example.audio.TargetSyllableSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.sin

class PitchAnalysisEngineTest {

    private fun generateSinePcm(
        freqHz: Float,
        durationSec: Float = 0.25f,
        sampleRate: Int = 16000,
        amplitude: Float = 0.6f
    ): ShortArray {
        val count = (durationSec * sampleRate).toInt()
        return ShortArray(count) { i ->
            val sample = sin(2.0 * PI * freqHz * i / sampleRate) * amplitude * Short.MAX_VALUE
            sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun generateChirpPcm(
        startHz: Float,
        endHz: Float,
        durationSec: Float = 0.6f,
        sampleRate: Int = 16000
    ): ShortArray {
        val count = (durationSec * sampleRate).toInt()
        var phase = 0.0
        return ShortArray(count) { i ->
            val t = i.toDouble() / count
            val instHz = startHz + (endHz - startHz) * t
            phase += 2.0 * PI * instHz / sampleRate
            (sin(phase) * 0.65 * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun encodeMonoWav16(samples: ShortArray, sampleRate: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val dataSize = samples.size * 2
        val chunkSize = 36 + dataSize

        fun writeStr(s: String) = out.write(s.toByteArray(Charsets.US_ASCII))
        fun writeLeInt(v: Int) {
            out.write(v and 0xFF)
            out.write((v shr 8) and 0xFF)
            out.write((v shr 16) and 0xFF)
            out.write((v shr 24) and 0xFF)
        }
        fun writeLeShort(v: Int) {
            out.write(v and 0xFF)
            out.write((v shr 8) and 0xFF)
        }

        writeStr("RIFF")
        writeLeInt(chunkSize)
        writeStr("WAVE")
        writeStr("fmt ")
        writeLeInt(16) // PCM fmt chunk size
        writeLeShort(1) // PCM format
        writeLeShort(1) // Mono
        writeLeInt(sampleRate)
        writeLeInt(sampleRate * 2) // Byte rate
        writeLeShort(2) // Block align
        writeLeShort(16) // Bits per sample
        writeStr("data")
        writeLeInt(dataSize)
        for (s in samples) {
            writeLeShort(s.toInt())
        }
        return out.toByteArray()
    }

    @Test
    fun estimatePitchYin_accuratelyDetects220HzSineWave() {
        val pcm = generateSinePcm(freqHz = 220f, durationSec = 0.1f, sampleRate = 16000)
        val window = pcm.copyOf(1024)
        val frame = PitchAnalysisEngine.estimatePitchYin(window, sampleRate = 16000)

        assertTrue("Expected voiced frame", frame.isVoiced)
        assertEquals(220f, frame.frequencyHz, 3.0f)
        assertTrue("Expected high confidence, got ${frame.confidence}", frame.confidence >= 0.90f)
    }

    @Test
    fun estimatePitchYin_marksSilenceAsUnvoiced() {
        val silence = ShortArray(1024) { 0 }
        val frame = PitchAnalysisEngine.estimatePitchYin(silence, sampleRate = 16000)
        assertFalse(frame.isVoiced)
        assertEquals(0f, frame.frequencyHz, 0.01f)
    }

    @Test
    fun decodeWavPcm16_roundTripsSyntheticWavFile() {
        val original = generateSinePcm(freqHz = 180f, durationSec = 0.12f, sampleRate = 16000)
        val wavBytes = encodeMonoWav16(original, 16000)

        val decoded = PitchAnalysisEngine.decodeWavPcm16(wavBytes)
        assertNotNull(decoded)
        val (pcm, sr) = decoded!!
        assertEquals(16000, sr)
        assertEquals(original.size, pcm.size)
        assertEquals(original[100], pcm[100])
        assertEquals(original[500], pcm[500])
    }

    @Test
    fun extractToneNumber_parsesMandarinToneMarksAndDigits() {
        assertEquals(1, PitchAnalysisEngine.extractToneNumber("mā"))
        assertEquals(2, PitchAnalysisEngine.extractToneNumber("má"))
        assertEquals(3, PitchAnalysisEngine.extractToneNumber("mǎ"))
        assertEquals(4, PitchAnalysisEngine.extractToneNumber("mà"))
        assertEquals(5, PitchAnalysisEngine.extractToneNumber("ma"))
        assertEquals(3, PitchAnalysisEngine.extractToneNumber("lǚ"))
        assertEquals(2, PitchAnalysisEngine.extractToneNumber("guo2"))
    }

    @Test
    fun comparePronunciation_scoresRisingTone2HigherThanFallingMistake() {
        val syllables = listOf(TargetSyllableSpec(hanzi = "麻", pinyin = "má", expectedTone = 2))

        val risingPcm = generateChirpPcm(startHz = 150f, endHz = 270f, durationSec = 0.55f)
        val risingFrames = PitchAnalysisEngine.analyzePcmBuffer(risingPcm, sampleRate = 16000)
        val goodReport = PitchAnalysisEngine.comparePronunciation(syllables, risingFrames)

        val fallingPcm = generateChirpPcm(startHz = 270f, endHz = 140f, durationSec = 0.55f)
        val fallingFrames = PitchAnalysisEngine.analyzePcmBuffer(fallingPcm, sampleRate = 16000)
        val wrongReport = PitchAnalysisEngine.comparePronunciation(syllables, fallingFrames)

        assertTrue("Expected rising Tone 2 score >= 75, got ${goodReport.overallScore}", goodReport.overallScore >= 75)
        assertTrue(
            "Expected matching Tone 2 (${goodReport.overallScore}) to beat falling mistake (${wrongReport.overallScore})",
            goodReport.overallScore > wrongReport.overallScore + 20
        )
        assertEquals("Rising ↗", goodReport.syllableFeedback.first().detectedPattern)
        assertEquals("Falling ↘", wrongReport.syllableFeedback.first().detectedPattern)
    }

    @Test
    fun comparePronunciation_handlesQuietEmptyFramesGracefully() {
        val syllables = listOf(TargetSyllableSpec("你", "nǐ", 3))
        val emptyFrames = List(10) { PitchFrame(it * 20L, 0f, 0f, 0.001f, false) }
        val report = PitchAnalysisEngine.comparePronunciation(syllables, emptyFrames)
        assertEquals(0, report.overallScore)
        assertTrue(report.summaryTip.isNotBlank())
    }
}
