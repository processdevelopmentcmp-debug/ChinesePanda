package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HanziEtymologyDao {

    @Query("SELECT COUNT(*) FROM hanzi_character_info")
    suspend fun countCharacters(): Int

    @Query("SELECT * FROM hanzi_character_info WHERE character IN (:characters)")
    suspend fun getCharacters(characters: List<String>): List<HanziCharacterEntity>

    @Query("SELECT * FROM hanzi_character_info WHERE character = :character LIMIT 1")
    suspend fun getCharacter(character: String): HanziCharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(items: List<HanziCharacterEntity>)

    @Query("SELECT * FROM word_story_cache WHERE hanzi = :hanzi LIMIT 1")
    suspend fun getCachedWordStory(hanzi: String): WordStoryCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWordStory(story: WordStoryCacheEntity)
}
