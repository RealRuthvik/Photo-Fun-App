package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        var showMonthSelector by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val label = selectedMonth?.let { month ->
                try {
                    val date = monthFormat.parse(month)
                    date?.let { displayFormat.format(it) } ?: month
                } catch (e: Exception) { month }
            } ?: "Archive"

            Box {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { if (months.size > 1) showMonthSelector = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                    if (months.size > 1) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                
                DropdownMenu(
                    expanded = showMonthSelector,
                    onDismissRequest = { showMonthSelector = false }
                ) {
                    months.forEach { month ->
                        val mLabel = try {
                            val date = monthFormat.parse(month)
                            date?.let { displayFormat.format(it) } ?: month
                        } catch (e: Exception) { month }
                        DropdownMenuItem(
                            text = { Text(mLabel, style = MaterialTheme.typography.bodyLarge) },
                            onClick = { 
                                selectedMonth = month
                                showMonthSelector = false 
                            }
                        )
                    }
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

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 32.dp
            ) {
                staggeredItems(logsByDay.keys.toList()) { dateId ->
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
    val initialLog = remember(dayLogs) { dayLogs.random() }

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
                .wrapContentHeight()
        ) {
            AsyncImage(
                model = initialLog.imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
            if (dayLogs.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${dayLogs.size} Photos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = displayDate,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayViewDialog(dayLogs: List<ChallengeLog>, onDismiss: () -> Unit) {
    var selectedLog by remember { mutableStateOf<ChallengeLog?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                val displayDate = try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayLogs.first().dateId)
                    parsed?.let { displayFormat.format(it) } ?: dayLogs.first().dateId
                } catch (e: Exception) { dayLogs.first().dateId }
                
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "“${dayLogs.first().prompt.replace("*", "")}”",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    items(dayLogs) { log ->
                        AsyncImage(
                            model = log.imagePath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedLog = log }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    selectedLog?.let { log ->
        FullscreenImageDialog(log = log, onDismiss = { selectedLog = null })
    }
}
