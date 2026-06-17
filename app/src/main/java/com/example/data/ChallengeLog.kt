package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_log")
data class ChallengeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateId: String, // e.g. "2023-10-25"
    val prompt: String,
    val imagePath: String, // path to local file
    val timestamp: Long
)
