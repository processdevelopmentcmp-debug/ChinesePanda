package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.max
import kotlin.math.roundToInt

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Learner",
    val xp: Long = 0L,
    val streak: Long = 1L,
    val hearts: Long = 5L,
    val currentHskLevel: Long = 1L,
    val completedUnitIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomWordDoc(
    @DocumentId val id: String = "",
    val hanzi: String = "",
    val pinyin: String = "",
    val english: String = "",
    val hskLevel: Long = 1L,
    val topicId: Long = 1L,
    val writable: Boolean = true,
    val exampleHanzi: String = "",
    val examplePinyin: String = "",
    val exampleEnglish: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SrsCardDoc(
    @DocumentId val id: String = "",
    val wordKey: String = "",
    val hanzi: String = "",
    val pinyin: String = "",
    val english: String = "",
    val hskLevel: Long = 1L,
    val exampleHanzi: String = "",
    val repetitions: Long = 0L,
    val intervalDays: Long = 1L,
    val easeFactor: Double = 2.5,
    val lapses: Long = 0L,
    val nextReviewEpochMs: Long = 0L,
    val mnemonicHint: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StorySentenceDoc(
    val zh: String = "",
    val pinyin: String = "",
    val en: String = ""
)

@Keep
@IgnoreExtraProperties
data class SavedStoryDoc(
    @DocumentId val id: String = "",
    val titleZh: String = "",
    val titleEn: String = "",
    val hskLevel: Long = 1L,
    val topicName: String = "",
    val sentences: List<StorySentenceDoc> = emptyList(),
    val quizQuestion: String = "",
    val quizOptions: List<String> = emptyList(),
    val quizCorrectIndex: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// Domain UI Models

data class HskLevelMeta(
    val level: Int,
    val label: String,
    val band: String,
    val wordTarget: String,
    val cumulative: String
)

data class TopicCategory(
    val id: Int,
    val nameEn: String,
    val nameZh: String,
    val tag: String,
    val minHsk: Int
)

data class VocabWord(
    val key: String,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val hskLevel: Int,
    val topicId: Int,
    val writable: Boolean = true,
    val exampleHanzi: String = "",
    val examplePinyin: String = "",
    val exampleEnglish: String = "",
    val customDocId: String? = null,
    val partOfSpeech: String = "",
    val isHsk30NewTerm: Boolean = false
)

data class RadicalItem(
    val char: String,
    val pinyin: String,
    val name: String,
    val meaning: String,
    val origin: String,
    val examples: List<String>
)

data class LessonQuestion(
    val id: String,
    val kind: String, // "TRANSLATE", "GRAMMAR", "TONE", "RADICAL", "LISTEN"
    val prompt: String,
    val hanziHighlight: String = "",
    val pinyinHint: String = "",
    val audioText: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class LearningUnit(
    val id: String,
    val order: Int,
    val hskBadge: String,
    val title: String,
    val subtitle: String,
    val keyHanzi: List<String>,
    val xpReward: Int,
    val questions: List<LessonQuestion>
)

data class TileSentenceItem(
    val id: String,
    val hskLevel: Int,
    val topicId: Int,
    val promptEn: String,
    val tiles: List<Pair<String, String>>, // (hanzi, pinyin)
    val correctHanzi: String,
    val grammarNote: String
)

data class SandhiToneTile(
    val char: String,
    val basePinyin: String,
    val spokenPinyin: String,
    val spokenTone: Int // 1, 2, 3, 4, 5 (neutral)
)

data class SandhiPhrase(
    val id: String,
    val hskLevel: Int,
    val hanzi: String,
    val ruleTitle: String,
    val ruleDetail: String,
    val translationEn: String,
    val tiles: List<SandhiToneTile>
)

enum class ListeningQuestionType(val label: String) {
    WORD_MATCH("Part 1 · Word Match"),
    SENTENCE_MEANING("Part 2 · Sentence Meaning"),
    MINI_DIALOGUE("Part 3 · Short Dialogue"),
    TRUE_FALSE("Part 4 · True or False")
}

data class ListeningQuestion(
    val id: String,
    val hskLevel: Int,
    val topicId: Int,
    val type: ListeningQuestionType,
    val audioScript: String,
    val audioPinyin: String,
    val promptText: String,
    val options: List<String>,
    val correctIndex: Int,
    val targetWordKey: String
)

data class GradedStory(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val hskLevel: Int,
    val topicName: String,
    val sentences: List<StorySentenceDoc>,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizCorrectIndex: Int,
    val aiGenerated: Boolean = false
)

data class AiSentenceGrade(
    val score: Int,
    val verdict: String,
    val correctedHanzi: String,
    val correctedPinyin: String,
    val grammarAnalysis: String,
    val nativeTip: String
)

enum class TutorMode(val label: String, val subtitle: String) {
    CONVERSATION("Chat", "Bilingual conversation"),
    GRAMMAR("Grammar", "Deep SVO & particles"),
    ROLEPLAY("Roleplay", "Interactive China scenes"),
    PRONUNCIATION("Tones", "Fast sandhi & drills")
}

data class GroundedSource(
    val title: String,
    val uri: String
)

data class AiChatMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
    val streaming: Boolean = false,
    val isError: Boolean = false,
    val isWelcome: Boolean = false,
    val failedPrompt: String? = null,
    val modeLabel: String? = null,
    val sources: List<GroundedSource> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val searchEntryPointHtml: String? = null,
    val timestampMs: Long = System.currentTimeMillis()
)

data class LiveVoiceTranscriptLine(
    val id: String,
    val fromUser: Boolean,
    val text: String
)

sealed interface LiveVoiceState {
    data object Idle : LiveVoiceState
    data object Connecting : LiveVoiceState
    data class Active(
        val voiceName: String,
        val hskLevel: Int,
        val transcripts: List<LiveVoiceTranscriptLine> = emptyList()
    ) : LiveVoiceState
    data class Error(val message: String) : LiveVoiceState
}

// SuperMemo SM-2 Spaced Repetition Calculator
data class Sm2Result(
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val lapses: Int,
    val nextReviewEpochMs: Long
)

object Sm2Calculator {
    private const val DAY_MS = 86_400_000L

    fun calculate(
        quality: Int,
        currentReps: Int,
        currentInterval: Int,
        currentEase: Double,
        currentLapses: Int,
        nowMs: Long = System.currentTimeMillis()
    ): Sm2Result {
        val q = quality.coerceIn(0, 5)
        val newEase = max(1.3, currentEase + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
        return if (q < 3) {
            Sm2Result(
                repetitions = 0,
                intervalDays = 1,
                easeFactor = newEase,
                lapses = currentLapses + 1,
                nextReviewEpochMs = nowMs + 60_000L // Review again soon
            )
        } else {
            val nextInterval = when (currentReps) {
                0 -> 1
                1 -> 6
                else -> max(1, (currentInterval * newEase).roundToInt())
            }
            Sm2Result(
                repetitions = currentReps + 1,
                intervalDays = nextInterval,
                easeFactor = newEase,
                lapses = currentLapses,
                nextReviewEpochMs = nowMs + nextInterval * DAY_MS
            )
        }
    }
}
