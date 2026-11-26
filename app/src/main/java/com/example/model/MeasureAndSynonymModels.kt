package com.example.model

enum class LabHskBand(val label: String) {
    ALL("All"),
    HSK_1_2("HSK 1–2"),
    HSK_3_4("HSK 3–4"),
    HSK_5_6("HSK 5–6");

    fun matchesLevel(hskLevel: Int): Boolean = when (this) {
        ALL -> true
        HSK_1_2 -> hskLevel in 1..2
        HSK_3_4 -> hskLevel in 3..4
        HSK_5_6 -> hskLevel >= 5
    }
}

enum class MeasureLabMainTab(val label: String) {
    MEASURE_WORDS("Measure Words (量词)"),
    SYNONYM_BATTLE("Synonym Battle (近义词 PK)")
}

enum class MeasureSubMode(val label: String) {
    CHEAT_SHEET("Visual Cheat Sheet"),
    SPEED_DRILL("Speed Drill")
}

enum class SynonymSubMode(val label: String) {
    VS_CARDS("VS Cards"),
    BATTLE_QUIZ("Battle Quiz")
}

data class MeasureExample(
    val numberPrefix: String = "一",
    val nounHanzi: String,
    val fullHanzi: String,
    val fullPinyin: String,
    val english: String
)

data class MeasureWordEntry(
    val id: String,
    val hanzi: String,
    val pinyin: String,
    val hskLevel: Int,
    val band: LabHskBand,
    val visualCategory: String,
    val shapeGlyph: String,
    val visualLogicRule: String,
    val examples: List<MeasureExample>
) {
    fun toVocabWord(): VocabWord {
        val sample = examples.firstOrNull()
        return VocabWord(
            key = "mw_${id}_$hanzi",
            hanzi = hanzi,
            pinyin = pinyin,
            english = "Measure word · $visualCategory ($visualLogicRule)",
            hskLevel = hskLevel,
            topicId = 1,
            writable = true,
            exampleHanzi = sample?.fullHanzi.orEmpty(),
            examplePinyin = sample?.fullPinyin.orEmpty(),
            exampleEnglish = sample?.english.orEmpty(),
            partOfSpeech = "measure word"
        )
    }
}

data class MeasureDrillQuestion(
    val id: String,
    val promptBlankHanzi: String,
    val nounOnlyHanzi: String,
    val fullHanzi: String,
    val fullPinyin: String,
    val english: String,
    val correctEntry: MeasureWordEntry,
    val optionEntries: List<MeasureWordEntry>,
    val whyItFits: String
)

data class SynonymSide(
    val hanzi: String,
    val pinyin: String,
    val coreMeaning: String,
    val usageRule: String,
    val exampleHanzi: String,
    val examplePinyin: String,
    val exampleEnglish: String
) {
    fun toVocabWord(hskLevel: Int, battleId: String): VocabWord = VocabWord(
        key = "syn_${battleId}_$hanzi",
        hanzi = hanzi,
        pinyin = pinyin,
        english = "$coreMeaning · $usageRule",
        hskLevel = hskLevel,
        topicId = 1,
        writable = true,
        exampleHanzi = exampleHanzi,
        examplePinyin = examplePinyin,
        exampleEnglish = exampleEnglish,
        partOfSpeech = "near-synonym"
    )
}

data class SynonymQuizPrompt(
    val id: String,
    val battleId: String,
    val battleTitle: String,
    val hskLevel: Int,
    val band: LabHskBand,
    val sentenceWithBlank: String,
    val fullHanzi: String,
    val fullPinyin: String,
    val english: String,
    val options: List<String>,
    val correctOption: String,
    val grammarReason: String,
    val targetSides: List<SynonymSide>
)

data class SynonymBattleItem(
    val id: String,
    val title: String,
    val hskLevel: Int,
    val band: LabHskBand,
    val sides: List<SynonymSide>,
    val coreDifference: String,
    val commonMistakeWrong: String,
    val commonMistakeRight: String,
    val commonMistakeNote: String,
    val offlineExtraExamples: String,
    val quizQuestions: List<SynonymQuizPrompt>
)
