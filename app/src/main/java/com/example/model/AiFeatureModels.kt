package com.example.model

data class ScannedSecondaryObject(
    val hanzi: String,
    val pinyin: String,
    val english: String
)

data class CameraPointLearnResult(
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val hskLevel: Int,
    val radicalTip: String,
    val exampleHanzi: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    val relatedObjects: List<ScannedSecondaryObject> = emptyList()
)

data class GrammarTransformResult(
    val baseSentence: String,
    val transformationLabel: String,
    val transformedHanzi: String,
    val transformedPinyin: String,
    val transformedEnglish: String,
    val highlightedChanges: List<String>,
    val grammarExplanation: String
)

data class ShadowingFlaggedWord(
    val expectedWord: String,
    val expectedPinyin: String,
    val issue: String
)

data class ShadowingGradeResult(
    val fluencyScore: Int,
    val verdict: String,
    val targetScript: String,
    val recognizedSpeech: String,
    val flaggedWords: List<ShadowingFlaggedWord>,
    val coachingTip: String
)
