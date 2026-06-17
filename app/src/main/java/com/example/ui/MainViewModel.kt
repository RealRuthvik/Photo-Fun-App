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

    init {
        viewModelScope.launch {
            checkAndSetTodayPrompt()
            generateMockData()
        }
    }

    private suspend fun generateMockData() {
        val logs = dao.getAllLogs().first()
        if (logs.isEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -5)
            val endDate = Calendar.getInstance()
            
            val mockImages = listOf(
                "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1498307833015-e7b400441eb8?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1555169062-013468b47731?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1481349518771-20055b2a7b24?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1506744626753-1fa28f67c9fe?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1523712999610-f77fbcfc3843?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1433086966358-54859d0ed716?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1444464666168-49b626d49c6b?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1501854140801-50d01698950b?q=80&w=600&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1470071131384-001b85755536?q=80&w=600&auto=format&fit=crop"
            )

            while (calendar.timeInMillis < endDate.timeInMillis) {
                // Generate logs for ~70% of days
                if ((1..10).random() <= 7) {
                    val numPhotos = (1..3).random()
                    val prompt = PromptDatabase.getRandomPrompt()
                    val dateStr = dateFormat.format(calendar.time)
                    
                    for (i in 0 until numPhotos) {
                        val log = ChallengeLog(
                            dateId = dateStr,
                            prompt = prompt,
                            imagePath = mockImages.random(),
                            timestamp = calendar.timeInMillis + (i * 1000)
                        )
                        dao.insertLog(log)
                    }
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private suspend fun checkAndSetTodayPrompt() {
        val currentStr = getCurrentDateString()
        val storedDate = settingsRepo.todayDate.first()
        val storedPrompt = settingsRepo.todayPrompt.first()

        if (storedDate != currentStr || storedPrompt.isNullOrEmpty()) {
            val newPrompt = PromptDatabase.getRandomPrompt()
            settingsRepo.setTodayPrompt(currentStr, newPrompt)
            activePrompt.value = newPrompt
        } else {
            activePrompt.value = storedPrompt
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
            val dateStr = getCurrentDateString()
            val prompt = activePrompt.value
            val log = ChallengeLog(
                dateId = dateStr,
                prompt = prompt,
                imagePath = uri.toString(),
                timestamp = System.currentTimeMillis()
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
