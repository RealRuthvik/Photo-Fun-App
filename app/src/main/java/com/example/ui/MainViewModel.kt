package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChallengeLog
import com.example.data.PromptDatabase
import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(
    private val database: AppDatabase,
    val settingsRepo: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val dao = database.challengeDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val allLogs = dao.getAllLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _currentDateId = MutableStateFlow(getCurrentDateString())

    val todayLogs: StateFlow<List<ChallengeLog>> = _currentDateId
        .combine(dao.getAllLogs()) { date, logs ->
            logs.filter { it.dateId == date }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val activePrompt = MutableStateFlow("")
    val activePromptIsCustom = MutableStateFlow(false)

    val promptHistory = dao.getPromptHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            checkAndSetTodayPrompt()
        }
    }

    private suspend fun checkAndSetTodayPrompt() {
        val currentStr = getCurrentDateString()
        val storedDate = settingsRepo.todayDate.first()
        val storedPrompt = settingsRepo.todayPrompt.first()
        val storedIsCustom = settingsRepo.todayPromptIsCustom.first()

        if (storedDate != currentStr || storedPrompt.isNullOrEmpty()) {
            val newPrompt = PromptDatabase.getRandomPrompt()
            settingsRepo.setTodayPrompt(currentStr, newPrompt, false)
            activePrompt.value = newPrompt
            activePromptIsCustom.value = false
            
            // Save official prompt to history
            dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
        } else {
            activePrompt.value = storedPrompt
            activePromptIsCustom.value = storedIsCustom
        }
    }

    fun replacePromptWithCustom(newPrompt: String, savePreviousToHistory: Boolean = true) {
        viewModelScope.launch {
            val currentPrompt = activePrompt.value
            val isCustom = activePromptIsCustom.value
            val currentStr = getCurrentDateString()
            
            if (savePreviousToHistory && !isCustom && currentPrompt.isNotEmpty()) {
                // If it was an official prompt and not yet in history, it should already be there. 
                // However, we can proactively ensure the new prompt gets in history if we wanted.
                // Actually the requirement says: "Add the previous prompt to prompt history."
                // Since official prompts are added on generation, we don't need to add it again, 
                // unless we want custom prompts to also go to history? 
                // "Replace the currently active prompt. Add the previous prompt to prompt history." 
                // (It's already in history when generated, but we can just insert anyway, REPLACE handles duplicates if we had unique constraints. IGNORE will ignore duplicate primary keys but it doesn't have one on prompt).
                // Let's just insert to be safe, it's fine if it appears multiple times.
                dao.insertPromptHistory(com.example.data.PromptHistory(prompt = currentPrompt, dateReceived = System.currentTimeMillis()))
            }
            
            settingsRepo.setTodayPrompt(currentStr, newPrompt, true)
            activePrompt.value = newPrompt
            activePromptIsCustom.value = true
        }
    }

    fun generateNewCustomPrompt() {
        replacePromptWithCustom(PromptDatabase.getRandomPrompt(), true)
    }

    fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR_OF_DAY) < 6) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return dateFormat.format(calendar.time)
    }

    fun saveTodayPhoto(uri: Uri) {
        viewModelScope.launch {
            var finalUri = uri
            try {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Look_${System.currentTimeMillis()}.jpg")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Look")
                    }
                }
                
                val mediaStoreUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (mediaStoreUri != null) {
                    resolver.openOutputStream(mediaStoreUri)?.use { outStream ->
                        resolver.openInputStream(uri)?.use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    finalUri = mediaStoreUri
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dateStr = getCurrentDateString()
            val prompt = activePrompt.value
            val isCustom = activePromptIsCustom.value
            val log = ChallengeLog(
                dateId = dateStr,
                prompt = prompt,
                imagePath = finalUri.toString(),
                timestamp = System.currentTimeMillis(),
                isCustomPrompt = isCustom
            )
            dao.insertLog(log)
        }
    }

    fun createImageUri(): Uri {
        val file = File(context.filesDir, "look_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // ViewModel Factory
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(
                    AppDatabase.getDatabase(context),
                    SettingsRepository(context),
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
