package com.example.data

import android.util.Log
import com.example.data.local.HskBand
import com.example.data.local.HskCatalogSyncState
import com.example.data.local.HskLevelSummary
import com.example.data.local.HskTopicEntity
import com.example.data.local.HskTopicListSection
import com.example.data.local.HskVocabularyDao
import com.example.data.local.HskVocabularyEntity
import com.example.data.local.HskVocabularyFilter
import com.example.data.local.toTopicCategory
import com.example.data.local.toVocabWord
import com.example.model.HskLevelMeta
import com.example.model.TopicCategory
import com.example.model.VocabWord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val REPO_TAG = "HskVocabRepo"
private const val FULL_CATALOG_THRESHOLD = 5_000

/**
 * Repository abstracting local Room storage for the structured HSK 3.0 vocabulary catalog
 * (all ~11,092 official HSK 3.0 words across Levels 1–9 and 14 real-life topics).
 *
 * Uses indexed SQLite queries + pagination for smooth 60fps browsing across 11,000+ words,
 * while user-authored custom vocabulary and SM-2 progress stay synced in Firestore via
 * [PandaFirestoreRepository].
 */
class HskVocabularyRepository(
    private val dao: HskVocabularyDao
) {

    val hskLevelRoadmap: List<HskLevelMeta> = HskCurriculumData.hskLevels

    private val _syncState = MutableStateFlow<HskCatalogSyncState>(HskCatalogSyncState.Idle)
    val syncState: StateFlow<HskCatalogSyncState> = _syncState.asStateFlow()

    val totalLocalVocabularyCount: Flow<Int> = dao.observeTotalVocabularyCount()

    val featuredVocabulary: Flow<List<VocabWord>> = dao.observeFeaturedVocabulary(limit = 160)
        .map { entities -> entities.map { it.toVocabWord() } }

    val allVocabulary: Flow<List<VocabWord>> = dao.observeAllVocabulary()
        .map { entities -> entities.map { it.toVocabWord() } }

    val allTopics: Flow<List<TopicCategory>> = dao.observeAllTopics()
        .map { entities -> entities.map { it.toTopicCategory() } }

    val levelSummaries: Flow<List<HskLevelSummary>> = dao.observeLevelCounts()
        .map { rows -> buildLevelSummariesFromRows(rows, emptyList()) }

    val topicSections: Flow<List<HskTopicListSection>> = combine(allTopics, featuredVocabulary) { topics, words ->
        val byTopic = words.groupBy { it.topicId }
        topics.map { topic ->
            val topicWords = byTopic[topic.id].orEmpty()
            HskTopicListSection(
                topic = topic,
                words = topicWords,
                levelsCovered = topicWords.map { it.hskLevel }.distinct().sorted()
            )
        }
    }

    fun observeLevelSummariesWithCustom(customWords: List<VocabWord>): Flow<List<HskLevelSummary>> {
        return dao.observeLevelCounts().map { rows ->
            buildLevelSummariesFromRows(rows, customWords)
        }
    }

    fun observeTopicCounts(
        filter: HskVocabularyFilter,
        customWords: List<VocabWord>
    ): Flow<Map<Int, Int>> {
        val (minLvl, maxLvl) = levelBounds(filter.levelFilter)
        val writeInt = if (filter.writableOnly) 1 else 0
        val newInt = if (filter.hsk30NewOnly) 1 else 0
        return dao.observeTopicCounts(minLvl, maxLvl, writeInt, newInt).map { rows ->
            val counts = rows.associate { it.topicId to it.count }.toMutableMap()
            filterVocabulary(customWords, filter.copy(topicId = null, searchQuery = "")).forEach { w ->
                counts[w.topicId] = (counts[w.topicId] ?: 0) + 1
            }
            counts
        }
    }

    fun observeFilteredPageWithTotal(
        filter: HskVocabularyFilter,
        customWords: List<VocabWord>
    ): Flow<Pair<List<VocabWord>, Int>> {
        val (minLvl, maxLvl) = levelBounds(filter.levelFilter)
        val writeInt = if (filter.writableOnly) 1 else 0
        val newInt = if (filter.hsk30NewOnly) 1 else 0
        val cleanQuery = filter.searchQuery.trim()
        val matchingCustom = filterVocabulary(customWords, filter)

        val pageFlow = dao.observeFilteredPage(
            levelMin = minLvl,
            levelMax = maxLvl,
            topicId = filter.topicId,
            query = cleanQuery,
            writableOnly = writeInt,
            hsk30NewOnly = newInt,
            limit = filter.pageSize.coerceAtLeast(20)
        )
        val countFlow = dao.observeFilteredCount(
            levelMin = minLvl,
            levelMax = maxLvl,
            topicId = filter.topicId,
            query = cleanQuery,
            writableOnly = writeInt,
            hsk30NewOnly = newInt
        )

        return combine(pageFlow, countFlow) { pageEntities, localTotal ->
            val localWords = pageEntities.map { it.toVocabWord() }
            val combined = if (matchingCustom.isNotEmpty()) {
                (matchingCustom + localWords).take(filter.pageSize.coerceAtLeast(20))
            } else {
                localWords
            }
            combined to (localTotal + matchingCustom.size)
        }
    }

    fun observeByHskLevel(level: Int): Flow<List<VocabWord>> {
        return dao.observeVocabularyByLevel(level.coerceIn(1, 9))
            .map { list -> list.map { it.toVocabWord() } }
    }

    fun observeByBand(band: HskBand): Flow<List<VocabWord>> {
        return dao.observeVocabularyByBand(band.name)
            .map { list -> list.map { it.toVocabWord() } }
    }

    fun observeByTopic(topicId: Int): Flow<List<VocabWord>> {
        return dao.observeVocabularyByTopic(topicId)
            .map { list -> list.map { it.toVocabWord() } }
    }

    fun observeWritable(): Flow<List<VocabWord>> {
        return dao.observeWritableVocabulary()
            .map { list -> list.map { it.toVocabWord() } }
    }

    fun search(query: String): Flow<List<VocabWord>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            featuredVocabulary
        } else {
            dao.searchVocabulary(trimmed).map { list -> list.map { it.toVocabWord() } }
        }
    }

    fun observeAllTopicCounts(customWords: List<VocabWord>): Flow<Map<Int, Int>> {
        return dao.observeTopicCounts(1, 9, 0, 0).map { rows ->
            val counts = rows.associate { it.topicId to it.count }.toMutableMap()
            customWords.forEach { w ->
                counts[w.topicId] = (counts[w.topicId] ?: 0) + 1
            }
            counts
        }
    }

    fun observeTopicLevelSummaries(topicId: Int): Flow<List<HskLevelSummary>> {
        return dao.observeTopicLevelCounts(topicId).map { rows ->
            buildLevelSummariesFromRows(rows, emptyList())
        }
    }

    fun observeScopeWordCount(levelFilter: String, topicId: Int?): Flow<Int> {
        val (minLvl, maxLvl) = levelBounds(levelFilter)
        return dao.observeScopeWordCount(minLvl, maxLvl, topicId)
    }

    fun observeChapterWords(
        levelFilter: String,
        topicId: Int?,
        chapterIndex: Int,
        chapterSize: Int = 100
    ): Flow<List<VocabWord>> {
        val (minLvl, maxLvl) = levelBounds(levelFilter)
        val safeOffset = (chapterIndex.coerceAtLeast(0)) * chapterSize
        return dao.observeChapterVocabulary(
            levelMin = minLvl,
            levelMax = maxLvl,
            topicId = topicId,
            limit = chapterSize,
            offset = safeOffset
        ).map { list -> list.map { it.toVocabWord() } }
    }

    suspend fun getDistractorWords(
        hskLevel: Int,
        excludeHanzi: List<String>,
        limit: Int = 16
    ): List<VocabWord> = withContext(Dispatchers.IO) {
        val safeExclude = excludeHanzi.ifEmpty { listOf("") }
        val (minLvl, maxLvl) = if (hskLevel >= 7) 7 to 9 else hskLevel.coerceIn(1, 6) to hskLevel.coerceIn(1, 6)
        val primary = dao.getDistractorWords(minLvl, maxLvl, safeExclude, limit)
            .map { it.toVocabWord() }
        if (primary.size >= 6) {
            primary
        } else {
            // Broaden across all levels if a small starter deck doesn't have enough distractors yet
            dao.getDistractorWords(1, 9, safeExclude, limit).map { it.toVocabWord() }
        }
    }

    suspend fun getWordByKey(wordKey: String): VocabWord? = withContext(Dispatchers.IO) {
        dao.getVocabularyByKey(wordKey)?.toVocabWord()
    }

    suspend fun getWordByHanzi(hanzi: String): VocabWord? = withContext(Dispatchers.IO) {
        dao.getVocabularyByHanzi(hanzi)?.toVocabWord()
    }

    suspend fun getWordsForTopics(topicIds: List<Int>, limit: Int = 30): List<VocabWord> =
        withContext(Dispatchers.IO) {
            if (topicIds.isEmpty()) return@withContext emptyList()
            dao.getWordsForTopics(topicIds, limit).map { it.toVocabWord() }
        }

    suspend fun getWordsForTopicNode(
        topicId: Int,
        minHsk: Int,
        maxHsk: Int,
        limit: Int = 10
    ): List<VocabWord> = withContext(Dispatchers.IO) {
        val exact = dao.getWordsForTopicAndLevelRange(topicId, minHsk, maxHsk, limit)
            .map { it.toVocabWord() }
        if (exact.size >= 4) exact
        else dao.getWordsForTopicAndLevelRange(topicId, 1, 9, limit).map { it.toVocabWord() }
    }

    suspend fun getShortcutWordsForLevel(targetLevel: Int, limit: Int = 18): List<VocabWord> =
        withContext(Dispatchers.IO) {
            val (minLvl, maxLvl) = if (targetLevel >= 7) 7 to 9 else targetLevel.coerceIn(1, 6) to targetLevel.coerceIn(1, 6)
            val exact = dao.getRandomWordsForLevelRange(minLvl, maxLvl, limit).map { it.toVocabWord() }
            if (exact.size >= 8) exact
            else dao.getRandomWordsForLevelRange(1, 9, limit).map { it.toVocabWord() }
        }

    suspend fun getWordsByHanziList(hanziSet: Set<String>): List<VocabWord> =
        withContext(Dispatchers.IO) {
            if (hanziSet.isEmpty()) return@withContext emptyList()
            val result = mutableListOf<VocabWord>()
            hanziSet.toList().chunked(400).forEach { chunk ->
                result.addAll(dao.getVocabularyByHanziList(chunk).map { it.toVocabWord() })
            }
            result
        }

    /**
     * Fetches the next [limit] unlearned HSK words for the user's chosen daily goal HSK scope,
     * scanning ordered pages in Room so already-learned words are skipped cleanly.
     */
    suspend fun getNextUnlearnedWordsForLevel(
        levelFilter: String,
        alreadyLearnedHanzi: Set<String>,
        limit: Int = 10
    ): List<VocabWord> = withContext(Dispatchers.IO) {
        val targetLimit = limit.coerceIn(1, 30)
        val (minLvl, maxLvl) = levelBounds(levelFilter)
        val collected = mutableListOf<VocabWord>()
        val seen = HashSet<String>()
        var offset = 0
        val pageSize = 200

        while (collected.size < targetLimit && offset <= 12_000) {
            val page = dao.getOrderedWordsForLevelRange(minLvl, maxLvl, pageSize, offset)
            if (page.isEmpty()) break
            for (entity in page) {
                if (entity.hanzi !in alreadyLearnedHanzi && seen.add(entity.hanzi)) {
                    collected.add(entity.toVocabWord())
                    if (collected.size >= targetLimit) break
                }
            }
            if (page.size < pageSize) break
            offset += pageSize
        }

        // If the user has mastered every word in the chosen level, fall back to fresh words from the level
        if (collected.isEmpty()) {
            return@withContext dao.getOrderedWordsForLevelRange(minLvl, maxLvl, targetLimit, 0)
                .map { it.toVocabWord() }
        }
        collected
    }

    /**
     * Populates the local Room database immediately with the curated starter HSK 3.0 vocabulary
     * and 14 topics so the UI renders with 0ms delay even before the full 11,000+ sync completes.
     */
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val seedTopics = buildSeedTopics()
        val seedVocab = buildSeedVocabulary()

        if (dao.countTopics() < seedTopics.size) {
            dao.insertAllTopics(seedTopics)
        }
        val currentCount = dao.countVocabulary()
        if (currentCount < seedVocab.size) {
            dao.insertAllVocabulary(seedVocab)
        }
        val postSeedCount = dao.countVocabulary()
        _syncState.value = HskCatalogSyncState.Ready(
            totalWords = postSeedCount,
            fullCatalogLoaded = postSeedCount >= FULL_CATALOG_THRESHOLD
        )
    }

    /**
     * Downloads and caches all ~11,092 official HSK 3.0 words (Levels 1–9) into Room SQLite
     * from the open-source MIT-licensed complete-hsk-vocabulary dataset.
     * Preserves all curated starter words (with example sentences) untouched.
     */
    suspend fun syncFullHsk30Catalog(force: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        val existingCount = dao.countVocabulary()
        if (!force && existingCount >= FULL_CATALOG_THRESHOLD) {
            _syncState.value = HskCatalogSyncState.Ready(
                totalWords = existingCount,
                fullCatalogLoaded = true
            )
            return@withContext Result.success(existingCount)
        }

        _syncState.value = HskCatalogSyncState.Syncing(5, "Preparing HSK 3.0 11,000-word catalog...")
        try {
            val curatedHanzi = dao.getCuratedHanziWithExamples().toHashSet()
            val downloaded = Hsk30CatalogDownloader.downloadAndParseAllHsk30Words(
                skipHanzi = curatedHanzi,
                onProgress = { pct, status ->
                    _syncState.value = HskCatalogSyncState.Syncing(pct, status)
                }
            )

            val batches = downloaded.chunked(500)
            batches.forEachIndexed { idx, batch ->
                dao.insertAllVocabularyIgnore(batch)
                val savedSoFar = ((idx + 1) * 500).coerceAtMost(downloaded.size)
                val pct = 72 + (((idx + 1) * 26) / batches.size.coerceAtLeast(1))
                _syncState.value = HskCatalogSyncState.Syncing(
                    progressPercent = pct.coerceIn(72, 98),
                    status = "Saving $savedSoFar of ${downloaded.size} words offline..."
                )
            }

            val finalCount = dao.countVocabulary()
            _syncState.value = HskCatalogSyncState.Ready(
                totalWords = finalCount,
                fullCatalogLoaded = finalCount >= FULL_CATALOG_THRESHOLD
            )
            Result.success(finalCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(REPO_TAG, "Failed syncing full 11k HSK 3.0 catalog", e)
            val fallbackCount = dao.countVocabulary()
            _syncState.value = HskCatalogSyncState.Error(
                message = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Could not download the 11,000-word HSK 3.0 catalog. Check internet and retry.",
                currentWords = fallbackCount
            )
            Result.failure(e)
        }
    }

    fun buildLevelSummaries(words: List<VocabWord>): List<HskLevelSummary> {
        return hskLevelRoadmap.map { meta ->
            val matching = if (meta.level >= 7) {
                words.filter { it.hskLevel >= 7 }
            } else {
                words.filter { it.hskLevel == meta.level }
            }
            HskLevelSummary(
                meta = meta,
                band = HskBand.fromLevel(meta.level),
                activeWordCount = matching.size,
                writableCount = matching.count { it.writable }
            )
        }
    }

    private fun buildLevelSummariesFromRows(
        rows: List<com.example.data.local.LevelCountRow>,
        customWords: List<VocabWord>
    ): List<HskLevelSummary> {
        return hskLevelRoadmap.map { meta ->
            val matchingRows = if (meta.level >= 7) {
                rows.filter { it.hskLevel >= 7 }
            } else {
                rows.filter { it.hskLevel == meta.level }
            }
            val matchingCustom = if (meta.level >= 7) {
                customWords.filter { it.hskLevel >= 7 }
            } else {
                customWords.filter { it.hskLevel == meta.level }
            }
            HskLevelSummary(
                meta = meta,
                band = HskBand.fromLevel(meta.level),
                activeWordCount = matchingRows.sumOf { it.totalCount } + matchingCustom.size,
                writableCount = matchingRows.sumOf { it.writableCount } + matchingCustom.count { it.writable }
            )
        }
    }

    private fun levelBounds(levelFilter: String): Pair<Int, Int> = when (levelFilter) {
        "ALL" -> 1 to 9
        "7+" -> 7 to 9
        else -> {
            val lvl = levelFilter.toIntOrNull()?.coerceIn(1, 9) ?: 1
            lvl to lvl
        }
    }

    fun filterVocabulary(
        words: List<VocabWord>,
        filter: HskVocabularyFilter
    ): List<VocabWord> {
        val query = filter.searchQuery.trim().lowercase()
        return words.filter { word ->
            val levelOk = when (filter.levelFilter) {
                "ALL" -> true
                "7+" -> word.hskLevel >= 7
                else -> word.hskLevel == filter.levelFilter.toIntOrNull()
            }
            val topicOk = filter.topicId == null || word.topicId == filter.topicId
            val writeOk = !filter.writableOnly || word.writable
            val newTermOk = !filter.hsk30NewOnly || word.isHsk30NewTerm
            val searchOk = query.isEmpty() ||
                word.hanzi.contains(query) ||
                word.pinyin.lowercase().contains(query) ||
                word.english.lowercase().contains(query) ||
                word.partOfSpeech.lowercase().contains(query)
            levelOk && topicOk && writeOk && newTermOk && searchOk
        }
    }

    private fun buildSeedTopics(): List<HskTopicEntity> =
        HskCurriculumData.topics.map { topic ->
            HskTopicEntity(
                topicId = topic.id,
                nameEn = topic.nameEn,
                nameZh = topic.nameZh,
                tag = topic.tag,
                minHskLevel = topic.minHsk
            )
        }

    private fun buildSeedVocabulary(): List<HskVocabularyEntity> {
        val baseEntities = HskCurriculumData.builtInVocab.map { word ->
            val isNewTerm = word.english.contains("HSK 3.0", ignoreCase = true) ||
                word.hanzi in HSK30_NEW_HIGHLIGHT_TERMS
            HskVocabularyEntity(
                wordKey = word.key,
                hanzi = word.hanzi,
                pinyin = word.pinyin,
                english = word.english.replace(" (HSK 3.0)", ""),
                hskLevel = word.hskLevel.coerceIn(1, 9),
                hskBand = HskBand.fromLevel(word.hskLevel).name,
                topicId = word.topicId.coerceIn(1, 14),
                partOfSpeech = inferPartOfSpeech(word),
                writable = word.writable,
                isHsk30NewTerm = isNewTerm,
                exampleHanzi = word.exampleHanzi,
                examplePinyin = word.examplePinyin,
                exampleEnglish = word.exampleEnglish
            )
        }
        return baseEntities + EXTRA_HSK30_ENTRIES
    }

    private fun inferPartOfSpeech(word: VocabWord): String = when {
        word.hanzi.length == 4 && word.hskLevel >= 7 -> "Idiom (Chengyu)"
        word.topicId == 2 && word.hanzi in setOf("个", "本", "杯", "次") -> "Measure Word"
        word.topicId == 14 -> "Connector"
        word.topicId == 6 -> "Verb"
        word.topicId == 13 -> "Adjective"
        word.topicId == 1 && word.hanzi in setOf("我", "你", "您") -> "Pronoun"
        word.topicId == 1 -> "Expression"
        else -> "Noun / Phrase"
    }

    companion object {
        private val HSK30_NEW_HIGHLIGHT_TERMS = setOf(
            "扫码", "外卖", "豆浆", "高铁", "充值", "电商", "直播", "人工智能", "碳中和", "新能源", "短视频", "网约车"
        )

        private val EXTRA_HSK30_ENTRIES: List<HskVocabularyEntity> = listOf(
            HskVocabularyEntity(
                wordKey = "w1009",
                hanzi = "短视频",
                pinyin = "duǎn shì pín",
                english = "Short-form video",
                hskLevel = 4,
                hskBand = HskBand.INTERMEDIATE.name,
                topicId = 10,
                partOfSpeech = "Noun",
                writable = true,
                isHsk30NewTerm = true,
                exampleHanzi = "很多人通过短视频学中文。",
                examplePinyin = "Hěn duō rén tōngguò duǎnshìpín xué Zhōngwén.",
                exampleEnglish = "Many people learn Chinese through short videos."
            ),
            HskVocabularyEntity(
                wordKey = "w807",
                hanzi = "网约车",
                pinyin = "wǎng yuē chē",
                english = "Ride-hailing car",
                hskLevel = 4,
                hskBand = HskBand.INTERMEDIATE.name,
                topicId = 8,
                partOfSpeech = "Noun",
                writable = true,
                isHsk30NewTerm = true,
                exampleHanzi = "下雨天叫网约车很方便。",
                examplePinyin = "Xiàyǔ tiān jiào wǎngyuēchē hěn fāngbiàn.",
                exampleEnglish = "Calling a ride-hailing car on rainy days is very convenient."
            ),
            HskVocabularyEntity(
                wordKey = "w1206",
                hanzi = "新能源",
                pinyin = "xīn néng yuán",
                english = "New / renewable energy",
                hskLevel = 5,
                hskBand = HskBand.INTERMEDIATE.name,
                topicId = 12,
                partOfSpeech = "Noun",
                writable = true,
                isHsk30NewTerm = true,
                exampleHanzi = "街上的新能源汽车越来越多。",
                examplePinyin = "Jiē shàng de xīn néngyuán qìchē yuèláiyuè duō.",
                exampleEnglish = "There are more and more new-energy vehicles on the streets."
            ),
            HskVocabularyEntity(
                wordKey = "w907",
                hanzi = "跨文化",
                pinyin = "kuà wén huà",
                english = "Cross-cultural",
                hskLevel = 7,
                hskBand = HskBand.ADVANCED.name,
                topicId = 9,
                partOfSpeech = "Adjective",
                writable = false,
                isHsk30NewTerm = true,
                exampleHanzi = "跨文化交流需要尊重与理解。",
                examplePinyin = "Kuàwénhuà jiāoliú xūyào zūnzhòng yǔ lǐjiě.",
                exampleEnglish = "Cross-cultural communication requires respect and understanding."
            ),
            HskVocabularyEntity(
                wordKey = "w1306",
                hanzi = "一丝不苟",
                pinyin = "yì sī bù gǒu",
                english = "Meticulous / thorough",
                hskLevel = 8,
                hskBand = HskBand.ADVANCED.name,
                topicId = 13,
                partOfSpeech = "Idiom (Chengyu)",
                writable = false,
                isHsk30NewTerm = false,
                exampleHanzi = "她写汉字总是一丝不苟。",
                examplePinyin = "Tā xiě Hànzì zǒngshì yìsībùgǒu.",
                exampleEnglish = "She is always meticulous when writing Chinese characters."
            ),
            HskVocabularyEntity(
                wordKey = "w1409",
                hanzi = "综上所述",
                pinyin = "zōng shàng suǒ shù",
                english = "In summary / to sum up",
                hskLevel = 9,
                hskBand = HskBand.ADVANCED.name,
                topicId = 14,
                partOfSpeech = "Connector",
                writable = false,
                isHsk30NewTerm = false,
                exampleHanzi = "综上所述，坚持每日听读至关重要。",
                examplePinyin = "Zōngshàng suǒshù, jiānchí měirì tīngdú zhìguān zhòngyào.",
                exampleEnglish = "In summary, daily listening and reading practice is vital."
            )
        )
    }
}
