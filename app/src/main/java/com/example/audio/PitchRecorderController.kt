package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

private const val RECORDER_TAG = "PitchRecorderController"

enum class ActivePlaybackSource {
    NONE,
    NATIVE,
    USER
}

data class PitchPracticeUiState(
    val isRecording: Boolean = false,
    val isPreparingNative: Boolean = false,
    val liveRms: Float = 0f,
    val liveHz: Float = 0f,
    val livePitchPoints: List<NormalizedPitchPoint> = emptyList(),
    val nativeContour: List<NormalizedPitchPoint> = emptyList(),
    val usedRealNativeWav: Boolean = false,
    val nativeMeanHz: Float = 0f,
    val recordingDurationMs: Long = 0L,
    val hasUserRecording: Boolean = false,
    val activePlayback: ActivePlaybackSource = ActivePlaybackSource.NONE,
    val playbackProgress: Float = 0f,
    val report: PronunciationPitchReport? = null,
    val errorMessage: String? = null
)

/**
 * Hardware microphone AudioRecord capture, real-time YIN pitch streaming,
 * native TTS WAV contour extraction, and AudioTrack playback controller.
 */
class PitchRecorderController(
    private val appContext: Context,
    private val audioManager: ChineseAudioManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(PitchPracticeUiState())
    val state: StateFlow<PitchPracticeUiState> = _state.asStateFlow()

    private val nativeCache = ConcurrentHashMap<String, NativeReferenceCacheEntry>()

    private var recordJob: Job? = null
    private var playbackJob: Job? = null
    private var nativePrepJob: Job? = null
    private var activeTrack: AudioTrack? = null

    @Volatile
    private var lastUserPcm: ShortArray? = null
    private var lastUserSampleRate: Int = PitchAnalysisEngine.DEFAULT_SAMPLE_RATE

    private var activeTargetHanzi: String = ""
    private var activeSyllables: List<TargetSyllableSpec> = emptyList()

    private data class NativeReferenceCacheEntry(
        val contour: List<NormalizedPitchPoint>,
        val meanHz: Float,
        val usedRealWav: Boolean,
        val durationMs: Long
    )

    /**
     * Loads or synthesizes the native Mandarin reference contour for the selected phrase.
     * Immediately renders canonical Chao 5-level tones, then upgrades seamlessly to the
     * real F0 pitch curve extracted from the native zh-CN TTS WAV file when available.
     */
    fun prepareNativeReference(
        hanzi: String,
        syllables: List<TargetSyllableSpec>,
        speed: Float = 0.85f
    ) {
        stopPlayback()
        if (_state.value.isRecording) {
            stopRecordingAndAnalyze()
        }
        activeTargetHanzi = hanzi
        activeSyllables = syllables
        lastUserPcm = null

        val canonical = PitchAnalysisEngine.buildCanonicalToneContour(
            tones = syllables.map { it.expectedTone },
            totalPoints = 64
        )
        val cacheKey = "${hanzi}_${(speed * 100).toInt()}"
        val cached = nativeCache[cacheKey]
        if (cached != null) {
            _state.value = PitchPracticeUiState(
                nativeContour = cached.contour,
                usedRealNativeWav = cached.usedRealWav,
                nativeMeanHz = cached.meanHz
            )
            return
        }

        _state.value = PitchPracticeUiState(
            isPreparingNative = true,
            nativeContour = canonical,
            usedRealNativeWav = false
        )

        nativePrepJob?.cancel()
        nativePrepJob = scope.launch {
            val outDir = File(appContext.cacheDir, "native_pitch_wavs")
            val safeName = "ref_${cacheKey.hashCode().toUInt().toString(16)}.wav"
            val wavFile = File(outDir, safeName)

            val synthOk = audioManager.synthesizeToWavFile(
                text = hanzi,
                outputFile = wavFile,
                speed = speed
            )

            val entry = if (synthOk) {
                val decoded = runCatching { PitchAnalysisEngine.decodeWavPcm16(wavFile.readBytes()) }.getOrNull()
                if (decoded != null) {
                    val (pcm, sampleRate) = decoded
                    val frames = PitchAnalysisEngine.analyzePcmBuffer(
                        samples = pcm,
                        sampleRate = sampleRate,
                        windowSize = 1024,
                        hopSize = 256
                    )
                    val (contour, meanHz) = PitchAnalysisEngine.normalizePitchTrack(frames, targetPointCount = 64)
                    val durMs = ((pcm.size.toLong() * 1000L) / sampleRate).coerceIn(600L, 6000L)
                    if (contour.count { it.isVoiced } >= 16) {
                        NativeReferenceCacheEntry(contour, meanHz, true, durMs)
                    } else {
                        NativeReferenceCacheEntry(canonical, 0f, false, (syllables.size * 520L).coerceIn(900L, 4500L))
                    }
                } else {
                    NativeReferenceCacheEntry(canonical, 0f, false, (syllables.size * 520L).coerceIn(900L, 4500L))
                }
            } else {
                NativeReferenceCacheEntry(canonical, 0f, false, (syllables.size * 520L).coerceIn(900L, 4500L))
            }

            nativeCache[cacheKey] = entry
            if (activeTargetHanzi == hanzi) {
                _state.value = _state.value.copy(
                    isPreparingNative = false,
                    nativeContour = entry.contour,
                    usedRealNativeWav = entry.usedRealWav,
                    nativeMeanHz = entry.meanHz
                )
            }
        }
    }

    /**
     * Plays the native speaker audio via TTS and animates a synchronized playhead across the native pitch curve.
     */
    fun playNativeReference(hanzi: String, speed: Float = 0.85f) {
        stopPlayback()
        if (hanzi.isBlank()) return
        audioManager.speak(hanzi, speed)

        val cacheKey = "${hanzi}_${(speed * 100).toInt()}"
        val estDurationMs = nativeCache[cacheKey]?.durationMs
            ?: (hanzi.length * 480L / speed.coerceIn(0.6f, 1.3f)).toLong().coerceIn(850L, 4500L)

        playbackJob = scope.launch {
            _state.value = _state.value.copy(
                activePlayback = ActivePlaybackSource.NATIVE,
                playbackProgress = 0f
            )
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / estDurationMs.toFloat()).coerceIn(0f, 1f)
                _state.value = _state.value.copy(playbackProgress = progress)
                if (progress >= 1f) break
                delay(24L)
            }
            _state.value = _state.value.copy(
                activePlayback = ActivePlaybackSource.NONE,
                playbackProgress = 0f
            )
        }
    }

    /**
     * Starts live microphone PCM recording via AudioRecord and streams real-time YIN pitch analysis.
     * Caller MUST ensure RECORD_AUDIO permission is granted before calling.
     */
    @SuppressLint("MissingPermission")
    fun startRecording(
        maxDurationMs: Long = 5200L,
        onCompleted: (PronunciationPitchReport) -> Unit = {}
    ) {
        stopPlayback()
        audioManager.stopListening()
        recordJob?.cancel()

        val sampleRate = PitchAnalysisEngine.DEFAULT_SAMPLE_RATE
        val minBufBytes = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufBytes <= 0) {
            _state.value = _state.value.copy(
                isRecording = false,
                errorMessage = "Audio recording hardware is unavailable on this device."
            )
            return
        }

        val bufferSizeBytes = maxOf(minBufBytes * 2, 4096)
        val recorder = createAudioRecord(sampleRate, bufferSizeBytes)
        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder?.release()
            _state.value = _state.value.copy(
                isRecording = false,
                errorMessage = "Could not initialize microphone. Check microphone permissions and try again."
            )
            return
        }

        _state.value = _state.value.copy(
            isRecording = true,
            liveRms = 0f,
            liveHz = 0f,
            livePitchPoints = emptyList(),
            recordingDurationMs = 0L,
            report = null,
            errorMessage = null
        )

        recordJob = scope.launch {
            val capturedChunks = mutableListOf<ShortArray>()
            val analyzedFrames = mutableListOf<PitchFrame>()
            val voicedHzHistory = mutableListOf<Float>()
            val windowSize = 1024
            val hopSize = 512
            val ringWindow = ShortArray(windowSize)
            val readBuf = ShortArray(hopSize)
            var totalSamples = 0
            val startWallMs = System.currentTimeMillis()

            try {
                recorder.startRecording()
                while (isActive) {
                    val elapsedMs = System.currentTimeMillis() - startWallMs
                    if (elapsedMs >= maxDurationMs) break

                    val readCount = recorder.read(readBuf, 0, hopSize)
                    if (readCount <= 0) {
                        delay(10L)
                        continue
                    }

                    val chunk = readBuf.copyOf(readCount)
                    capturedChunks.add(chunk)
                    totalSamples += readCount

                    // Shift ring buffer and append new hop
                    if (readCount >= windowSize) {
                        System.arraycopy(readBuf, readCount - windowSize, ringWindow, 0, windowSize)
                    } else {
                        System.arraycopy(ringWindow, readCount, ringWindow, 0, windowSize - readCount)
                        System.arraycopy(readBuf, 0, ringWindow, windowSize - readCount, readCount)
                    }

                    if (totalSamples >= windowSize) {
                        val frame = PitchAnalysisEngine.estimatePitchYin(
                            samples = ringWindow,
                            sampleRate = sampleRate,
                            timeMs = elapsedMs
                        )
                        analyzedFrames.add(frame)
                        if (frame.isVoiced && frame.frequencyHz > 0f) {
                            voicedHzHistory.add(frame.frequencyHz)
                        }

                        // Build real-time normalized preview points so user sees their pitch line live
                        val baselineHz = if (voicedHzHistory.isNotEmpty()) {
                            val sorted = voicedHzHistory.sorted()
                            sorted[sorted.size / 2]
                        } else 185f

                        val livePreview = buildLivePreviewPoints(analyzedFrames, baselineHz)
                        _state.value = _state.value.copy(
                            liveRms = (frame.rmsLevel * 3.8f).coerceIn(0f, 1f),
                            liveHz = frame.frequencyHz,
                            livePitchPoints = livePreview,
                            recordingDurationMs = elapsedMs
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(RECORDER_TAG, "AudioRecord capture error", e)
                _state.value = _state.value.copy(
                    errorMessage = "Microphone error: ${e.localizedMessage ?: "recording interrupted"}"
                )
            } finally {
                runCatching {
                    if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        recorder.stop()
                    }
                }
                runCatching { recorder.release() }

                // Assemble full recorded PCM for playback + full-resolution pitch report
                val fullPcm = ShortArray(totalSamples)
                var cursor = 0
                for (c in capturedChunks) {
                    System.arraycopy(c, 0, fullPcm, cursor, c.size)
                    cursor += c.size
                }
                lastUserPcm = fullPcm.takeIf { it.isNotEmpty() }
                lastUserSampleRate = sampleRate

                val highResFrames = if (fullPcm.size >= 1024) {
                    PitchAnalysisEngine.analyzePcmBuffer(
                        samples = fullPcm,
                        sampleRate = sampleRate,
                        windowSize = 1024,
                        hopSize = 256
                    )
                } else {
                    analyzedFrames
                }

                val currentState = _state.value
                val report = PitchAnalysisEngine.comparePronunciation(
                    syllables = activeSyllables,
                    userFrames = highResFrames,
                    nativeWavContour = currentState.nativeContour.takeIf { currentState.usedRealNativeWav },
                    nativeMeanHz = currentState.nativeMeanHz
                )

                _state.value = _state.value.copy(
                    isRecording = false,
                    liveRms = 0f,
                    liveHz = 0f,
                    hasUserRecording = fullPcm.size > 1600,
                    report = report,
                    livePitchPoints = report.userContour
                )

                withContext(Dispatchers.Main) {
                    onCompleted(report)
                }
            }
        }
    }

    fun stopRecordingAndAnalyze() {
        recordJob?.cancel()
    }

    /**
     * Replays the learner's most recent microphone recording through AudioTrack
     * while animating the playhead cursor across their pitch contour.
     */
    fun playUserRecording() {
        stopPlayback()
        val pcm = lastUserPcm ?: return
        if (pcm.size < 400) return

        val sampleRate = lastUserSampleRate
        val durationMs = ((pcm.size.toLong() * 1000L) / sampleRate).coerceAtLeast(350L)

        playbackJob = scope.launch(Dispatchers.IO) {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(RECORDER_TAG, "Failed to create AudioTrack", e)
                return@launch
            }

            activeTrack = track
            _state.value = _state.value.copy(
                activePlayback = ActivePlaybackSource.USER,
                playbackProgress = 0f
            )

            val progressJob = launch {
                val start = System.currentTimeMillis()
                while (isActive) {
                    val p = ((System.currentTimeMillis() - start).toFloat() / durationMs).coerceIn(0f, 1f)
                    _state.value = _state.value.copy(playbackProgress = p)
                    if (p >= 1f) break
                    delay(24L)
                }
            }

            try {
                track.play()
                var offset = 0
                val chunk = 1024
                while (isActive && offset < pcm.size) {
                    val toWrite = min(chunk, pcm.size - offset)
                    val written = track.write(pcm, offset, toWrite)
                    if (written <= 0) break
                    offset += written
                }
                delay(120L)
            } catch (e: Exception) {
                Log.w(RECORDER_TAG, "AudioTrack playback interrupted", e)
            } finally {
                progressJob.cancel()
                runCatching {
                    track.stop()
                    track.release()
                }
                if (activeTrack === track) activeTrack = null
                _state.value = _state.value.copy(
                    activePlayback = ActivePlaybackSource.NONE,
                    playbackProgress = 0f
                )
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        runCatching {
            activeTrack?.stop()
            activeTrack?.release()
        }
        activeTrack = null
        if (_state.value.activePlayback != ActivePlaybackSource.NONE) {
            _state.value = _state.value.copy(
                activePlayback = ActivePlaybackSource.NONE,
                playbackProgress = 0f
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun release() {
        stopPlayback()
        recordJob?.cancel()
        nativePrepJob?.cancel()
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord(sampleRate: Int, bufferSizeBytes: Int): AudioRecord? {
        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC
        )
        for (source in sources) {
            val rec = runCatching {
                AudioRecord(
                    source,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeBytes
                )
            }.getOrNull()
            if (rec != null && rec.state == AudioRecord.STATE_INITIALIZED) {
                return rec
            }
            rec?.release()
        }
        return null
    }

    private fun buildLivePreviewPoints(
        frames: List<PitchFrame>,
        baselineHz: Float
    ): List<NormalizedPitchPoint> {
        val firstVoiced = frames.indexOfFirst { it.isVoiced }
        if (firstVoiced < 0) return emptyList()
        val slice = frames.subList(firstVoiced, frames.size)
        val denom = maxOf(slice.size - 1, 24).toFloat()
        return slice.mapIndexed { idx, frame ->
            val t = (idx / denom).coerceIn(0f, 1f)
            val level = if (frame.isVoiced && frame.frequencyHz > 0f) {
                PitchAnalysisEngine.mapLiveHzToPitchLevel(frame.frequencyHz, baselineHz)
            } else {
                0.5f
            }
            NormalizedPitchPoint(
                t = t,
                pitchLevel = level,
                frequencyHz = frame.frequencyHz,
                isVoiced = frame.isVoiced
            )
        }
    }
}
