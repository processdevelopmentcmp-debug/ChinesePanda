package com.example.audio

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PitchFrame(
    val timeMs: Long,
    val frequencyHz: Float,
    val confidence: Float,
    val rmsLevel: Float,
    val isVoiced: Boolean
)

/**
 * Normalized point on the Chao 5-level Mandarin pitch chart.
 * [t] is normalized time [0f..1f]; [pitchLevel] is normalized pitch height [0f..1f] (0.10 = Tone 1-low, 0.90 = Tone 5-high).
 */
data class NormalizedPitchPoint(
    val t: Float,
    val pitchLevel: Float,
    val frequencyHz: Float = 0f,
    val isVoiced: Boolean = true
)

data class TargetSyllableSpec(
    val hanzi: String,
    val pinyin: String,
    val expectedTone: Int // 1..5
)

data class SyllablePitchFeedback(
    val index: Int,
    val hanzi: String,
    val pinyin: String,
    val expectedTone: Int,
    val score: Int,
    val detectedPattern: String,
    val tip: String
)

data class PronunciationPitchReport(
    val overallScore: Int,
    val contourAccuracy: Int,
    val toneRangeScore: Int,
    val syllableAccuracy: Int,
    val userContour: List<NormalizedPitchPoint>,
    val nativeContour: List<NormalizedPitchPoint>,
    val usedRealNativeWav: Boolean,
    val userMeanHz: Float,
    val nativeMeanHz: Float,
    val syllableFeedback: List<SyllablePitchFeedback>,
    val summaryTip: String
)

/**
 * Pure-Kotlin real-time DSP pitch detector (YIN algorithm), WAV PCM decoder,
 * Mandarin Chao 5-level tone contour generator, and acoustic pronunciation comparator.
 */
object PitchAnalysisEngine {

    const val DEFAULT_SAMPLE_RATE = 16000
    const val MIN_VOCAL_HZ = 75f
    const val MAX_VOCAL_HZ = 520f
    private const val YIN_THRESHOLD = 0.20f
    private const val MIN_VOICED_RMS = 0.015f

    /**
     * Computes normalized RMS energy [0f..1f] for a 16-bit PCM frame.
     */
    fun computeRms(samples: ShortArray, offset: Int = 0, length: Int = samples.size - offset): Float {
        if (length <= 0 || offset < 0 || offset + length > samples.size) return 0f
        var sumSq = 0.0
        for (i in offset until offset + length) {
            val norm = samples[i] / 32768.0
            sumSq += norm * norm
        }
        return sqrt(sumSq / length).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Estimates fundamental frequency (F0) of a mono PCM frame using the YIN algorithm
     * with Cumulative Mean Normalized Difference Function (CMNDF) and parabolic interpolation.
     */
    fun estimatePitchYin(
        samples: ShortArray,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        timeMs: Long = 0L,
        minHz: Float = MIN_VOCAL_HZ,
        maxHz: Float = MAX_VOCAL_HZ
    ): PitchFrame {
        val rms = computeRms(samples)
        if (samples.size < 256 || rms < MIN_VOICED_RMS) {
            return PitchFrame(
                timeMs = timeMs,
                frequencyHz = 0f,
                confidence = 0f,
                rmsLevel = rms,
                isVoiced = false
            )
        }

        val halfSize = samples.size / 2
        val minTau = max(2, (sampleRate / maxHz).toInt())
        val maxTau = min(halfSize - 2, (sampleRate / minHz).toInt())
        if (maxTau <= minTau + 2) {
            return PitchFrame(timeMs, 0f, 0f, rms, false)
        }

        // Remove DC offset for clean difference calculation
        var mean = 0.0
        for (s in samples) mean += s
        mean /= samples.size
        val signal = FloatArray(samples.size) { i -> ((samples[i] - mean) / 32768.0).toFloat() }

        // Step 1: Difference function d(tau)
        val diff = FloatArray(maxTau + 2)
        for (tau in 1..maxTau + 1) {
            var sum = 0f
            for (j in 0 until halfSize) {
                val delta = signal[j] - signal[j + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // Step 2: Cumulative Mean Normalized Difference Function (CMNDF)
        val cmndf = FloatArray(maxTau + 2)
        cmndf[0] = 1f
        var runningSum = 0f
        for (tau in 1..maxTau + 1) {
            runningSum += diff[tau]
            cmndf[tau] = if (runningSum > 1e-9f) diff[tau] * tau / runningSum else 1f
        }

        // Step 3: Absolute threshold search for first local minimum below YIN_THRESHOLD
        var bestTau = -1
        var tau = minTau
        while (tau <= maxTau) {
            if (cmndf[tau] < YIN_THRESHOLD) {
                while (tau + 1 <= maxTau && cmndf[tau + 1] < cmndf[tau]) {
                    tau++
                }
                bestTau = tau
                break
            }
            tau++
        }

        // Fallback: global minimum if moderately periodic (helps softer Mandarin tone-3 creaky voice)
        if (bestTau == -1) {
            var minVal = Float.MAX_VALUE
            var minIdx = -1
            for (t in minTau..maxTau) {
                if (cmndf[t] < minVal) {
                    minVal = cmndf[t]
                    minIdx = t
                }
            }
            if (minIdx != -1 && minVal < 0.34f) {
                bestTau = minIdx
            }
        }

        if (bestTau <= 0) {
            return PitchFrame(timeMs, 0f, 0f, rms, false)
        }

        // Step 4: Parabolic interpolation around bestTau
        val refinedTau = if (bestTau in 1 until maxTau) {
            val s0 = cmndf[bestTau - 1]
            val s1 = cmndf[bestTau]
            val s2 = cmndf[bestTau + 1]
            val denom = 2f * (2f * s1 - s2 - s0)
            if (abs(denom) > 1e-6f) {
                bestTau + (s2 - s0) / denom
            } else {
                bestTau.toFloat()
            }
        } else {
            bestTau.toFloat()
        }

        val freq = if (refinedTau > 0f) sampleRate / refinedTau else 0f
        val confidence = (1f - cmndf[bestTau]).coerceIn(0f, 1f)
        val valid = freq in minHz..maxHz && confidence >= 0.64f

        return PitchFrame(
            timeMs = timeMs,
            frequencyHz = if (valid) freq else 0f,
            confidence = if (valid) confidence else 0f,
            rmsLevel = rms,
            isVoiced = valid
        )
    }

    /**
     * Slides a YIN pitch window across a full 16-bit mono PCM buffer.
     */
    fun analyzePcmBuffer(
        samples: ShortArray,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        windowSize: Int = 1024,
        hopSize: Int = 320
    ): List<PitchFrame> {
        if (samples.size < windowSize) return emptyList()
        val frames = mutableListOf<PitchFrame>()
        val window = ShortArray(windowSize)
        var offset = 0
        while (offset + windowSize <= samples.size) {
            System.arraycopy(samples, offset, window, 0, windowSize)
            val timeMs = (offset.toLong() * 1000L) / sampleRate
            frames.add(estimatePitchYin(window, sampleRate, timeMs))
            offset += hopSize
        }
        return frames
    }

    /**
     * Decodes a standard uncompressed 16-bit PCM WAV file (mono or stereo -> mono) into samples + sampleRate.
     */
    fun decodeWavPcm16(wavBytes: ByteArray): Pair<ShortArray, Int>? {
        if (wavBytes.size < 44) return null
        if (wavBytes[0].toInt().toChar() != 'R' ||
            wavBytes[1].toInt().toChar() != 'I' ||
            wavBytes[2].toInt().toChar() != 'F' ||
            wavBytes[3].toInt().toChar() != 'F'
        ) return null

        var pos = 12
        var channels = 1
        var sampleRate = DEFAULT_SAMPLE_RATE
        var bitsPerSample = 16
        var dataStart = -1
        var dataLength = 0

        while (pos + 8 <= wavBytes.size) {
            val chunkId = String(wavBytes, pos, 4, Charsets.US_ASCII)
            val chunkSize = readLeInt(wavBytes, pos + 4)
            val contentStart = pos + 8
            if (chunkSize < 0 || contentStart > wavBytes.size) break

            when (chunkId) {
                "fmt " -> {
                    if (contentStart + 16 <= wavBytes.size) {
                        val audioFormat = readLeShort(wavBytes, contentStart)
                        if (audioFormat != 1) return null // PCM only
                        channels = readLeShort(wavBytes, contentStart + 2).coerceAtLeast(1)
                        sampleRate = readLeInt(wavBytes, contentStart + 4).coerceAtLeast(8000)
                        bitsPerSample = readLeShort(wavBytes, contentStart + 14)
                    }
                }
                "data" -> {
                    dataStart = contentStart
                    dataLength = min(chunkSize, wavBytes.size - contentStart)
                    break
                }
            }
            val paddedSize = chunkSize + (chunkSize % 2)
            pos = contentStart + paddedSize
        }

        if (dataStart < 0 || dataLength < 4 || bitsPerSample != 16) return null
        val bytesPerFrame = channels * 2
        val frameCount = dataLength / bytesPerFrame
        if (frameCount <= 0) return null

        val mono = ShortArray(frameCount)
        for (i in 0 until frameCount) {
            var acc = 0
            val base = dataStart + i * bytesPerFrame
            for (ch in 0 until channels) {
                acc += readLeSignedShort(wavBytes, base + ch * 2)
            }
            mono[i] = (acc / channels).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return mono to sampleRate
    }

    /**
     * Converts raw pitch frames into a trimmed, median-smoothed, speaker-independent
     * normalized contour [0f..1f] on the Chao 5-level pitch scale.
     */
    fun normalizePitchTrack(
        frames: List<PitchFrame>,
        targetPointCount: Int = 64
    ): Pair<List<NormalizedPitchPoint>, Float> {
        val voicedIndices = frames.indices.filter { frames[it].isVoiced && frames[it].frequencyHz > 0f }
        if (voicedIndices.size < 3) return emptyList<NormalizedPitchPoint>() to 0f

        val startIdx = voicedIndices.first()
        val endIdx = voicedIndices.last()
        val trimmed = frames.subList(startIdx, endIdx + 1)

        // Convert voiced frequencies to semitones relative to 100 Hz
        val voicedHz = trimmed.filter { it.isVoiced && it.frequencyHz > 0f }.map { it.frequencyHz }
        val sortedHz = voicedHz.sorted()
        val medianHz = sortedHz[sortedHz.size / 2]
        val meanHz = voicedHz.average().toFloat()

        val rawSemitones = trimmed.map { frame ->
            if (frame.isVoiced && frame.frequencyHz > 0f) {
                hzToSemitones(frame.frequencyHz, medianHz)
            } else {
                Float.NaN
            }
        }

        // 3-point median filter on voiced frames to eliminate octave-jump artifacts
        val smoothed = FloatArray(rawSemitones.size) { i ->
            val cur = rawSemitones[i]
            if (cur.isNaN()) {
                Float.NaN
            } else {
                val neighbors = mutableListOf(cur)
                if (i > 0 && !rawSemitones[i - 1].isNaN()) neighbors.add(rawSemitones[i - 1])
                if (i + 1 < rawSemitones.size && !rawSemitones[i + 1].isNaN()) neighbors.add(rawSemitones[i + 1])
                neighbors.sort()
                neighbors[neighbors.size / 2]
            }
        }

        // Fill brief unvoiced consonant gaps (up to 4 frames) linearly for smooth visualization
        val interpolated = smoothed.copyOf()
        var i = 0
        while (i < interpolated.size) {
            if (interpolated[i].isNaN()) {
                val gapStart = i
                while (i < interpolated.size && interpolated[i].isNaN()) i++
                val gapEnd = i - 1
                val gapLen = gapEnd - gapStart + 1
                if (gapLen <= 4 && gapStart > 0 && gapEnd + 1 < interpolated.size) {
                    val left = interpolated[gapStart - 1]
                    val right = interpolated[gapEnd + 1]
                    for (k in gapStart..gapEnd) {
                        val frac = (k - gapStart + 1).toFloat() / (gapLen + 1).toFloat()
                        interpolated[k] = left + (right - left) * frac
                    }
                }
            } else {
                i++
            }
        }

        val validSt = interpolated.filter { !it.isNaN() }.sorted()
        if (validSt.isEmpty()) return emptyList<NormalizedPitchPoint>() to meanHz

        // Use a grounded dynamic range (at least 7 semitones span) so flat Tone 1 doesn't blow up noise into huge swings
        val p10 = validSt[(validSt.size * 0.10f).toInt().coerceIn(0, validSt.lastIndex)]
        val p90 = validSt[(validSt.size * 0.90f).toInt().coerceIn(0, validSt.lastIndex)]
        val observedSpan = p90 - p10
        val effectiveSpan = max(7.5f, observedSpan * 1.15f)
        val centerSt = (p10 + p90) / 2f

        val resampled = List(targetPointCount) { idx ->
            val t = if (targetPointCount > 1) idx.toFloat() / (targetPointCount - 1) else 0f
            val srcPos = t * (interpolated.size - 1)
            val lo = srcPos.toInt().coerceIn(0, interpolated.lastIndex)
            val hi = (lo + 1).coerceAtMost(interpolated.lastIndex)
            val frac = srcPos - lo
            val vLo = interpolated[lo]
            val vHi = interpolated[hi]
            val stVal = when {
                !vLo.isNaN() && !vHi.isNaN() -> vLo + (vHi - vLo) * frac
                !vLo.isNaN() -> vLo
                !vHi.isNaN() -> vHi
                else -> Float.NaN
            }
            if (stVal.isNaN()) {
                NormalizedPitchPoint(t = t, pitchLevel = 0.5f, frequencyHz = 0f, isVoiced = false)
            } else {
                val normalized = (0.5f + ((stVal - centerSt) / effectiveSpan) * 0.78f).coerceIn(0.08f, 0.92f)
                val approxHz = (medianHz * Math.pow(2.0, stVal / 12.0)).toFloat()
                NormalizedPitchPoint(t = t, pitchLevel = normalized, frequencyHz = approxHz, isVoiced = true)
            }
        }

        return resampled to meanHz
    }

    /**
     * Converts a single live pitch reading into a [0f..1f] vertical chart coordinate relative to the speaker's running median Hz.
     */
    fun mapLiveHzToPitchLevel(hz: Float, baselineHz: Float): Float {
        if (hz <= 0f || baselineHz <= 0f) return 0.5f
        val st = hzToSemitones(hz, baselineHz)
        return (0.5f + (st / 9.5f) * 0.78f).coerceIn(0.08f, 0.92f)
    }

    /**
     * Extracts Mandarin tone number (1..5) from a pinyin syllable with tone marks or trailing digits.
     */
    fun extractToneNumber(pinyinSyllable: String): Int {
        val s = pinyinSyllable.trim().lowercase()
        if (s.isEmpty()) return 5
        val lastChar = s.last()
        if (lastChar in '1'..'5') return lastChar - '0'
        for (ch in s) {
            when (ch) {
                'ā', 'ē', 'ī', 'ō', 'ū', 'ǖ' -> return 1
                'á', 'é', 'í', 'ó', 'ú', 'ǘ' -> return 2
                'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ' -> return 3
                'à', 'è', 'ì', 'ò', 'ù', 'ǜ' -> return 4
            }
        }
        return 5
    }

    /**
     * Parses a multi-syllable word/phrase into TargetSyllableSpecs (Hanzi + Pinyin + Tone).
     */
    fun parseSyllablesFromWord(hanzi: String, pinyin: String): List<TargetSyllableSpec> {
        val cleanHanzi = hanzi.filter { !it.isWhitespace() && it != '，' && it != '。' && it != '！' && it != '？' }
            .map { it.toString() }
        val cleanPinyin = pinyin.trim()
            .replace(",", " ")
            .replace(".", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (cleanHanzi.isEmpty()) return emptyList()
        return cleanHanzi.mapIndexed { idx, ch ->
            val py = cleanPinyin.getOrElse(idx) { cleanPinyin.lastOrNull().orEmpty() }
            TargetSyllableSpec(
                hanzi = ch,
                pinyin = py,
                expectedTone = extractToneNumber(py)
            )
        }
    }

    /**
     * Generates a canonical Chao 5-level Mandarin pitch contour for a sequence of tones (1..5),
     * modeling natural half-third sandhi when a Tone 3 is followed by another syllable.
     */
    fun buildCanonicalToneContour(
        tones: List<Int>,
        totalPoints: Int = 64
    ): List<NormalizedPitchPoint> {
        val safeTones = tones.ifEmpty { listOf(1) }
        val count = safeTones.size
        return List(totalPoints) { idx ->
            val t = if (totalPoints > 1) idx.toFloat() / (totalPoints - 1) else 0f
            val scaled = t * count
            val sylIdx = scaled.toInt().coerceIn(0, count - 1)
            val localT = (scaled - sylIdx).coerceIn(0f, 1f)
            val tone = safeTones[sylIdx]
            val isFinal = sylIdx == count - 1

            val level = when (tone) {
                1 -> 0.84f // 55 High level
                2 -> 0.42f + 0.44f * smoothStep(localT) // 35 Mid-to-high rising
                3 -> if (isFinal) {
                    // 214 Low dipping with modest tail rise
                    if (localT < 0.55f) {
                        0.36f - 0.22f * smoothStep(localT / 0.55f)
                    } else {
                        0.14f + 0.30f * smoothStep((localT - 0.55f) / 0.45f)
                    }
                } else {
                    // Half-third (21) when non-final in connected Mandarin speech
                    0.36f - 0.22f * smoothStep(localT)
                }
                4 -> 0.88f - 0.72f * smoothStep(localT) // 51 High-to-low sharp fall
                else -> 0.48f - 0.08f * localT // Neutral tone
            }

            NormalizedPitchPoint(
                t = t,
                pitchLevel = level.coerceIn(0.08f, 0.92f),
                frequencyHz = 0f,
                isVoiced = true
            )
        }
    }

    /**
     * Compares the learner's recorded pitch contour against the native reference contour
     * (either extracted directly from a native TTS WAV recording or canonical Chao tones)
     * and generates overall + per-syllable feedback.
     */
    fun comparePronunciation(
        syllables: List<TargetSyllableSpec>,
        userFrames: List<PitchFrame>,
        nativeWavContour: List<NormalizedPitchPoint>? = null,
        nativeMeanHz: Float = 0f
    ): PronunciationPitchReport {
        val canonicalContour = buildCanonicalToneContour(syllables.map { it.expectedTone }, totalPoints = 64)
        val hasRealNative = nativeWavContour != null && nativeWavContour.count { it.isVoiced } >= 16
        val referenceContour = if (hasRealNative) nativeWavContour!! else canonicalContour

        val (userContour, userMeanHz) = normalizePitchTrack(userFrames, targetPointCount = 64)

        if (userContour.count { it.isVoiced } < 6) {
            return PronunciationPitchReport(
                overallScore = 0,
                contourAccuracy = 0,
                toneRangeScore = 0,
                syllableAccuracy = 0,
                userContour = userContour,
                nativeContour = referenceContour,
                usedRealNativeWav = hasRealNative,
                userMeanHz = userMeanHz,
                nativeMeanHz = nativeMeanHz,
                syllableFeedback = syllables.mapIndexed { idx, s ->
                    SyllablePitchFeedback(
                        index = idx,
                        hanzi = s.hanzi,
                        pinyin = s.pinyin,
                        expectedTone = s.expectedTone,
                        score = 0,
                        detectedPattern = "Too quiet",
                        tip = "Speak closer to the microphone with steady volume."
                    )
                },
                summaryTip = "We couldn't detect enough voiced speech. Speak a little louder and closer to the mic!"
            )
        }

        // 1. Pairwise MAE & Pearson shape correlation across voiced points
        val uVals = mutableListOf<Float>()
        val rVals = mutableListOf<Float>()
        var maeSum = 0f
        for (i in referenceContour.indices) {
            val u = userContour[i]
            val r = referenceContour[i]
            if (u.isVoiced && r.isVoiced) {
                uVals.add(u.pitchLevel)
                rVals.add(r.pitchLevel)
                maeSum += abs(u.pitchLevel - r.pitchLevel)
            }
        }
        val mae = if (uVals.isNotEmpty()) maeSum / uVals.size else 0.5f
        val maeScore = ((1f - (mae / 0.42f)) * 100f).coerceIn(0f, 100f)

        val corr = computePearsonCorrelation(uVals, rVals)
        // Map correlation [-1..1] -> [0..100], blending with MAE so flat Tone 1 (low variance) still scores accurately
        val refStd = computeStdDev(rVals)
        val contourAccuracy = if (refStd < 0.06f) {
            // Flat target (e.g., all Tone 1): reward low error & low variance
            val uStd = computeStdDev(uVals)
            val flatnessBonus = ((1f - (uStd / 0.25f)) * 100f).coerceIn(0f, 100f)
            (maeScore * 0.65f + flatnessBonus * 0.35f).roundToInt().coerceIn(0, 100)
        } else {
            val corrScore = (((corr + 1f) / 2f) * 100f).coerceIn(0f, 100f)
            (maeScore * 0.55f + corrScore * 0.45f).roundToInt().coerceIn(0, 100)
        }

        // 2. Tone pitch range expressiveness
        val uMin = uVals.minOrNull() ?: 0.5f
        val uMax = uVals.maxOrNull() ?: 0.5f
        val rMin = rVals.minOrNull() ?: 0.5f
        val rMax = rVals.maxOrNull() ?: 0.5f
        val uSpan = uMax - uMin
        val rSpan = max(0.08f, rMax - rMin)
        val rangeRatio = min(uSpan, rSpan) / max(uSpan, rSpan).coerceAtLeast(0.08f)
        val toneRangeScore = (rangeRatio * 100f).roundToInt().coerceIn(25, 100)

        // 3. Per-syllable directional & shape diagnostics
        val safeSyllables = syllables.ifEmpty { listOf(TargetSyllableSpec("字", "zì", 4)) }
        val sylFeedback = safeSyllables.mapIndexed { idx, spec ->
            evaluateSyllableSegment(
                index = idx,
                totalSyllables = safeSyllables.size,
                spec = spec,
                userContour = userContour,
                referenceContour = referenceContour
            )
        }
        val syllableAccuracy = if (sylFeedback.isNotEmpty()) {
            sylFeedback.map { it.score }.average().roundToInt()
        } else contourAccuracy

        val overallScore = (contourAccuracy * 0.45f + syllableAccuracy * 0.40f + toneRangeScore * 0.15f)
            .roundToInt()
            .coerceIn(0, 100)

        val weakest = sylFeedback.minByOrNull { it.score }
        val summaryTip = when {
            overallScore >= 85 -> "Super crisp Mandarin pitch! Your contour closely tracks the native speaker."
            overallScore >= 70 && weakest != null ->
                "Good flow! Polish 「${weakest.hanzi} (${weakest.pinyin})」: ${weakest.tip}"
            weakest != null ->
                "Focus on 「${weakest.hanzi} (${weakest.pinyin})」: ${weakest.tip}"
            else -> "Listen to the native curve and exaggerate your high-to-low pitch contrast."
        }

        return PronunciationPitchReport(
            overallScore = overallScore,
            contourAccuracy = contourAccuracy,
            toneRangeScore = toneRangeScore,
            syllableAccuracy = syllableAccuracy,
            userContour = userContour,
            nativeContour = referenceContour,
            usedRealNativeWav = hasRealNative,
            userMeanHz = userMeanHz,
            nativeMeanHz = nativeMeanHz,
            syllableFeedback = sylFeedback,
            summaryTip = summaryTip
        )
    }

    private fun evaluateSyllableSegment(
        index: Int,
        totalSyllables: Int,
        spec: TargetSyllableSpec,
        userContour: List<NormalizedPitchPoint>,
        referenceContour: List<NormalizedPitchPoint>
    ): SyllablePitchFeedback {
        val n = userContour.size
        val start = ((index.toFloat() / totalSyllables) * n).toInt().coerceIn(0, n - 1)
        val end = (((index + 1).toFloat() / totalSyllables) * n).toInt().coerceIn(start + 1, n)

        val segUser = userContour.subList(start, end).filter { it.isVoiced }
        val segRef = referenceContour.subList(start, end).filter { it.isVoiced }
        if (segUser.size < 2) {
            return SyllablePitchFeedback(
                index = index,
                hanzi = spec.hanzi,
                pinyin = spec.pinyin,
                expectedTone = spec.expectedTone,
                score = 40,
                detectedPattern = "Weak voice",
                tip = "Hold the vowel slightly longer so the tone is clear."
            )
        }

        val firstThird = segUser.take(max(1, segUser.size / 3)).map { it.pitchLevel }.average().toFloat()
        val midThird = segUser.subList(segUser.size / 3, max(segUser.size / 3 + 1, (segUser.size * 2) / 3))
            .map { it.pitchLevel }.average().toFloat()
        val lastThird = segUser.takeLast(max(1, segUser.size / 3)).map { it.pitchLevel }.average().toFloat()
        val slope = lastThird - firstThird
        val meanLevel = segUser.map { it.pitchLevel }.average().toFloat()

        val detectedPattern = when {
            midThird < firstThird - 0.08f && lastThird > midThird + 0.06f -> "Dipping ↘↗"
            slope > 0.14f -> "Rising ↗"
            slope < -0.14f -> "Falling ↘"
            meanLevel >= 0.62f -> "High flat →"
            meanLevel <= 0.36f -> "Low flat →"
            else -> "Mid flat →"
        }

        val segMae = if (segRef.isNotEmpty()) {
            val minLen = min(segUser.size, segRef.size)
            var sum = 0f
            for (k in 0 until minLen) {
                sum += abs(segUser[k].pitchLevel - segRef[k].pitchLevel)
            }
            sum / minLen
        } else 0.25f

        val baseMaeScore = ((1f - segMae / 0.45f) * 100f).coerceIn(20f, 100f)

        val (shapeScore, tip) = when (spec.expectedTone) {
            1 -> {
                val flatPenalty = abs(slope) * 140f
                val highBonus = if (meanLevel >= 0.60f) 100f else (meanLevel / 0.60f) * 90f
                val s = (highBonus - flatPenalty).coerceIn(20f, 100f)
                val t = when {
                    s >= 78f -> "Steady high Tone 1!"
                    meanLevel < 0.56f -> "Start higher and keep it steady like singing a high note."
                    else -> "Keep Tone 1 level without dropping at the end."
                }
                s to t
            }
            2 -> {
                val s = when {
                    slope >= 0.16f -> (82f + min(18f, (slope - 0.16f) * 80f))
                    slope >= 0.05f -> 60f + (slope / 0.16f) * 22f
                    else -> (45f + slope * 80f).coerceIn(15f, 55f)
                }
                val t = if (s >= 76f) "Crisp rising Tone 2!" else "Lift your pitch upward like asking 'What?'."
                s to t
            }
            3 -> {
                val minLevel = segUser.minOf { it.pitchLevel }
                val dipDepth = firstThird - minLevel
                val s = when {
                    minLevel <= 0.34f && dipDepth >= 0.08f -> 92f
                    minLevel <= 0.42f -> 76f
                    else -> (65f - (minLevel - 0.42f) * 90f).coerceIn(20f, 65f)
                }
                val t = if (s >= 76f) "Deep, relaxed Tone 3!" else "Drop lower into the bottom of your voice range."
                s to t
            }
            4 -> {
                val s = when {
                    slope <= -0.18f -> (84f + min(16f, (-slope - 0.18f) * 70f))
                    slope <= -0.06f -> 60f + (-slope / 0.18f) * 24f
                    else -> (42f - slope * 80f).coerceIn(15f, 55f)
                }
                val t = if (s >= 76f) "Decisive falling Tone 4!" else "Start high and drop sharply like a firm 'No!'."
                s to t
            }
            else -> {
                val s = 82f
                s to "Light and brief neutral tone."
            }
        }

        val combined = (baseMaeScore * 0.40f + shapeScore * 0.60f).roundToInt().coerceIn(0, 100)
        return SyllablePitchFeedback(
            index = index,
            hanzi = spec.hanzi,
            pinyin = spec.pinyin,
            expectedTone = spec.expectedTone,
            score = combined,
            detectedPattern = detectedPattern,
            tip = tip
        )
    }

    private fun hzToSemitones(hz: Float, refHz: Float): Float {
        if (hz <= 0f || refHz <= 0f) return 0f
        return (12.0 * ln((hz / refHz).toDouble()) / ln(2.0)).toFloat()
    }

    private fun smoothStep(x: Float): Float {
        val c = x.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    private fun computePearsonCorrelation(x: List<Float>, y: List<Float>): Float {
        val n = min(x.size, y.size)
        if (n < 3) return 0f
        var mx = 0f
        var my = 0f
        for (i in 0 until n) {
            mx += x[i]
            my += y[i]
        }
        mx /= n
        my /= n
        var num = 0f
        var dx = 0f
        var dy = 0f
        for (i in 0 until n) {
            val ax = x[i] - mx
            val ay = y[i] - my
            num += ax * ay
            dx += ax * ax
            dy += ay * ay
        }
        val denom = sqrt((dx * dy).toDouble()).toFloat()
        return if (denom > 1e-6f) (num / denom).coerceIn(-1f, 1f) else 0f
    }

    private fun computeStdDev(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average().toFloat()
        var sum = 0f
        for (v in values) {
            val d = v - mean
            sum += d * d
        }
        return sqrt((sum / values.size).toDouble()).toFloat()
    }

    private fun readLeShort(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun readLeSignedShort(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or (bytes[offset + 1].toInt() shl 8)
    }

    private fun readLeInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }
}
