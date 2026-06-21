package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.PromptDatabase
import com.example.data.SettingsRepository
import com.example.ui.showTestNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class PromptAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        if (action == "com.example.TEST_NOTIFICATION") {
            val activePrompt = intent.getStringExtra("active_prompt") ?: "Get your camera ready for today's challenge."
            showTestNotification(context, activePrompt)
            return
        }
        
        // Handle boot completed or prompt refresh
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepo = SettingsRepository(context)
                val currentMode = settingsRepo.promptMode.first()
                
                if (action == Intent.ACTION_BOOT_COMPLETED) {
                    // Reschedule on boot
                    val nextRefreshTimeStr = settingsRepo.nextRefreshTime.first()
                    if (nextRefreshTimeStr > 0) {
                        schedulePromptRefresh(context, nextRefreshTimeStr)
                    }
                    
                    val remindersEnabled = settingsRepo.remindersEnabled.first()
                    if (remindersEnabled) {
                        scheduleNextReminder(context)
                    }
                    return@launch
                }
                
                if (action == "com.example.REMINDER_FIRED") {
                    val remindersEnabled = settingsRepo.remindersEnabled.first()
                    if (remindersEnabled) {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.challengeDao()
                        val calendar = Calendar.getInstance()
                        if (calendar.get(Calendar.HOUR_OF_DAY) < 6) {
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                        }
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val todayStr = dateFormat.format(calendar.time)
                        
                        val allLogs = dao.getAllLogsSync()
                        val hasTakenPhotoToday = allLogs.any { it.dateId == todayStr }
                        if (!hasTakenPhotoToday) {
                            val activePrompt = settingsRepo.todayPrompt.first()
                            val nextRefreshTimeStr = settingsRepo.nextRefreshTime.first()
                            if (activePrompt != null && nextRefreshTimeStr > System.currentTimeMillis()) {
                                com.example.ui.showReminderNotification(context, activePrompt, nextRefreshTimeStr)
                                // Schedule next reminder in 4 hours
                                scheduleNextReminder(context)
                            }
                        }
                    }
                    return@launch
                }
                
                if (action == "com.example.PROMPT_REFRESH") {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.challengeDao()
                    
                    val activePrompt = settingsRepo.todayPrompt.first()
                    val promptHistory = dao.getPromptHistorySync()
                    val allLogs = dao.getAllLogsSync()
                    
                    val used = promptHistory.map { it.prompt } + allLogs.map { it.prompt } + (activePrompt ?: "")
                    val newPrompt = PromptDatabase.getUnusedPrompt(used.distinct())
                    
                    // Same date format and offset as MainViewModel
                    val calendar = Calendar.getInstance()
                    if (calendar.get(Calendar.HOUR_OF_DAY) < 6) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val todayStr = dateFormat.format(calendar.time)
                    
                    settingsRepo.setTodayPrompt(todayStr, newPrompt, false)
                    dao.insertPromptHistory(com.example.data.PromptHistory(prompt = newPrompt, dateReceived = System.currentTimeMillis()))
                    
                    val shouldNotify = settingsRepo.notificationsEnabled.first()
                    if (shouldNotify) {
                        showTestNotification(context, newPrompt)
                    }
                    
                    // Setup next refresh time
                    val nextTime = calculateNextRefreshTime(currentMode)
                    settingsRepo.setNextRefreshTime(nextTime)
                    schedulePromptRefresh(context, nextTime)
                    
                    val remindersEnabled = settingsRepo.remindersEnabled.first()
                    if (remindersEnabled) {
                        scheduleNextReminder(context)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun scheduleTestNotification(context: Context, delayMillis: Long, activePrompt: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PromptAlarmReceiver::class.java).apply {
                action = "com.example.TEST_NOTIFICATION"
                putExtra("active_prompt", activePrompt)
            }
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flag)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delayMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delayMillis,
                    pendingIntent
                )
            }
        }

        fun schedulePromptRefresh(context: Context, triggerAtMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PromptAlarmReceiver::class.java).apply {
                action = "com.example.PROMPT_REFRESH"
            }
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1002, intent, flag)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        fun calculateNextRefreshTime(mode: String): Long {
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
                else -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 10)
                    calendar.set(Calendar.MINUTE, 0)
                }
            }
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                if (mode == "Random") {
                    calendar.set(Calendar.HOUR_OF_DAY, (9..20).random())
                    calendar.set(Calendar.MINUTE, (0..59).random())
                }
            }
            return calendar.timeInMillis
        }
        
        fun cancelTestNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PromptAlarmReceiver::class.java).apply {
                action = "com.example.TEST_NOTIFICATION"
            }
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flag)
            alarmManager.cancel(pendingIntent)
        }

        fun scheduleNextReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PromptAlarmReceiver::class.java).apply {
                action = "com.example.REMINDER_FIRED"
            }
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1003, intent, flag)
            
            // Generate a random-ish noise for reminder or just 4 hours.
            val triggerAtMillis = System.currentTimeMillis() + 4 * 60 * 60 * 1000L
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
        
        fun cancelNextReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PromptAlarmReceiver::class.java).apply {
                action = "com.example.REMINDER_FIRED"
            }
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1003, intent, flag)
            alarmManager.cancel(pendingIntent)
        }
    }
}
