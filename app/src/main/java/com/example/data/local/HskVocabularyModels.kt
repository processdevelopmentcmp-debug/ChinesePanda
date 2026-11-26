package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.HskLevelMeta
import com.example.model.TopicCategory
import com.example.model.VocabWord

/**
 * Official HSK 3.0 three-stage, nine-level proficiency bands:
 * - Elementary (Levels 1–3)
 * - Intermediate (Levels 4–6)
 * - Advanced (Levels 7–9)
 */
enum class HskBand(val displayName: String, val levelRange: IntRange) {
    ELEMENTARY("Elementary (HSK 1–3)", 1..3),
    INTERMEDIATE("Intermediate (HSK 4–6)", 4..6),
    ADVANCED("Advanced (HSK 7–9)", 7..9);

    companion object {
        fun fromLevel(level: Int): HskBand = when {
            level <= 3 -> ELEMENTARY
            level <= 6 -> INTERMEDIATE
            else -> ADVANCED
        }
    }
}

/**
 * Local Room entity representing a structured HSK 3.0 vocabulary list entry.
 */
@Entity(
    tableName = "hsk_vocabulary",
    indices = [
        Index(value = ["hsk_level"]),
        Index(value = ["topic_id"]),
        Index(value = ["hsk_band"])
    ]
)
data class HskVocabularyEntity(
    @PrimaryKey
    @ColumnInfo(name = "word_key")
    val wordKey: String,
    @ColumnInfo(name = "hanzi")
    val hanzi: String,
    @ColumnInfo(name = "pinyin")
    val pinyin: String,
    @ColumnInfo(name = "english")
    val english: String,
    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,
    @ColumnInfo(name = "hsk_band")
    val hskBand: String,
    @ColumnInfo(name = "topic_id")
    val topicId: Int,
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,
    @ColumnInfo(name = "writable")
    val writable: Boolean,
    @ColumnInfo(name = "is_hsk30_new_term")
    val isHsk30NewTerm: Boolean,
    @ColumnInfo(name = "example_hanzi")
    val exampleHanzi: String,
    @ColumnInfo(name = "example_pinyin")
    val examplePinyin: String,
    @ColumnInfo(name = "example_english")
    val exampleEnglish: String
)

/**
 * Local Room entity representing one of the 14 real-life HSK 3.0 thematic topics.
 */
@Entity(tableName = "hsk_topics")
data class HskTopicEntity(
    @PrimaryKey
    @ColumnInfo(name = "topic_id")
    val topicId: Int,
    @ColumnInfo(name = "name_en")
    val nameEn: String,
    @ColumnInfo(name = "name_zh")
    val nameZh: String,
    @ColumnInfo(name = "tag")
    val tag: String,
    @ColumnInfo(name = "min_hsk_level")
    val minHskLevel: Int
)

fun HskVocabularyEntity.toVocabWord(): VocabWord = VocabWord(
    key = wordKey,
    hanzi = hanzi,
    pinyin = pinyin,
    english = english,
    hskLevel = hskLevel,
    topicId = topicId,
    writable = writable,
    exampleHanzi = exampleHanzi,
    examplePinyin = examplePinyin,
    exampleEnglish = exampleEnglish,
    partOfSpeech = partOfSpeech,
    isHsk30NewTerm = isHsk30NewTerm
)

fun HskTopicEntity.toTopicCategory(): TopicCategory = TopicCategory(
    id = topicId,
    nameEn = nameEn,
    nameZh = nameZh,
    tag = tag,
    minHsk = minHskLevel
)

/**
 * Structured summary for an HSK 3.0 level card in the learning interface.
 */
data class HskLevelSummary(
    val meta: HskLevelMeta,
    val band: HskBand,
    val activeWordCount: Int,
    val writableCount: Int
)

/**
 * Structured vocabulary group by topic for populating learning decks and lists.
 */
data class HskTopicListSection(
    val topic: TopicCategory,
    val words: List<VocabWord>,
    val levelsCovered: List<Int>
)

/**
 * SQLite projection for per-level word counts.
 */
data class LevelCountRow(
    val hskLevel: Int,
    val totalCount: Int,
    val writableCount: Int
)

/**
 * SQLite projection for per-topic word counts.
 */
data class TopicCountRow(
    val topicId: Int,
    val count: Int
)

/**
 * Sync status for the full 11,000+ word HSK 3.0 offline Room catalog.
 */
sealed interface HskCatalogSyncState {
    data object Idle : HskCatalogSyncState
    data class Syncing(val progressPercent: Int, val status: String) : HskCatalogSyncState
    data class Ready(val totalWords: Int, val fullCatalogLoaded: Boolean) : HskCatalogSyncState
    data class Error(val message: String, val currentWords: Int) : HskCatalogSyncState
}

/**
 * Filter criteria used by the ViewModel and UI to query HSK 3.0 vocabulary lists.
 */
data class HskVocabularyFilter(
    val levelFilter: String = "ALL", // "ALL", "1".."6", "7+"
    val topicId: Int? = null,
    val searchQuery: String = "",
    val writableOnly: Boolean = false,
    val hsk30NewOnly: Boolean = false,
    val pageSize: Int = 60
)
