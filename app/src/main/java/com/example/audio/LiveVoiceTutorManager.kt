package com.example.audio

import android.annotation.SuppressLint
import android.util.Log
import com.example.model.LiveVoiceState
import com.example.model.LiveVoiceTranscriptLine
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val LIVE_TAG = "LiveVoiceTutor"
private const val LIVE_VOICE_MODEL = "gemini-3.8-live"

/**
 * Manages real-time bidirectional voice conversations with Panda Laoshi using
 * Firebase AI Logic's Gemini Live API (`gemini-3.8-live`).
 */
@OptIn(PublicPreviewAPI::class)
class LiveVoiceTutorManager {

    private val _state = MutableStateFlow<LiveVoiceState>(LiveVoiceState.Idle)
    val state: StateFlow<LiveVoiceState> = _state.asStateFlow()

    private var activeSession: LiveSession? = null

    /**
     * Connects to the Gemini Live API and starts bidirectional audio conversation.
     * Caller MUST ensure [android.Manifest.permission.RECORD_AUDIO] is already granted.
     */
    @SuppressLint("MissingPermission")
    suspend fun startConversation(
        hskLevel: Int,
        voiceName: String = "Aoede"
    ) {
        stopConversation()
        _state.value = LiveVoiceState.Connecting

        val clampedLevel = hskLevel.coerceIn(1, 9)
        try {
            val liveModel = Firebase.ai.liveModel(
                modelName = LIVE_VOICE_MODEL,
                generationConfig = liveGenerationConfig {
                    responseModality = ResponseModality.AUDIO
                    speechConfig = SpeechConfig(voice = Voice(voiceName))
                    inputAudioTranscription = AudioTranscriptionConfig()
                    outputAudioTranscription = AudioTranscriptionConfig()
                },
                systemInstruction = content {
                    text(
                        "You are Panda Laoshi (熊猫老师), a warm, encouraging real-time Mandarin Chinese voice tutor " +
                            "for HSK 3.0 Level $clampedLevel. Speak clearly at a friendly learner pace. " +
                            "Start by greeting the learner warmly in Mandarin and English, then invite them to speak. " +
                            "Gently praise their effort, model accurate Mandarin tones, and keep each spoken turn concise (1–3 sentences) so the conversation feels natural and interactive."
                    )
                }
            )

            val session = withTimeout(30_000L) {
                liveModel.connect()
            }
            activeSession = session

            _state.value = LiveVoiceState.Active(
                voiceName = voiceName,
                hskLevel = clampedLevel,
                transcripts = listOf(
                    LiveVoiceTranscriptLine(
                        id = "live_ready",
                        fromUser = false,
                        text = "Connected to Panda Laoshi Live (HSK $clampedLevel · $voiceName). Speak Mandarin or English now!"
                    )
                )
            )

            session.startAudioConversation(
                functionCallHandler = null,
                transcriptHandler = { inputTranscript, outputTranscript ->
                    val userText = inputTranscript?.text?.trim().orEmpty()
                    if (userText.isNotEmpty()) {
                        appendOrUpdateTranscript(fromUser = true, delta = userText)
                    }
                    val modelText = outputTranscript?.text?.trim().orEmpty()
                    if (modelText.isNotEmpty()) {
                        appendOrUpdateTranscript(fromUser = false, delta = modelText)
                    }
                }
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(LIVE_TAG, "Gemini Live connection timed out", e)
            cleanupSession()
            _state.value = LiveVoiceState.Error(
                "Connection to Gemini Live timed out after 30 seconds. Please try again."
            )
        } catch (e: CancellationException) {
            cleanupSession()
            _state.value = LiveVoiceState.Idle
            throw e
        } catch (e: SecurityException) {
            Log.e(LIVE_TAG, "Microphone permission missing for Live API", e)
            cleanupSession()
            _state.value = LiveVoiceState.Error(
                "Microphone permission is required for real-time voice conversations."
            )
        } catch (e: Exception) {
            Log.e(LIVE_TAG, "Gemini Live voice session failed", e)
            cleanupSession()
            _state.value = LiveVoiceState.Error(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Could not start Live Voice conversation. Please try again."
            )
        }
    }

    private fun appendOrUpdateTranscript(fromUser: Boolean, delta: String) {
        val current = _state.value as? LiveVoiceState.Active ?: return
        val existing = current.transcripts.toMutableList()
        val last = existing.lastOrNull()
        if (last != null && last.fromUser == fromUser && last.id != "live_ready") {
            val joined = if (last.text.endsWith(delta)) {
                last.text
            } else {
                "${last.text} $delta".trim()
            }
            existing[existing.lastIndex] = last.copy(text = joined)
        } else {
            existing += LiveVoiceTranscriptLine(
                id = UUID.randomUUID().toString(),
                fromUser = fromUser,
                text = delta
            )
        }
        _state.value = current.copy(transcripts = existing.takeLast(30))
    }

    suspend fun stopConversation() {
        cleanupSession()
        _state.value = LiveVoiceState.Idle
    }

    private suspend fun cleanupSession() {
        val session = activeSession
        activeSession = null
        if (session != null) {
            try {
                session.stopAudioConversation()
            } catch (e: Exception) {
                Log.w(LIVE_TAG, "Error stopping audio conversation", e)
            }
            try {
                session.close()
            } catch (e: Exception) {
                Log.w(LIVE_TAG, "Error closing LiveSession", e)
            }
        }
    }

    fun closeSync() {
        val session = activeSession
        activeSession = null
        if (session != null) {
            try {
                session.stopAudioConversation()
            } catch (_: Exception) {
            }
        }
        _state.value = LiveVoiceState.Idle
    }

    companion object {
        val AVAILABLE_VOICES = listOf("Aoede", "Puck", "Kore", "Fenrir")
    }
}
