package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStudyLogDao {

    @Query(
        """
        SELECT * FROM daily_study_logs
        WHERE user_id = :userId
        ORDER BY date_key ASC
        """
    )
    fun observeAllLogsForUser(userId: String): Flow<List<DailyStudyLogEntity>>

    @Query(
        """
        SELECT * FROM daily_study_logs
        WHERE user_id = :userId AND date_key = :dateKey
        LIMIT 1
        """
    )
    suspend fun getLogForDay(userId: String, dateKey: String): DailyStudyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyStudyLogEntity)
}
