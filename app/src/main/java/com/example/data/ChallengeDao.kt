package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ChallengeLog>>

    @Query("SELECT * FROM challenge_log WHERE dateId = :dateId ORDER BY timestamp ASC")
    fun getLogsByDateFlow(dateId: String): Flow<List<ChallengeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChallengeLog)
}
