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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration

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
fun TodayScreen(viewModel: MainViewModel) {
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
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
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
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.outline)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("“")
                    val parts = activePrompt.split("*")
                    parts.forEachIndexed { index, part ->
                        if (index % 2 == 1) { // odd means surrounded by *
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.onBackground)) {
                                append(part)
                            }
                        } else {
                            append(part)
                        }
                    }
                    append("”")
                },
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (todayLogs.isNotEmpty()) {
                PhotoDeck(logs = todayLogs, modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .dashedBorder(
                            color = MaterialTheme.colorScheme.outline,
                            width = 2.dp,
                            radius = 40.dp
                        )
                        .clickable {
                            val uri = viewModel.createImageUri()
                            capturedUri = uri
                            cameraLauncher.launch(uri)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AWAITING DISCOVERY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
        }
    }
}

@Composable
fun PhotoDeck(
    logs: List<com.example.data.ChallengeLog>,
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
                    .padding(bottom = 64.dp, top = 32.dp, start = 32.dp, end = 32.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationY = animatedOffset.toPx()
                        rotationZ = animatedRotation
                        alpha = animatedAlpha
                        shadowElevation = if (i == 0) 16.dp.toPx() else 8.dp.toPx()
                    }
                    .clip(RoundedCornerShape(40.dp))
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
