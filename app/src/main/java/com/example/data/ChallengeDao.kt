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

    @Query("SELECT * FROM challenge_log ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(): List<ChallengeLog>

    @Query("SELECT * FROM challenge_log WHERE dateId = :dateId ORDER BY timestamp ASC")
    fun getLogsByDateFlow(dateId: String): Flow<List<ChallengeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChallengeLog)

    @Query("SELECT * FROM prompt_history ORDER BY dateReceived DESC")
    fun getPromptHistory(): Flow<List<PromptHistory>>

    @Query("SELECT * FROM prompt_history ORDER BY dateReceived DESC")
    suspend fun getPromptHistorySync(): List<PromptHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPromptHistory(promptHistory: PromptHistory)

    @Query("DELETE FROM prompt_history WHERE prompt = :prompt")
    suspend fun deletePromptHistory(prompt: String)

    @Query("DELETE FROM challenge_log")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM prompt_history")
    suspend fun deleteAllPromptHistory()
}
