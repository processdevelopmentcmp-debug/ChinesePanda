package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val AUDIO_TAG = "ChineseAudioManager"

class ChineseAudioManager(private val appContext: Context) {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private val pendingSynthDeferreds = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechStatus = MutableStateFlow<String?>(null)
    val speechStatus: StateFlow<String?> = _speechStatus.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                ttsReady = res != TextToSpeech.LANG_MISSING_DATA &&
                    res != TextToSpeech.LANG_NOT_SUPPORTED
                if (!ttsReady) {
                    // Fallback to general Chinese or default voice if simplified pack isn't downloaded
                    tts?.setLanguage(Locale.CHINESE)
                    ttsReady = true
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId != null) {
                            pendingSynthDeferreds.remove(utteranceId)?.complete(true)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId != null) {
                            pendingSynthDeferreds.remove(utteranceId)?.complete(false)
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (utteranceId != null) {
                            pendingSynthDeferreds.remove(utteranceId)?.complete(false)
                        }
                    }
                })
            } else {
                Log.w(AUDIO_TAG, "TTS initialization status: $status")
            }
        }
    }

    fun speak(text: String, speed: Float = 1.0f) {
        if (text.isBlank()) return
        val engine = tts ?: return
        if (!ttsReady) {
            Log.w(AUDIO_TAG, "TTS not ready yet for: $text")
        }
        engine.setSpeechRate(speed.coerceIn(0.5f, 1.5f))
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "panda_${System.currentTimeMillis()}")
    }

    /**
     * Synthesizes a native Mandarin reference utterance to a local WAV file so its
     * real F0 pitch contour can be extracted and overlaid against the learner's recording.
     */
    suspend fun synthesizeToWavFile(
        text: String,
        outputFile: File,
        speed: Float = 0.85f,
        timeoutMs: Long = 3500L
    ): Boolean {
        if (text.isBlank()) return false
        val engine = tts ?: return false
        if (!ttsReady) return false

        val utteranceId = "panda_synth_${System.nanoTime()}"
        val deferred = CompletableDeferred<Boolean>()
        pendingSynthDeferreds[utteranceId] = deferred

        return try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) outputFile.delete()
            engine.setSpeechRate(speed.coerceIn(0.5f, 1.5f))
            val result = engine.synthesizeToFile(text, Bundle(), outputFile, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                pendingSynthDeferreds.remove(utteranceId)
                return false
            }
            val ok = withTimeoutOrNull(timeoutMs) { deferred.await() } ?: false
            ok && outputFile.exists() && outputFile.length() > 64L
        } catch (e: Exception) {
            Log.w(AUDIO_TAG, "TTS synthesizeToFile failed", e)
            false
        } finally {
            pendingSynthDeferreds.remove(utteranceId)
        }
    }

    fun startChineseListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError("Speech recognition service is not available on this device.")
            return
        }
        stopListening()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _speechStatus.value = "Listening in Mandarin (zh-CN)..."
            }

            override fun onBeginningOfSpeech() {
                _speechStatus.value = "Hearing your voice..."
            }

            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                _isListening.value = false
                _speechStatus.value = "Analyzing pronunciation..."
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No Chinese speech recognized. Speak clearly and try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap record and speak."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Network error during speech recognition."
                    else -> "Speech recognition could not complete (code $error)."
                }
                _speechStatus.value = msg
                onError(msg)
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull().orEmpty()
                if (best.isNotBlank()) {
                    _speechStatus.value = "Heard: $best"
                    onResult(best)
                } else {
                    _speechStatus.value = "Could not detect speech."
                    onError("Could not detect speech. Please try again.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    _speechStatus.value = "Hearing: $partial"
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError(e.localizedMessage ?: "Failed to start microphone.")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
        _isListening.value = false
    }

    fun release() {
        stopListening()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
