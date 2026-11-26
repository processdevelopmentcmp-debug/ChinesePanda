package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HskVocabularyDao {

    @Query("SELECT * FROM hsk_vocabulary ORDER BY hsk_level ASC, topic_id ASC, word_key ASC")
    fun observeAllVocabulary(): Flow<List<HskVocabularyEntity>>

    @Query(
        """
        SELECT * FROM hsk_vocabulary
        ORDER BY CASE WHEN example_hanzi != '' THEN 0 ELSE 1 END,
                 hsk_level ASC,
                 word_key ASC
        LIMIT :limit
        """
    )
    fun observeFeaturedVocabulary(limit: Int = 150): Flow<List<HskVocabularyEntity>>

    @Query("SELECT COUNT(*) FROM hsk_vocabulary")
    fun observeTotalVocabularyCount(): Flow<Int>

    @Query(
        """
        SELECT hsk_level AS hskLevel,
               COUNT(*) AS totalCount,
               SUM(CASE WHEN writable = 1 THEN 1 ELSE 0 END) AS writableCount
        FROM hsk_vocabulary
        GROUP BY hsk_level
        """
    )
    fun observeLevelCounts(): Flow<List<LevelCountRow>>

    @Query(
        """
        SELECT topic_id AS topicId, COUNT(*) AS count
        FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND (:writableOnly = 0 OR writable = 1)
          AND (:hsk30NewOnly = 0 OR is_hsk30_new_term = 1)
        GROUP BY topic_id
        """
    )
    fun observeTopicCounts(
        levelMin: Int,
        levelMax: Int,
        writableOnly: Int,
        hsk30NewOnly: Int
    ): Flow<List<TopicCountRow>>

    @Query(
        """
        SELECT COUNT(*) FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND (:topicId IS NULL OR topic_id = :topicId)
          AND (:writableOnly = 0 OR writable = 1)
          AND (:hsk30NewOnly = 0 OR is_hsk30_new_term = 1)
          AND (
              :query = ''
              OR hanzi LIKE '%' || :query || '%'
              OR pinyin LIKE '%' || :query || '%'
              OR english LIKE '%' || :query || '%'
              OR part_of_speech LIKE '%' || :query || '%'
          )
        """
    )
    fun observeFilteredCount(
        levelMin: Int,
        levelMax: Int,
        topicId: Int?,
        query: String,
        writableOnly: Int,
        hsk30NewOnly: Int
    ): Flow<Int>

    @Query(
        """
        SELECT * FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND (:topicId IS NULL OR topic_id = :topicId)
          AND (:writableOnly = 0 OR writable = 1)
          AND (:hsk30NewOnly = 0 OR is_hsk30_new_term = 1)
          AND (
              :query = ''
              OR hanzi LIKE '%' || :query || '%'
              OR pinyin LIKE '%' || :query || '%'
              OR english LIKE '%' || :query || '%'
              OR part_of_speech LIKE '%' || :query || '%'
          )
        ORDER BY CASE WHEN example_hanzi != '' THEN 0 ELSE 1 END,
                 hsk_level ASC,
                 topic_id ASC,
                 word_key ASC
        LIMIT :limit
        """
    )
    fun observeFilteredPage(
        levelMin: Int,
        levelMax: Int,
        topicId: Int?,
        query: String,
        writableOnly: Int,
        hsk30NewOnly: Int,
        limit: Int
    ): Flow<List<HskVocabularyEntity>>

    @Query("SELECT * FROM hsk_vocabulary WHERE hsk_level = :level ORDER BY topic_id ASC, word_key ASC")
    fun observeVocabularyByLevel(level: Int): Flow<List<HskVocabularyEntity>>

    @Query("SELECT * FROM hsk_vocabulary WHERE hsk_band = :band ORDER BY hsk_level ASC, topic_id ASC")
    fun observeVocabularyByBand(band: String): Flow<List<HskVocabularyEntity>>

    @Query("SELECT * FROM hsk_vocabulary WHERE topic_id = :topicId ORDER BY hsk_level ASC, word_key ASC")
    fun observeVocabularyByTopic(topicId: Int): Flow<List<HskVocabularyEntity>>

    @Query("SELECT * FROM hsk_vocabulary WHERE writable = 1 ORDER BY hsk_level ASC, word_key ASC")
    fun observeWritableVocabulary(): Flow<List<HskVocabularyEntity>>

    @Query(
        """
        SELECT * FROM hsk_vocabulary
        WHERE hanzi LIKE '%' || :query || '%'
           OR pinyin LIKE '%' || :query || '%'
           OR english LIKE '%' || :query || '%'
        ORDER BY hsk_level ASC, word_key ASC
        LIMIT 100
        """
    )
    fun searchVocabulary(query: String): Flow<List<HskVocabularyEntity>>

    @Query("SELECT * FROM hsk_topics ORDER BY topic_id ASC")
    fun observeAllTopics(): Flow<List<HskTopicEntity>>

    @Query(
        """
        SELECT hsk_level AS hskLevel,
               COUNT(*) AS totalCount,
               SUM(CASE WHEN writable = 1 THEN 1 ELSE 0 END) AS writableCount
        FROM hsk_vocabulary
        WHERE topic_id = :topicId
        GROUP BY hsk_level
        """
    )
    fun observeTopicLevelCounts(topicId: Int): Flow<List<LevelCountRow>>

    @Query(
        """
        SELECT COUNT(*) FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND (:topicId IS NULL OR topic_id = :topicId)
        """
    )
    fun observeScopeWordCount(
        levelMin: Int,
        levelMax: Int,
        topicId: Int?
    ): Flow<Int>

    @Query(
        """
        SELECT * FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND (:topicId IS NULL OR topic_id = :topicId)
        ORDER BY CASE WHEN example_hanzi != '' THEN 0 ELSE 1 END,
                 hsk_level ASC,
                 topic_id ASC,
                 word_key ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeChapterVocabulary(
        levelMin: Int,
        levelMax: Int,
        topicId: Int?,
        limit: Int,
        offset: Int
    ): Flow<List<HskVocabularyEntity>>

    @Query(
        """
        SELECT * FROM hsk_vocabulary
        WHERE hsk_level BETWEEN :levelMin AND :levelMax
          AND hanzi NOT IN (:excludeHanzi)
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getDistractorWords(
        levelMin: Int,
        levelMax: Int,
        excludeHanzi: List<String>,
        limit: Int
    ): List<HskVocabularyEntity>

    @Query("SELECT * FROM hsk_vocabulary WHERE word_key = :wordKey LIMIT 1")
    suspend fun getVocabularyByKey(wordKey: String): HskVocabularyEntity?

    @Query("SELECT * FROM hsk_vocabulary WHERE hanzi = :hanzi LIMIT 1")
    suspend fun getVocabularyByHanzi(hanzi: String): HskVocabularyEntity?

    @Query("SELECT hanzi FROM hsk_vocabulary WHERE example_hanzi != ''")
    suspend fun getCuratedHanziWithExamples(): List<String>

    @Query("SELECT COUNT(*) FROM hsk_vocabulary")
    suspend fun countVocabulary(): Int

    @Query("SELECT COUNT(*) FROM hsk_topics")
    suspend fun countTopics(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVocabulary(words: List<HskVocabularyEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllVocabularyIgnore(words: List<HskVocabularyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTopics(topics: List<HskTopicEntity>)
}
