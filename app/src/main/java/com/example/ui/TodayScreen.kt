package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.filled.Close
fun Modifier.dashedBorder(color: Color, width: Dp, radius: Dp) = this.drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
        ),
        cornerRadius = CornerRadius(radius.toPx(), radius.toPx())
    )
}

@Composable
fun TodayScreen(viewModel: MainViewModel, onSwipeStateChange: (Boolean) -> Unit = {}) {
    val activePrompt by viewModel.activePrompt.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val useAccentColors by viewModel.settingsRepo.useAccentColors.collectAsStateWithLifecycle(initialValue = true)
    
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                capturedUri?.let { uri ->
                    viewModel.saveTodayPhoto(uri)
                }
            }
        }
    )

    val dateFormatDay = SimpleDateFormat("EEEE", Locale.getDefault())
    val dateFormatDate = SimpleDateFormat("MMMM d", Locale.getDefault())
    val currentDate = Date()
    val dayString = dateFormatDay.format(currentDate)
    val dateString = dateFormatDate.format(currentDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayString.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            
            androidx.compose.animation.AnimatedContent(
                targetState = todayLogs.isNotEmpty(),
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(800, delayMillis = 150)) togetherWith
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) using
                    androidx.compose.animation.SizeTransform { initialSize, targetSize ->
                        androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 100f)
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "HomeContentTransition"
            ) { hasLogs ->
                if (hasLogs) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Photo is Hero
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SwipingPhotoDeck(logs = todayLogs, onSwipeStateChange = onSwipeStateChange, modifier = Modifier.weight(1f).fillMaxWidth())
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val revealTrigger = viewModel.showNotificationReveal.collectAsState().value
                        LaunchedEffect(activePrompt, revealTrigger) {
                            com.example.util.Haptics.softPulse()
                        }
                        androidx.compose.runtime.key(revealTrigger) {
                            com.example.ui.components.AnimatedPromptText(
                                text = "“$activePrompt”",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                isDynamicColor = false,
                                useAccentColors = useAccentColors
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Prompt is Hero
                        Spacer(modifier = Modifier.weight(1f))
                        val revealTrigger = viewModel.showNotificationReveal.collectAsState().value
                        LaunchedEffect(activePrompt, revealTrigger) {
                            com.example.util.Haptics.softPulse()
                        }
                        androidx.compose.runtime.key(revealTrigger) {
                            com.example.ui.components.AnimatedPromptText(
                                text = "“$activePrompt”",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 48.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                isDynamicColor = false,
                                useAccentColors = useAccentColors
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            Button(
                onClick = {
                    com.example.util.Haptics.mediumPulse()
                    val uri = viewModel.createImageUri()
                    capturedUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (todayLogs.isEmpty()) "Capture Moment" else "Capture Another",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
