package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local Room entity caching character-level structural decomposition, Kangxi radical,
 * and etymology data (Make Me a Hanzi ~9,574 characters — powering HanziCraft & CharacterPop-style
 * radical/semantic/phonetic breakdowns offline).
 */
@Entity(tableName = "hanzi_character_info")
data class HanziCharacterEntity(
    @PrimaryKey
    @ColumnInfo(name = "character")
    val character: String,
    @ColumnInfo(name = "definition")
    val definition: String,
    @ColumnInfo(name = "pinyin")
    val pinyin: String,
    @ColumnInfo(name = "radical")
    val radical: String,
    @ColumnInfo(name = "decomposition")
    val decomposition: String,
    @ColumnInfo(name = "etymology_type")
    val etymologyType: String,
    @ColumnInfo(name = "semantic_component")
    val semanticComponent: String,
    @ColumnInfo(name = "phonetic_component")
    val phoneticComponent: String,
    @ColumnInfo(name = "etymology_hint")
    val etymologyHint: String
)

/**
 * Permanent local SQLite cache for AI-generated CharacterPop-style memory stories,
 * word-making logic, and HanziCraft-style historical etymology guides.
 */
@Entity(tableName = "word_story_cache")
data class WordStoryCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "hanzi")
    val hanzi: String,
    @ColumnInfo(name = "word_making_logic")
    val wordMakingLogic: String,
    @ColumnInfo(name = "character_pop_story")
    val characterPopStory: String,
    @ColumnInfo(name = "historical_origin")
    val historicalOrigin: String,
    @ColumnInfo(name = "character_notes_json")
    val characterNotesJson: String,
    @ColumnInfo(name = "related_words_json")
    val relatedWordsJson: String,
    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long = System.currentTimeMillis()
)

// --- UI DOMAIN MODELS FOR WORD ANATOMY, DECOMPOSITION, STORY & HISTORY ---

enum class ComponentRole(val label: String) {
    SEMANTIC("Meaning"),
    PHONETIC("Sound"),
    RADICAL("Radical"),
    BUILDING_BLOCK("Component")
}

data class HanziComponentPart(
    val glyph: String,
    val pinyin: String,
    val meaning: String,
    val role: ComponentRole
)

data class CharacterAnatomy(
    val character: String,
    val pinyin: String,
    val definition: String,
    val radical: String,
    val radicalMeaning: String,
    val decompositionRaw: String,
    val structureLabel: String,
    val formationType: String,
    val formationBadgeZh: String,
    val semanticComponent: String,
    val phoneticComponent: String,
    val etymologyHint: String,
    val components: List<HanziComponentPart>
)

data class AiCharacterNote(
    val character: String,
    val formationExplanation: String,
    val miniStory: String
)

data class RelatedFamilyWord(
    val hanzi: String,
    val pinyin: String,
    val english: String
)

data class WordEtymologyStory(
    val hanzi: String,
    val wordMakingLogic: String,
    val characterPopStory: String,
    val historicalOrigin: String,
    val characterNotes: List<AiCharacterNote>,
    val relatedWords: List<RelatedFamilyWord>
)

/**
 * Decodes Unicode Ideographic Description Sequences (IDS) from Make Me a Hanzi
 * into readable HanziCraft / CharacterPop layout descriptions and component glyphs.
 */
object IdsDecompositionParser {

    private val IDS_OPERATORS = mapOf(
        '⿰' to "Left + Right (⿰)",
        '⿱' to "Top + Bottom (⿱)",
        '⿲' to "Left · Mid · Right (⿲)",
        '⿳' to "Top · Mid · Bottom (⿳)",
        '⿴' to "Full Enclosure (⿴)",
        '⿵' to "Surround from Top (⿵)",
        '⿶' to "Surround from Bottom (⿶)",
        '⿷' to "Surround from Left (⿷)",
        '⿸' to "Upper-Left Surround (⿸)",
        '⿹' to "Upper-Right Surround (⿹)",
        '⿺' to "Lower-Left Surround (⿺)",
        '⿻' to "Overlaid Structure (⿻)"
    )

    fun extractStructureLabel(decomposition: String): String {
        val firstOp = decomposition.firstOrNull { it in IDS_OPERATORS }
        return if (firstOp != null) {
            IDS_OPERATORS[firstOp] ?: "Compound"
        } else if (decomposition.isBlank() || decomposition == "？") {
            "Single / Atomic Character"
        } else {
            "Atomic Unit"
        }
    }

    fun extractComponentGlyphs(character: String, decomposition: String): List<String> {
        if (decomposition.isBlank()) return emptyList()
        return decomposition
            .filter { ch -> ch !in IDS_OPERATORS && ch != '？' && ch != '?' && !ch.isWhitespace() }
            .map { it.toString() }
            .filter { it != character }
            .distinct()
    }

    fun formatFormationType(rawType: String): Pair<String, String> = when (rawType.trim().lowercase()) {
        "pictophonetic" -> "Phono-Semantic" to "形声字"
        "ideographic" -> "Compound Ideograph" to "会意字"
        "pictographic" -> "Pictograph" to "象形字"
        else -> "Character Compound" to "汉字结构"
    }

    fun extractHanziCharacters(text: String): List<String> {
        return text.filter { ch ->
            val code = ch.code
            (code in 0x4E00..0x9FFF) || (code in 0x3400..0x4DBF)
        }.map { it.toString() }
    }
}
