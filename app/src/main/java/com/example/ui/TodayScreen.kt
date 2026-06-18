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
                .padding(top = 32.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            
            if (todayLogs.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )
            
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
            val phase by androidx.compose.animation.core.rememberInfiniteTransition(label = "wave_transition").animateFloat(
                initialValue = 0f,
                targetValue = (2.0 * Math.PI).toFloat(),
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing)
                ),
                label = "wave_phase"
            )
            val accentColor = MaterialTheme.colorScheme.primary

            Text(
                text = buildAnnotatedString {
                    append("“")
                    val parts = activePrompt.split("*")
                    parts.forEachIndexed { index, part ->
                        if (index % 2 == 1) { // odd means surrounded by *
                            pushStringAnnotation(tag = "highlight", annotation = part)
                            withStyle(style = SpanStyle(color = accentColor)) {
                                append(part)
                            }
                            pop()
                        } else {
                            append(part)
                        }
                    }
                    append("”")
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Normal, // Softer editorial feel
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .drawBehind { // Wavy underline
                        textLayoutResult?.let { layout ->
                            val highlights = layout.layoutInput.text.getStringAnnotations("highlight", 0, layout.layoutInput.text.length)
                            highlights.forEach { annotation ->
                                val start = annotation.start
                                val end = annotation.end
                                val boundingBox = layout.getBoundingBox(start)
                                val endBox = layout.getBoundingBox(end - 1)
                                
                                val yOffset = boundingBox.bottom + 4.dp.toPx()
                                val startX = boundingBox.left
                                val endX = endBox.right
                                
                                val wavePath = androidx.compose.ui.graphics.Path()
                                val waveLength = 24.dp.toPx()
                                val amplitude = 2.dp.toPx()
                                
                                wavePath.moveTo(startX, yOffset)
                                var currentX = startX
                                while (currentX < endX) {
                                    val nextX = minOf(currentX + 2.dp.toPx(), endX)
                                    // Move phase in place rather than sliding
                                    val sineY = yOffset + amplitude * kotlin.math.sin((currentX / waveLength) * 2 * Math.PI + kotlin.math.sin(phase.toDouble()) * Math.PI).toFloat()
                                    wavePath.lineTo(nextX, sineY)
                                    currentX = nextX
                                }
                                
                                drawPath(
                                    path = wavePath,
                                    color = accentColor.copy(alpha = 0.6f),
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                onTextLayout = { textLayoutResult = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (todayLogs.isNotEmpty()) {
                SwipingPhotoDeck(logs = todayLogs, onSwipeStateChange = onSwipeStateChange, modifier = Modifier.weight(1f).fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Button(
                onClick = {
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
            
            Spacer(modifier = Modifier.height(84.dp).navigationBarsPadding())
        }
    }
}
