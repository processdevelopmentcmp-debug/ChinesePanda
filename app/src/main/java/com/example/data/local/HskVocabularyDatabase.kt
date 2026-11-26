package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HskVocabularyEntity::class,
        HskTopicEntity::class,
        HanziCharacterEntity::class,
        WordStoryCacheEntity::class,
        DailyStudyLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class HskVocabularyDatabase : RoomDatabase() {

    abstract fun hskVocabularyDao(): HskVocabularyDao
    abstract fun hanziEtymologyDao(): HanziEtymologyDao
    abstract fun dailyStudyLogDao(): DailyStudyLogDao

    companion object {
        private const val DB_NAME = "pandachinese_hsk30_vocab.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `hanzi_character_info` (
                        `character` TEXT NOT NULL,
                        `definition` TEXT NOT NULL,
                        `pinyin` TEXT NOT NULL,
                        `radical` TEXT NOT NULL,
                        `decomposition` TEXT NOT NULL,
                        `etymology_type` TEXT NOT NULL,
                        `semantic_component` TEXT NOT NULL,
                        `phonetic_component` TEXT NOT NULL,
                        `etymology_hint` TEXT NOT NULL,
                        PRIMARY KEY(`character`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `word_story_cache` (
                        `hanzi` TEXT NOT NULL,
                        `word_making_logic` TEXT NOT NULL,
                        `character_pop_story` TEXT NOT NULL,
                        `historical_origin` TEXT NOT NULL,
                        `character_notes_json` TEXT NOT NULL,
                        `related_words_json` TEXT NOT NULL,
                        `updated_at_ms` INTEGER NOT NULL,
                        PRIMARY KEY(`hanzi`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_study_logs` (
                        `user_id` TEXT NOT NULL,
                        `date_key` TEXT NOT NULL,
                        `xp_earned` INTEGER NOT NULL,
                        `words_learned` INTEGER NOT NULL,
                        `lessons_completed` INTEGER NOT NULL,
                        `srs_reviewed` INTEGER NOT NULL,
                        `stories_explored` INTEGER NOT NULL,
                        `practice_sessions` INTEGER NOT NULL,
                        `study_minutes` INTEGER NOT NULL,
                        `updated_at_ms` INTEGER NOT NULL,
                        PRIMARY KEY(`user_id`, `date_key`)
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: HskVocabularyDatabase? = null

        fun getInstance(context: Context): HskVocabularyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HskVocabularyDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
