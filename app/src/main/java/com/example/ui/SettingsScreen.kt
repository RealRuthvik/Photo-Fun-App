package com.example.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Daily Prompts"
        val descriptionText = "Notifications for Morrow daily photography challenges"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("MORROW_CHANNEL", name, importance).apply {
            description = descriptionText
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 150)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showTestNotification(context: Context, activePrompt: String) {
    createNotificationChannel(context)
        
    val style = NotificationCompat.BigTextStyle()
        .bigText(activePrompt)
            
    val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("launched_from_notification", true)
    }
    val pendingIntent = android.app.PendingIntent.getActivity(
        context, 0, intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
            
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.R.mipmap.ic_launcher)
    val largeIconBitmap = if (drawable != null) {
        val bmp = android.graphics.Bitmap.createBitmap(
            Math.max(1, drawable.intrinsicWidth),
            Math.max(1, drawable.intrinsicHeight),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
    } else {
        android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.mipmap.ic_launcher)
    }
    
    val builder = NotificationCompat.Builder(context, "MORROW_CHANNEL")
        .setSmallIcon(com.example.R.drawable.ic_launcher_foreground)
        .setLargeIcon(largeIconBitmap)
        .setContentTitle("A new photography challenge is here!")
        .setContentText(activePrompt)
        .setStyle(style)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(1001, builder.build())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settingsRepo = viewModel.settingsRepo
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val notificationsEnabled by settingsRepo.notificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val activePrompt by viewModel.activePrompt.collectAsStateWithLifecycle()
    val promptHistory by viewModel.promptHistory.collectAsStateWithLifecycle()
    
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(5) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                settingsRepo.setNotificationsEnabled(true)
            }
        }
    }
    
    val totalPhotos = allLogs.size
    val officialLogs = allLogs.filter { !it.isCustomPrompt }
    val totalDays = officialLogs.map { it.dateId }.distinct().size
    val firstMemoryTimestamp = allLogs.minByOrNull { it.timestamp }?.timestamp
    val firstMemoryStr = firstMemoryTimestamp?.let {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it))
    } ?: "No memories yet"
    
        val promptTime by viewModel.settingsRepo.promptMode.collectAsStateWithLifecycle(initialValue = "Morning")
        var showPromptTimeSheet by remember { mutableStateOf(false) }

        val todayDate = viewModel.getCurrentDateString()
        val hasPlayedToday = officialLogs.any { it.dateId == todayDate }
        val currentStreak = viewModel.calculateCurrentStreak(officialLogs)

        var showPromptLibraryWarning by remember { mutableStateOf(false) }
        var showPromptLibrary by remember { mutableStateOf(false) }
        var showGetPromptNowDialog by remember { mutableStateOf(false) }

        var showDeleteWarning1 by remember { mutableStateOf(false) }

        if (showGetPromptNowDialog) {
            AlertDialog(
                onDismissRequest = { showGetPromptNowDialog = false },
                title = { Text("Get Prompt Now") },
                text = { Text("This will replace your current prompt. Custom prompts do not contribute toward daily streaks.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.generateNewCustomPrompt()
                        showGetPromptNowDialog = false
                    }) {
                        Text("Continue", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGetPromptNowDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        var deleteImages by remember { mutableStateOf(false) }
        var deletePromptHistory by remember { mutableStateOf(false) }
        var showDeleteWarning2 by remember { mutableStateOf(false) }

        LaunchedEffect(showDeleteWarning1) {
            if (showDeleteWarning1) {
                com.example.util.Haptics.softPulse()
            }
        }
        LaunchedEffect(showDeleteWarning2) {
            if (showDeleteWarning2) {
                com.example.util.Haptics.strongPulse()
            }
        }

        if (showDeleteWarning1) {
            AlertDialog(
                onDismissRequest = { showDeleteWarning1 = false },
                title = { Text("Delete Data") },
                text = { 
                    Column {
                        Text("Select the data you wish to permanently remove. This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { deleteImages = !deleteImages }.padding(vertical = 8.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = deleteImages,
                                onCheckedChange = { deleteImages = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Images & Memories", color = MaterialTheme.colorScheme.onBackground)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { deletePromptHistory = !deletePromptHistory }.padding(vertical = 8.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = deletePromptHistory,
                                onCheckedChange = { deletePromptHistory = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prompt History", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                },
                confirmButton = {
                    val isEnabled = deleteImages || deletePromptHistory
                    TextButton(
                        onClick = { 
                            showDeleteWarning1 = false
                            showDeleteWarning2 = true
                        },
                        enabled = isEnabled
                    ) {
                        Text("Continue", color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteWarning1 = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        if (showDeleteWarning2) {
            AlertDialog(
                onDismissRequest = { showDeleteWarning2 = false },
                title = { Text("Are you absolutely sure?") },
                text = { Text("This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(onClick = { 
                        showDeleteWarning2 = false
                        viewModel.triggerDeleteAnimation(deleteImages, deletePromptHistory)
                    }) {
                        Text("Delete Selected Data", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteWarning2 = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        if (showPromptTimeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPromptTimeSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Notification Time", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text("Your daily challenge is ready at 6:00 AM. When would you like to be reminded?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val options = listOf("Morning", "Noon", "Evening", "Random")
                    options.forEach { option ->
                        val isSelected = option == promptTime
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { 
                                    coroutineScope.launch {
                                        viewModel.settingsRepo.setPromptMode(option)
                                        viewModel.setupNextRefreshTime(option)
                                    }
                                    showPromptTimeSheet = false 
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyLarge, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
        }
        
        if (showPromptLibraryWarning) {
            AlertDialog(
                onDismissRequest = { showPromptLibraryWarning = false },
                title = { Text("Spoiler Warning") },
                text = { Text("Viewing the Prompt Library will reveal potential future photography challenges, which may reduce the daily surprise. Are you sure you want to continue?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(onClick = { 
                        showPromptLibraryWarning = false
                        showPromptLibrary = true 
                    }) {
                        Text("View Library", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPromptLibraryWarning = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        if (showPromptLibrary) {
            PromptLibraryScreen(viewModel = viewModel, onBack = { showPromptLibrary = false })
            return
        }
        
    val useAccentColors by settingsRepo.useAccentColors.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        item {
            Text("Journey", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        
        // Daily Prompt Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Daily Prompts", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text("Daily Reminders", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Receive a notification for the daily challenge.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                com.example.util.Haptics.softPulse()
                                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }
                                coroutineScope.launch {
                                    settingsRepo.setNotificationsEnabled(isChecked)
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPromptTimeSheet = true }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text("Prompt Delivery Schedule", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Controls when a new prompt is generated and when notifications arrive.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(promptTime, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Next prompt in", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Get Prompt Now",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showGetPromptNowDialog = true }.padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                            )
                        }
                        NextPromptTimerText(promptTime)
                    }
                    
                    var countdownJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(enabled = !isCountingDown) {
                                if (!isCountingDown) {
                                    isCountingDown = true
                                    countdownValue = 5
                                    com.example.receiver.PromptAlarmReceiver.scheduleTestNotification(context, 5000, activePrompt)
                                    countdownJob = coroutineScope.launch {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        while (countdownValue > 0) {
                                            delay(1000)
                                            countdownValue--
                                            if (countdownValue > 0) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                        delay(1000)
                                        isCountingDown = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AnimatedContent(
                            targetState = isCountingDown,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            },
                            label = "button_content"
                        ) { countingDown ->
                            if (countingDown) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Notification in $countdownValue...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "Cancel", 
                                        style = MaterialTheme.typography.labelLarge, 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            countdownJob?.cancel()
                                            com.example.receiver.PromptAlarmReceiver.cancelTestNotification(context)
                                            isCountingDown = false
                                            countdownValue = 0
                                        }.padding(8.dp)
                                    )
                                }
                            } else {
                                Text("Send Notification", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        
        // Explore Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Explore", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showPromptLibraryWarning = true }.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("Prompt Library", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Explore examples of generated challenges", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "View Library", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        // Your Journey Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Your Journey", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Column {
                    JourneyStatCard(
                        value = totalPhotos.toString(),
                        label = "Images Taken",
                        useAccentColors = useAccentColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        JourneyStatCard(
                            value = totalDays.toString(),
                            label = "Days Completed",
                            useAccentColors = useAccentColors,
                            modifier = Modifier.weight(1f)
                        )
                        JourneyStatCard(
                            value = "$currentStreak",
                            label = "Current Streak",
                            useAccentColors = useAccentColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    JourneyStatCard(
                        value = firstMemoryStr,
                        label = "First Memory",
                        useAccentColors = useAccentColors,
                        isSmallValue = true
                    )
                }
            }
        }
        
        // Appearance Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("Use Accent Colors", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Apply subtle device accent colors across the UI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = useAccentColors,
                        onCheckedChange = { checked ->
                            com.example.util.Haptics.softPulse()
                            scope.launch { settingsRepo.setUseAccentColors(checked) }
                        },
                    )
                }
            }
        }
        
        // Data Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showDeleteWarning1 = true }.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delete All Data", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        // About Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("About Morrow", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Text(
                    "Most people take photos when something important happens. This app helps people discover that important things are happening every day, they just need a reason to notice them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Morrow Logo",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Thank you for using Morrow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NextPromptTimerText(mode: String) {
    if (mode == "Random") {
        var randomText by remember { mutableStateOf("??:??:??") }
        LaunchedEffect(Unit) {
            val chars = listOf('?', '#')
            while (true) {
                val newStr = StringBuilder("??:??:??")
                val replaceIdx = (0..7).random()
                if (replaceIdx != 2 && replaceIdx != 5) {
                    newStr[replaceIdx] = chars.random()
                }
                val replaceIdx2 = (0..7).random()
                if (replaceIdx2 != 2 && replaceIdx2 != 5) {
                    newStr[replaceIdx2] = chars.random()
                }
                randomText = newStr.toString()
                delay((100..400).random().toLong())
            }
        }
        Text(randomText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    } else {
        var timeUntilNext by remember { mutableStateOf("") }
        val context = LocalContext.current
        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.ui.MainViewModel>(
            factory = com.example.ui.MainViewModel.Factory(context.applicationContext)
        )
        val nextRefreshTime by viewModel.settingsRepo.nextRefreshTime.collectAsStateWithLifecycle(initialValue = 0L)
        
        LaunchedEffect(nextRefreshTime) {
            while(true) {
                val now = System.currentTimeMillis()
                val diff = nextRefreshTime - now
                if (diff <= 0) {
                    timeUntilNext = "00:00:00"
                } else {
                    val hours = diff / (1000 * 60 * 60)
                    val minutes = (diff / (1000 * 60)) % 60
                    val seconds = (diff / 1000) % 60
                    timeUntilNext = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000)
            }
        }
        Text(timeUntilNext, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun PromptLibraryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val promptHistory by viewModel.promptHistory.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val activePrompt by viewModel.activePrompt.collectAsStateWithLifecycle()
    
    var promptsList by remember { mutableStateOf<List<String>>(emptyList()) }
    
    LaunchedEffect(promptHistory, allLogs, activePrompt) {
        if (promptsList.isEmpty()) {
            val used = promptHistory.map { it.prompt }.toSet() + allLogs.map { it.prompt } + activePrompt
            val generated = mutableListOf<String>()
            repeat(6) {
                generated.add(com.example.data.PromptDatabase.getUnusedPrompt((used + generated).toList()))
            }
            promptsList = generated
        }
    }
    
    var selectedPrompt by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    if (selectedPrompt != null) {
        AlertDialog(
            onDismissRequest = { selectedPrompt = null },
            title = { Text("Use This Prompt") },
            text = { Text("This prompt will replace your current daily challenge. Library prompts do not contribute toward daily streaks.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { 
                    com.example.util.Haptics.strongPulse()
                    viewModel.triggerPromptReplacementAnimation(selectedPrompt)
                    selectedPrompt = null
                    onBack()
                }) {
                    Text("Use Prompt", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPrompt = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Prompt Library", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            if (selectedTabIndex == 0) {
                TextButton(onClick = { 
                    val used = promptHistory.map { it.prompt }.toSet() + allLogs.map { it.prompt } + activePrompt
                    val generated = mutableListOf<String>()
                    repeat(6) {
                        generated.add(com.example.data.PromptDatabase.getUnusedPrompt((used + generated).toList()))
                    }
                    promptsList = generated
                }) {
                    Text("Refresh", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text("Generated Examples", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text("Used Prompts", modifier = Modifier.padding(16.dp))
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTabIndex == 0) {
                items(promptsList.size) { index ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                com.example.util.Haptics.softPulse()
                                selectedPrompt = promptsList[index] 
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "“${promptsList[index].replace("*", "")}”",
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
                if (promptsList.isEmpty()) {
                    item {
                        Text("No unseen examples available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 32.dp))
                    }
                }
            } else {
                val sortedHistory = promptHistory.sortedByDescending { it.dateReceived }
                items(sortedHistory.size) { index ->
                    val historyItem = sortedHistory[index]
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(historyItem.dateReceived))
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            text = "“${historyItem.prompt.replace("*", "")}”",
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Used on $dateFormatted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
                if (promptHistory.isEmpty()) {
                    item {
                        Text("You haven't used any prompts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun JourneyStatCard(
    value: String,
    label: String,
    useAccentColors: Boolean,
    modifier: Modifier = Modifier,
    isSmallValue: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = value,
            style = if (isSmallValue) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

