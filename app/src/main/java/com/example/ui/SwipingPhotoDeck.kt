package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.ChallengeLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun SwipingPhotoDeck(
    logs: List<ChallengeLog>,
    onSwipeStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedLog by remember { mutableStateOf<ChallengeLog?>(null) }
    var showFullscreen by remember { mutableStateOf(false) }

    val offsetX = remember { Animatable(0f) }
    val fadeAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val log = logs[currentIndex]
        
        val animatedOffsetX = offsetX.value
        val animatedRotation = offsetX.value / 30f
        val dragAlpha = 1f - minOf(1f, abs(offsetX.value) / 1000f)
        val finalAlpha = fadeAlpha.value * dragAlpha
        
        if (finalAlpha > 0.01f || showFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        translationX = animatedOffsetX
                        rotationZ = animatedRotation
                        alpha = finalAlpha
                        shadowElevation = 32.dp.toPx()
                        shape = RoundedCornerShape(16.dp)
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(log.id, currentIndex) {
                        if (logs.size > 1) {
                            detectHorizontalDragGestures(
                                onDragStart = { onSwipeStateChange(true) },
                                onDragEnd = {
                                    onSwipeStateChange(false)
                                    scope.launch {
                                        val threshold = size.width * 0.25f
                                        if (abs(offsetX.value) > threshold) {
                                            val endX = if (offsetX.value > 0) size.width.toFloat() * 1.5f else -size.width.toFloat() * 1.5f
                                            offsetX.animateTo(endX, spring())
                                            
                                            // Next image
                                            currentIndex = (currentIndex + 1) % logs.size
                                            offsetX.snapTo(0f)
                                            fadeAlpha.snapTo(0f)
                                            fadeAlpha.animateTo(1f, androidx.compose.animation.core.tween(150))
                                        } else {
                                            offsetX.animateTo(0f, spring())
                                        }
                                    }
                                },
                                onDragCancel = {
                                    onSwipeStateChange(false)
                                    scope.launch { offsetX.animateTo(0f, spring()) }
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount)
                                }
                            }
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedLog = logs[currentIndex]
                        showFullscreen = true
                    }
            ) {
                AsyncImage(
                    model = log.imagePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }

    if (showFullscreen && selectedLog != null) {
        FullscreenImageDialog(
            log = selectedLog!!,
            onDismiss = { showFullscreen = false }
        )
    }
}
