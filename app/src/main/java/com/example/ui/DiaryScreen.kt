package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ChallengeLog
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DiaryScreen(viewModel: MainViewModel) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    var selectedDayLogs by remember { mutableStateOf<List<ChallengeLog>?>(null) }

    val logsByMonth = remember(logs) {
        logs.groupBy { 
            // dateId is like "2023-10-25" -> we want "2023-10"
            if (it.dateId.length >= 7) it.dateId.substring(0, 7) else "Unknown"
        }.toSortedMap(reverseOrder())
    }
    
    val months = logsByMonth.keys.toList()
    var selectedMonth by remember(months) { 
        mutableStateOf(months.firstOrNull())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Requested pure black background
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gallery", style = MaterialTheme.typography.displayMedium, color = Color.White)
        }

        if (months.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(months) { month ->
                    val isSelected = month == selectedMonth
                    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    val label = try {
                        val date = monthFormat.parse(month)
                        date?.let { displayFormat.format(it) } ?: month
                    } catch (e: Exception) { month }

                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else Color.DarkGray,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedMonth = month
                        }.padding(vertical = 8.dp)
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awaiting your first discovery.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )
            }
        } else {
            val currentMonthLogs = selectedMonth?.let { logsByMonth[it] } ?: emptyList()
            val logsByDay = remember(currentMonthLogs) {
                currentMonthLogs.groupBy { it.dateId }.toSortedMap(reverseOrder())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(logsByDay.keys.toList()) { dateId ->
                    val dayLogs = logsByDay[dateId] ?: emptyList()
                    DailyMemoryCard(dateId = dateId, dayLogs = dayLogs) {
                        selectedDayLogs = dayLogs
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    selectedDayLogs?.let { dayLogs ->
        if (dayLogs.isNotEmpty()) {
            DayViewDialog(dayLogs = dayLogs, onDismiss = { selectedDayLogs = null })
        }
    }
}

@Composable
fun DailyMemoryCard(dateId: String, dayLogs: List<ChallengeLog>, onClick: () -> Unit) {
    if (dayLogs.isEmpty()) return
    val initialLog = dayLogs.first()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val displayDate = try {
        val date = dateFormat.parse(dateId)
        date?.let { displayFormat.format(it) } ?: dateId
    } catch (e: Exception) { dateId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            val visibleCount = minOf(3, dayLogs.size)
            for (i in (visibleCount - 1) downTo 0) {
                val log = dayLogs[i]
                val offset = (i * 8).dp
                val rotation = if (i == 0) 0f else if (i == 1) 4f else -2f
                val alphaVal = 1f - (i * 0.1f)
                val scaleVal = 1f - (i * 0.05f)

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp, top = offset) // give room for stack offsets
                        .graphicsLayer {
                            rotationZ = rotation
                            alpha = alphaVal
                            scaleX = scaleVal
                            scaleY = scaleVal
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (i==0) 8.dp else 4.dp)
                ) {
                    AsyncImage(
                        model = log.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            if (dayLogs.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${dayLogs.size} PHOTOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = displayDate,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "“${initialLog.prompt.replace("*", "")}”",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DayViewDialog(dayLogs: List<ChallengeLog>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha=0.5f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PhotoDeckGallery(logs = dayLogs, modifier = Modifier.weight(1f).fillMaxWidth())
                
                val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                val displayDate = try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayLogs.first().dateId)
                    parsed?.let { displayFormat.format(it) } ?: dayLogs.first().dateId
                } catch (e: Exception) { dayLogs.first().dateId }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "“${dayLogs.first().prompt.replace("*", "")}”",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PhotoDeckGallery(
    logs: List<ChallengeLog>,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val visibleCount = minOf(4, logs.size)
        // Reverse order so index 0 is drawn last (on top)
        for (i in (visibleCount - 1) downTo 0) {
            val realIndex = (currentIndex + i) % logs.size
            val log = logs[realIndex]
            
            val animatedScale by animateFloatAsState(
                targetValue = 1f - (i * 0.08f),
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 100f),
                label = "scale"
            )
            val animatedOffset by animateDpAsState(
                targetValue = (i * 24).dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 100f),
                label = "offset"
            )
            val animatedRotation by animateFloatAsState(
                targetValue = if (i == 0) 0f else if (i % 2 == 1) 3f else -3f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 100f),
                label = "rotation"
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f - (i * 0.15f),
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 200f),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp, top = 32.dp, start = 24.dp, end = 24.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationY = animatedOffset.toPx()
                        rotationZ = animatedRotation
                        alpha = animatedAlpha
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (logs.size > 1) {
                            currentIndex = (currentIndex + 1) % logs.size
                        }
                    }
            ) {
                AsyncImage(
                    model = log.imagePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
