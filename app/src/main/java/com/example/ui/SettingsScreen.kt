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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settingsRepo = viewModel.settingsRepo
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val notificationsEnabled by settingsRepo.notificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    
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
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Notifications for Morrow challenges"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MORROW_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTestNotification(context: Context) {
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, "MORROW_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Morrow Test")
            .setContentText("This is a test notification for Morrow!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }
    
    val totalPhotos = allLogs.size
    val totalDays = allLogs.map { it.dateId }.distinct().size
    val firstMemoryTimestamp = allLogs.minByOrNull { it.timestamp }?.timestamp
    val firstMemoryStr = firstMemoryTimestamp?.let {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it))
    } ?: "No memories yet"
    
        var promptTime by remember { mutableStateOf("Morning") }
        var showPromptTimeSheet by remember { mutableStateOf(false) }

        val todayDate = viewModel.getCurrentDateString()
        val hasPlayedToday = allLogs.any { it.dateId == todayDate }
        val currentStreak = if (hasPlayedToday) totalDays else maxOf(0, totalDays - 1)

        var showPromptLibraryWarning by remember { mutableStateOf(false) }
        var showPromptLibrary by remember { mutableStateOf(false) }

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
                    
                    val options = listOf("Morning", "Noon", "Evening", "Surprise Me")
                    options.forEach { option ->
                        val isSelected = option == promptTime
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { 
                                    promptTime = option
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
            PromptLibraryScreen(onBack = { showPromptLibrary = false })
            return
        }
        
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Text("Journey", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        
        // Daily Prompt Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Daily Prompt", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                Text("Daily Reminders", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Receive a notification for the daily challenge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { isChecked ->
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPromptTimeSheet = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notification Time", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            Text(promptTime, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Next prompt in", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            NextPromptTimerText()
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
                                .clickable(enabled = !isCountingDown) {
                                    isCountingDown = true
                                    countdownValue = 5
                                    coroutineScope.launch {
                                        while (countdownValue > 0) {
                                            delay(1000)
                                            countdownValue--
                                        }
                                        showTestNotification(context)
                                        delay(1000)
                                        isCountingDown = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isCountingDown,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                },
                                label = "button_content"
                            ) { countingDown ->
                                if (countingDown) {
                                    Text("Notification in $countdownValue...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("Send Test Notification", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Prompt Library Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Prompt Engine", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.clickable { showPromptLibraryWarning = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text("Prompt Library", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Explore examples of generated challenges", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "View Library", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        // Archive Stats Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your Journey", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text(totalPhotos.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Images taken", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(totalDays.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Days completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(currentStreak.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Current streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(firstMemoryStr, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("First memory", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        
        // Your Data
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().clickable{}.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delete All Data", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        // About
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("About Morrow", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "A daily creative practice for noticing the extraordinary hidden within ordinary life. No feeds, no followers, no algorithms. Just you, your surroundings, and the moments worth remembering.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }
    }
}

@Composable
fun NextPromptTimerText() {
    var timeUntilNext by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while(true) {
            val calendar = java.util.Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val diff = calendar.timeInMillis - now
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60
            val seconds = (diff / 1000) % 60
            timeUntilNext = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            delay(1000)
        }
    }
    Text(timeUntilNext, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun PromptLibraryScreen(onBack: () -> Unit) {
    val samplePrompts = remember { 
        listOf(
            "A morning shadow stretching across the floor",
            "Something that has been broken and repaired",
            "The way light reflects off a glass of water",
            "A forgotten object on a window sill",
            "The texture of your favorite piece of clothing",
            "A quiet corner of your neighborhood",
            "Something that represents the passing of time",
            "A familiar view from an unfamiliar angle",
            "The remnants of a meal",
            "An object you interact with every day but rarely look at",
            "A juxtaposition of nature and architecture",
            "The color yellow in an unexpected place"
        ).shuffled()
    }
    
    var promptsList by remember { mutableStateOf(samplePrompts.take(6)) }
    
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
            TextButton(onClick = { promptsList = samplePrompts.shuffled().take(6) }) {
                Text("Refresh", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(promptsList.size) { index ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "“${promptsList[index]}”",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}
