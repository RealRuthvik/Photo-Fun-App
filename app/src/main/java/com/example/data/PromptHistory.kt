package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class PromptHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val dateReceived: Long
)
