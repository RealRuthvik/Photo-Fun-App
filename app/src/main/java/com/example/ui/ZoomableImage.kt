package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    model: Any?,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            if (scale.value > 1.0f) {
                                launch { scale.animateTo(1f) }
                                launch { offsetX.animateTo(0f) }
                                launch { offsetY.animateTo(0f) }
                            } else {
                                scale.animateTo(2.5f)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    coroutineScope.launch {
                        val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                        scale.snapTo(newScale)
                        val maxX = (size.width * (newScale - 1)) / 2f
                        val maxY = (size.height * (newScale - 1)) / 2f
                        val newOffsetX = (offsetX.value + pan.x * newScale).coerceIn(-maxX, maxX)
                        val newOffsetY = (offsetY.value + pan.y * newScale).coerceIn(-maxY, maxY)
                        if (newScale > 1f) {
                            offsetX.snapTo(newOffsetX)
                            offsetY.snapTo(newOffsetY)
                        } else {
                            offsetX.snapTo(0f)
                            offsetY.snapTo(0f)
                        }
                    }
                }
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offsetX.value,
                    translationY = offsetY.value
                )
        )
    }
}
