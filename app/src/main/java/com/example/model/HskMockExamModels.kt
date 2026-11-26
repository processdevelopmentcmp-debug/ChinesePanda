package com.example.model

enum class HskMockLevel(
    val id: String,
    val label: String,
    val targetNumericLevel: Int,
    val allowPinyinToggle: Boolean
) {
    HSK_1("1", "HSK 1", 1, true),
    HSK_2("2", "HSK 2", 2, true),
    HSK_3("3", "HSK 3", 3, false),
    HSK_4("4", "HSK 4", 4, false),
    HSK_5("5", "HSK 5", 5, false),
    HSK_6("6", "HSK 6", 6, false),
    HSK_7_9("7-9", "HSK 7–9", 7, false)
}

enum class HskMockExamMode(
    val label: String,
    val subtitle: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val listeningCount: Int,
    val readingCount: Int,
    val writingCount: Int
) {
    QUICK(
        label = "Quick Mock",
        subtitle = "15 Qs · 10 min",
        totalQuestions = 15,
        durationMinutes = 10,
        listeningCount = 5,
        readingCount = 6,
        writingCount = 4
    ),
    FULL(
        label = "Full Mock",
        subtitle = "36 Qs · 25 min",
        totalQuestions = 36,
        durationMinutes = 25,
        listeningCount = 12,
        readingCount = 16,
        writingCount = 8
    )
}

enum class MockExamSection(
    val labelEn: String,
    val labelZh: String
) {
    LISTENING("Listening", "听力"),
    READING("Reading", "阅读"),
    WRITING("Writing", "书写")
}

enum class MockQuestionKind {
    LISTENING_AUDIO_MCQ,
    READING_HANZI_MEANING,
    READING_COMPREHENSION,
    READING_CLOZE,
    WRITING_TILE_ORDER,
    WRITING_FREE_SENTENCE
}

data class MockExamQuestion(
    val id: String,
    val section: MockExamSection,
    val kind: MockQuestionKind,
    val promptText: String,
    val hanziPrompt: String = "",
    val pinyinPrompt: String = "",
    val audioScript: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = -1,
    val scrambledTiles: List<Pair<String, String>> = emptyList(),
    val expectedSentenceHanzi: String = "",
    val expectedSentencePinyin: String = "",
    val targetWords: List<VocabWord> = emptyList(),
    val explanation: String = ""
)

data class MockQuestionAnswer(
    val selectedOptionIndex: Int? = null,
    val assembledTileIndices: List<Int> = emptyList(),
    val typedSentence: String = "",
    val replaysUsed: Int = 0
)

data class ActiveMockExamSession(
    val examId: String,
    val level: HskMockLevel,
    val mode: HskMockExamMode,
    val questions: List<MockExamQuestion>,
    val durationSeconds: Int,
    val startedAtEpochMs: Long = System.currentTimeMillis()
)

data class MockExamMistakeItem(
    val questionNumber: Int,
    val section: MockExamSection,
    val promptSummary: String,
    val userAnswerDisplay: String,
    val correctHanzi: String,
    val correctPinyin: String,
    val correctEnglish: String,
    val feedbackNote: String,
    val missedWords: List<VocabWord>
)

data class MockExamReport(
    val examId: String,
    val level: HskMockLevel,
    val mode: HskMockExamMode,
    val timestampEpochMs: Long,
    val listeningScore: Int,
    val readingScore: Int,
    val writingScore: Int,
    val totalScore: Int,
    val passed: Boolean,
    val elapsedSeconds: Int,
    val mistakes: List<MockExamMistakeItem>,
    val missedVocabWords: List<VocabWord>,
    val xpEarned: Int,
    val minutesLogged: Int
)

data class PastMockExamRecord(
    val id: String,
    val levelLabel: String,
    val modeLabel: String,
    val dateLabel: String,
    val totalScore: Int,
    val listeningScore: Int,
    val readingScore: Int,
    val writingScore: Int,
    val passed: Boolean,
    val timestampEpochMs: Long
)
