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

    val activePrompt = MutableStateFlow("")
    val activePromptIsCustom = MutableStateFlow(false)

    val todayLogs: StateFlow<List<ChallengeLog>> = combine(
        _currentDateId,
        activePrompt,
        dao.getAllLogs()
    ) { date, promptText, logs ->
        logs.filter { it.dateId == date && it.prompt == promptText }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val isDeletingData = MutableStateFlow(false)
    val deleteAnimationTriggered = MutableStateFlow(false)

    val promptHistory = dao.getPromptHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            checkAndSetTodayPrompt()
        }
        
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                var nextRefreshTime = settingsRepo.nextRefreshTime.first()
                val currentMode = settingsRepo.promptMode.first()
                if (nextRefreshTime <= 0L) {
                    setupNextRefreshTime(currentMode)
                    nextRefreshTime = settingsRepo.nextRefreshTime.first()
                }
                
                if (System.currentTimeMillis() >= nextRefreshTime) {
                    // Time to refresh
                    val used = promptHistory.value.map { it.prompt } + allLogs.value.map { it.prompt } + activePrompt.value
                    val newPrompt = PromptDatabase.getUnusedPrompt(used.distinct())
                    
                    settingsRepo.setTodayPrompt(getCurrentDateString(), newPrompt, false)
                    activePrompt.value = newPrompt
                    activePromptIsCustom.value = false
                    dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
                    
                    setupNextRefreshTime(currentMode)
                    
                    val shouldNotify = settingsRepo.notificationsEnabled.first()
                    if (shouldNotify) {
                        try {
                            val clazz = Class.forName("com.example.ui.SettingsScreenKt")
                            val method = clazz.getMethod("showTestNotification", Context::class.java, String::class.java)
                            method.invoke(null, context, newPrompt)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    suspend fun setupNextRefreshTime(mode: String) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        when (mode) {
            "Morning" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
            }
            "Noon" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
            }
            "Evening" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 17)
                calendar.set(Calendar.MINUTE, 0)
            }
            "Random" -> {
                calendar.set(Calendar.HOUR_OF_DAY, (9..20).random())
                calendar.set(Calendar.MINUTE, (0..59).random())
            }
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            // for random, recalculate time for next day
            if (mode == "Random") {
                calendar.set(Calendar.HOUR_OF_DAY, (9..20).random())
                calendar.set(Calendar.MINUTE, (0..59).random())
            }
        }
        
        settingsRepo.setNextRefreshTime(calendar.timeInMillis)
    }

    private suspend fun checkAndSetTodayPrompt() {
        val storedPrompt = settingsRepo.todayPrompt.first()
        val storedIsCustom = settingsRepo.todayPromptIsCustom.first()

        if (storedPrompt.isNullOrEmpty()) {
            val used = promptHistory.value.map { it.prompt } + allLogs.value.map { it.prompt }
            val newPrompt = PromptDatabase.getUnusedPrompt(used.distinct())
            settingsRepo.setTodayPrompt(getCurrentDateString(), newPrompt, false)
            activePrompt.value = newPrompt
            activePromptIsCustom.value = false
            
            // Save official prompt to history
            dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
            setupNextRefreshTime(settingsRepo.promptMode.first())
        } else {
            activePrompt.value = storedPrompt
            activePromptIsCustom.value = storedIsCustom
        }
    }

    fun replacePromptWithCustom(newPrompt: String, savePreviousToHistory: Boolean = true) {
        viewModelScope.launch {
            val currentStr = getCurrentDateString()
            val currentPrompt = activePrompt.value
            
            // Check if current prompt has photos
            val hasPhotos = dao.getAllLogs().first().any { it.prompt == currentPrompt }
            // Always keep the previous prompt in history as per new requirements
            
            // Immediately add the newly activated prompt to history
            val isAlreadyInHistory = dao.getPromptHistory().first().any { it.prompt == newPrompt }
            if (!isAlreadyInHistory) {
                dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
            }
            
            settingsRepo.setTodayPrompt(currentStr, newPrompt, true)
            activePrompt.value = newPrompt
            activePromptIsCustom.value = true
        }
    }

    fun generateNewCustomPrompt() {
        viewModelScope.launch {
            val used = promptHistory.value.map { it.prompt } + allLogs.value.map { it.prompt } + activePrompt.value
            val next = PromptDatabase.getUnusedPrompt(used.distinct())
            triggerPromptReplacementAnimation(next)
        }
    }

    val isChangingPrompt = MutableStateFlow(false)
    val changePromptStage = MutableStateFlow(0)
    val deleteAnimationStage = MutableStateFlow(0)
    
    val showNotificationReveal = MutableStateFlow(false)
    
    fun triggerPromptReveal() {
        viewModelScope.launch {
            showNotificationReveal.value = true
            kotlinx.coroutines.delay(2000)
            showNotificationReveal.value = false
        }
    }

    fun triggerDeleteAnimation(deleteImages: Boolean, deletePromptHistory: Boolean) {
        viewModelScope.launch {
            deleteAnimationTriggered.value = true
            isDeletingData.value = true
            com.example.util.Haptics.descendingPattern()
            
            deleteAnimationStage.value = 1
            kotlinx.coroutines.delay(1500) // Dissolve animation duration
            
            deleteAnimationStage.value = 2
            kotlinx.coroutines.delay(1500)
            
            // Delete selected data
            deleteAllData(deleteImages, deletePromptHistory)
            
            kotlinx.coroutines.delay(500)
            isDeletingData.value = false
            deleteAnimationTriggered.value = false
            deleteAnimationStage.value = 0
            com.example.util.Haptics.softPulse()
        }
    }

    fun triggerPromptReplacementAnimation(newPrompt: String? = null) {
        viewModelScope.launch {
            isChangingPrompt.value = true
            changePromptStage.value = 1
            kotlinx.coroutines.delay(1500) // Overlay duration
            
            val promptToSet = newPrompt ?: PromptDatabase.getRandomPrompt()
            replacePromptWithCustom(promptToSet, true)
            
            changePromptStage.value = 2
            com.example.util.Haptics.doublePulse()
            kotlinx.coroutines.delay(1500) // Let it settle
            
            isChangingPrompt.value = false
            changePromptStage.value = 0
        }
    }

    private fun deleteAllData(deleteImages: Boolean, deletePromptHistory: Boolean) {
        viewModelScope.launch {
            if (deletePromptHistory) {
                // Clear settings repository
                settingsRepo.clearAll()
                // Delete all DB entries for prompt history
                dao.deleteAllPromptHistory()
            }
            
            if (deleteImages) {
                // Delete all DB entries for logs
                dao.deleteAllLogs()
                // Delete local image files
                context.filesDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("morrow_") && file.name.endsWith(".jpg")) {
                        file.delete()
                    }
                }
            }
            
            if (deletePromptHistory) {
                // Generate a fresh prompt for today
                val currentStr = getCurrentDateString()
                val newPrompt = PromptDatabase.getRandomPrompt()
                settingsRepo.setTodayPrompt(currentStr, newPrompt, false)
                activePrompt.value = newPrompt
                activePromptIsCustom.value = false
                dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
            }
        }
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
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Morrow_${System.currentTimeMillis()}.jpg")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Morrow")
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
            
            val todayHasPlayed = allLogs.value.any { it.dateId == dateStr && !it.isCustomPrompt }
            if (!isCustom && !todayHasPlayed) {
                val currentStreak = allLogs.value.filter { !it.isCustomPrompt }.map { it.dateId }.distinct().size
                val newStreak = currentStreak + 1
                if (newStreak == 7 || newStreak == 30 || newStreak == 100 || newStreak == 365) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(2000)
                        com.example.util.Haptics.doublePulse()
                        kotlinx.coroutines.delay(100)
                        com.example.util.Haptics.strongPulse()
                    }
                }
            }
            
            dao.insertLog(log)
        }
    }

    fun createImageUri(): Uri {
        val file = File(context.filesDir, "morrow_${System.currentTimeMillis()}.jpg")
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
