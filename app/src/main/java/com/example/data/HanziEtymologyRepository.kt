package com.example.data

import android.util.Log
import com.example.data.local.AiCharacterNote
import com.example.data.local.CharacterAnatomy
import com.example.data.local.ComponentRole
import com.example.data.local.HanziComponentPart
import com.example.data.local.HanziEtymologyDao
import com.example.data.local.IdsDecompositionParser
import com.example.data.local.RelatedFamilyWord
import com.example.data.local.WordEtymologyStory
import com.example.data.local.WordStoryCacheEntity
import com.example.model.VocabWord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val ETYM_REPO_TAG = "HanziEtymologyRepo"
private const val MIN_CHARACTER_DICT_COUNT = 5_000

/**
 * Coordinates instant offline character decomposition (Make Me a Hanzi ~9,574 characters —
 * Kangxi radicals, IDS layouts, semantic & phonetic components) and permanent SQLite caching
 * of CharacterPop-style stories + HanziCraft-style word-making histories generated via Gemini.
 */
class HanziEtymologyRepository(
    private val dao: HanziEtymologyDao,
    private val aiRepo: PandaAiRepository
) {

    /**
     * Streams all ~9,574 character decompositions into local Room SQLite once for 0ms offline lookup.
     */
    suspend fun ensureCharacterDictionarySynced(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.countCharacters()
            if (existing >= MIN_CHARACTER_DICT_COUNT) {
                return@withContext Result.success(existing)
            }
            val characters = HanziDecompositionDownloader.downloadAndParseAllCharacters()
            characters.chunked(500).forEach { batch ->
                dao.insertCharacters(batch)
            }
            val finalCount = dao.countCharacters()
            Log.i(ETYM_REPO_TAG, "Cached $finalCount character decompositions offline.")
            Result.success(finalCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETYM_REPO_TAG, "Character decomposition sync deferred", e)
            Result.failure(e)
        }
    }

    /**
     * Instant offline HanziCraft / CharacterPop structural breakdown for every character in [hanzi].
     */
    suspend fun getWordAnatomy(hanzi: String): List<CharacterAnatomy> = withContext(Dispatchers.IO) {
        val chars = IdsDecompositionParser.extractHanziCharacters(hanzi).distinct()
        if (chars.isEmpty()) return@withContext emptyList()

        val primaryMap = dao.getCharacters(chars).associateBy { it.character }

        // Gather all sub-component and radical glyphs so we can resolve their meanings in ONE query
        val subGlyphs = linkedSetOf<String>()
        primaryMap.values.forEach { entry ->
            if (entry.radical.isNotBlank()) subGlyphs += entry.radical
            if (entry.semanticComponent.isNotBlank()) subGlyphs += entry.semanticComponent
            if (entry.phoneticComponent.isNotBlank()) subGlyphs += entry.phoneticComponent
            subGlyphs += IdsDecompositionParser.extractComponentGlyphs(entry.character, entry.decomposition)
        }

        val subMap = if (subGlyphs.isNotEmpty()) {
            dao.getCharacters(subGlyphs.toList()).associateBy { it.character }
        } else {
            emptyMap()
        }

        chars.map { ch ->
            val entity = primaryMap[ch]
            if (entity == null) {
                CharacterAnatomy(
                    character = ch,
                    pinyin = "",
                    definition = COMMON_COMPONENT_MEANINGS[ch].orEmpty(),
                    radical = ch,
                    radicalMeaning = COMMON_COMPONENT_MEANINGS[ch].orEmpty(),
                    decompositionRaw = "",
                    structureLabel = "Tap Story & History for full breakdown",
                    formationType = "Chinese Character",
                    formationBadgeZh = "汉字",
                    semanticComponent = "",
                    phoneticComponent = "",
                    etymologyHint = "",
                    components = emptyList()
                )
            } else {
                val (formationType, formationZh) = IdsDecompositionParser.formatFormationType(entity.etymologyType)
                val rawComponents = IdsDecompositionParser.extractComponentGlyphs(entity.character, entity.decomposition)
                val componentParts = rawComponents.map { glyph ->
                    val sub = subMap[glyph]
                    val role = when {
                        entity.semanticComponent.isNotBlank() && glyph == entity.semanticComponent -> ComponentRole.SEMANTIC
                        entity.phoneticComponent.isNotBlank() && glyph == entity.phoneticComponent -> ComponentRole.PHONETIC
                        entity.radical.isNotBlank() && glyph == entity.radical -> ComponentRole.RADICAL
                        else -> ComponentRole.BUILDING_BLOCK
                    }
                    val meaning = COMMON_COMPONENT_MEANINGS[glyph]
                        ?: sub?.definition?.substringBefore(";")?.trim().orEmpty()
                    HanziComponentPart(
                        glyph = glyph,
                        pinyin = sub?.pinyin.orEmpty(),
                        meaning = meaning,
                        role = role
                    )
                }

                val radicalMeaning = COMMON_COMPONENT_MEANINGS[entity.radical]
                    ?: subMap[entity.radical]?.definition?.substringBefore(";")?.trim().orEmpty()

                CharacterAnatomy(
                    character = entity.character,
                    pinyin = entity.pinyin,
                    definition = entity.definition,
                    radical = entity.radical,
                    radicalMeaning = radicalMeaning,
                    decompositionRaw = entity.decomposition,
                    structureLabel = IdsDecompositionParser.extractStructureLabel(entity.decomposition),
                    formationType = formationType,
                    formationBadgeZh = formationZh,
                    semanticComponent = entity.semanticComponent,
                    phoneticComponent = entity.phoneticComponent,
                    etymologyHint = entity.etymologyHint,
                    components = componentParts
                )
            }
        }
    }

    /**
     * Loads the cached CharacterPop story & HanziCraft word history from local SQLite,
     * or generates & caches it permanently via Firebase AI Logic (Gemini).
     */
    suspend fun getOrGenerateWordStory(
        word: VocabWord,
        anatomy: List<CharacterAnatomy>,
        forceRefresh: Boolean = false
    ): Result<WordEtymologyStory> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val cached = dao.getCachedWordStory(word.hanzi)
            if (cached != null) {
                return@withContext Result.success(cached.toDomain())
            }
        }

        val generated = aiRepo.generateWordEtymologyStory(word, anatomy)
        generated.onSuccess { story ->
            dao.upsertWordStory(story.toCacheEntity())
        }
        generated
    }

    private fun WordEtymologyStory.toCacheEntity(): WordStoryCacheEntity {
        val notesJson = JSONArray().apply {
            characterNotes.forEach { n ->
                put(
                    JSONObject().apply {
                        put("character", n.character)
                        put("formationExplanation", n.formationExplanation)
                        put("miniStory", n.miniStory)
                    }
                )
            }
        }.toString()

        val relatedJson = JSONArray().apply {
            relatedWords.forEach { r ->
                put(
                    JSONObject().apply {
                        put("hanzi", r.hanzi)
                        put("pinyin", r.pinyin)
                        put("english", r.english)
                    }
                )
            }
        }.toString()

        return WordStoryCacheEntity(
            hanzi = hanzi,
            wordMakingLogic = wordMakingLogic,
            characterPopStory = characterPopStory,
            historicalOrigin = historicalOrigin,
            characterNotesJson = notesJson,
            relatedWordsJson = relatedJson,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    private fun WordStoryCacheEntity.toDomain(): WordEtymologyStory {
        val notes = mutableListOf<AiCharacterNote>()
        try {
            val arr = JSONArray(characterNotesJson)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                notes += AiCharacterNote(
                    character = o.optString("character", ""),
                    formationExplanation = o.optString("formationExplanation", ""),
                    miniStory = o.optString("miniStory", "")
                )
            }
        } catch (_: Exception) {
        }

        val related = mutableListOf<RelatedFamilyWord>()
        try {
            val arr = JSONArray(relatedWordsJson)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                related += RelatedFamilyWord(
                    hanzi = o.optString("hanzi", ""),
                    pinyin = o.optString("pinyin", ""),
                    english = o.optString("english", "")
                )
            }
        } catch (_: Exception) {
        }

        return WordEtymologyStory(
            hanzi = hanzi,
            wordMakingLogic = wordMakingLogic,
            characterPopStory = characterPopStory,
            historicalOrigin = historicalOrigin,
            characterNotes = notes,
            relatedWords = related
        )
    }

    companion object {
        // Concise meanings for common Kangxi radicals & variant component forms that aren't standalone words
        private val COMMON_COMPONENT_MEANINGS = mapOf(
            "亻" to "person", "人" to "person", "氵" to "water", "水" to "water",
            "扌" to "hand", "手" to "hand", "讠" to "speech / words", "言" to "speech",
            "心" to "heart / mind", "忄" to "heart / emotion", "口" to "mouth", "囗" to "enclosure",
            "女" to "woman", "子" to "child", "木" to "tree / wood", "艹" to "grass / plant",
            "火" to "fire", "灬" to "fire / heat", "日" to "sun / day", "月" to "moon / flesh",
            "土" to "earth / soil", "金" to "metal / gold", "钅" to "metal", "玉" to "jade",
            "王" to "king / jade", "目" to "eye", "耳" to "ear", "足" to "foot", "⻊" to "foot",
            "走" to "walk", "辶" to "movement / road", "彳" to "step / street", "门" to "gate / door",
            "户" to "door / household", "宀" to "roof / house", "广" to "shelter / building",
            "厂" to "cliff", "山" to "mountain", "石" to "stone", "雨" to "rain", "风" to "wind",
            "田" to "field", "力" to "power / strength", "刀" to "knife", "刂" to "knife",
            "戈" to "weapon / spear", "弓" to "bow", "矢" to "arrow", "车" to "vehicle",
            "舟" to "boat", "马" to "horse", "牛" to "ox / cow", "羊" to "sheep", "犬" to "dog",
            "犭" to "animal", "鸟" to "bird", "鱼" to "fish", "虫" to "insect", "贝" to "shell / money",
            "糸" to "silk / thread", "纟" to "silk / thread", "衣" to "clothing", "衤" to "clothing",
            "食" to "food / eat", "饣" to "food / eat", "米" to "rice", "禾" to "grain / crop",
            "竹" to "bamboo", "⺮" to "bamboo", "页" to "head / page", "见" to "see",
            "欠" to "yawn / owe", "止" to "stop / foot", "殳" to "strike", "攵" to "tap / action",
            "斤" to "axe", "方" to "square / direction", "示" to "spirit / ritual", "礻" to "spirit / ritual",
            "病" to "illness", "疒" to "sickness", "穴" to "cave / hole", "立" to "stand",
            "白" to "white", "黑" to "black", "青" to "blue-green", "红" to "red",
            "大" to "big", "小" to "small", "工" to "work / craft", "己" to "self",
            "巾" to "cloth", "干" to "shield / dry", "夕" to "evening", "寸" to "inch / hand",
            "尸" to "body", "弓" to "bow", "彡" to "bristles / pattern", "阝" to "mound / city",
            "隹" to "short-tailed bird", "皿" to "dish / vessel", "酉" to "wine jar", "里" to "village / mile"
        )
    }
}
