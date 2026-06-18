package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.PathEffect
import kotlin.math.absoluteValue

val sessionOffset = kotlin.random.Random.nextInt(100)

enum class HighlightStyle {
    WAVE, BRUSH, DOUBLE, DOTTED, SOLID
}

val PrimaryColors = listOf(
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF42A5F5), // Blue
    Color(0xFF26C6DA), // Cyan
    Color(0xFF009688), // Teal
    Color(0xFF66BB6A), // Green
    Color(0xFFD4E157), // Lime
    Color(0xFFFFCA28), // Amber
    Color(0xFFFFA726), // Orange
    Color(0xFFEC407A), // Pink
    Color(0xFFAB47BC)  // Violet
)

fun getPromptAccentColor(word: String, isDynamicEnabled: Boolean, systemPrimary: Color): Color {
    if (isDynamicEnabled) return systemPrimary
    val hash = word.hashCode().absoluteValue
    return PrimaryColors[hash % PrimaryColors.size]
}

fun getHighlightStyle(prompt: String): HighlightStyle {
    val hash = prompt.hashCode().absoluteValue + sessionOffset
    val styles = HighlightStyle.values()
    return styles[hash % styles.size]
}

fun extractHighlights(prompt: String): String {
    if (prompt.contains("*")) return prompt
    
    val stopWords = setOf(
        "capture", "take", "photo", "photograph", "picture", "image", 
        "that", "this", "something", "feels", "like", "with", "from", 
        "your", "what", "where", "when", "why", "who", "how", "make",
        "find", "into", "around", "about", "look", "show", "tell", "which",
        "would", "could", "should", "those", "these", "their", "there", "then",
        "than", "them", "they", "have", "been", "being", "were", "will",
        "someone", "moments", "scene", "meaning"
    )
    
    val words = prompt.split(Regex("[\\s\\p{Punct}]+"))
        .filter { it.length > 4 && it.lowercase() !in stopWords } 
    
    val sortedWords = words.sortedByDescending { it.length }.distinctBy { it.lowercase() }
    
    // Select top 1-2 non-adjacent words
    val selectedWords = mutableListOf<String>()
    
    for (word in sortedWords) {
        if (selectedWords.size >= 2) break // Max 2 highlights usually
        
        // check adjacency in original prompt
        val currentIdx = prompt.indexOf(word, ignoreCase = true)
        val isAdjacent = selectedWords.any { existing ->
            val exIdx = prompt.indexOf(existing, ignoreCase = true)
            val minIdx = minOf(currentIdx, exIdx)
            val maxIdx = maxOf(currentIdx, exIdx)
            val firstWordLen = if (currentIdx < exIdx) word.length else existing.length
            if (minIdx + firstWordLen > prompt.length) return@any false
            val between = prompt.substring(minIdx + firstWordLen, maxIdx)
            between.trim().length <= 3 // close enough to be adjacent
        }
        
        if (!isAdjacent) {
            selectedWords.add(word)
        }
    }
    
    var highlightedPrompt = prompt
    for (word in selectedWords) {
        highlightedPrompt = highlightedPrompt.replace(Regex("(?i)\\b$word\\b")) { matchResult ->
            "*${matchResult.value}*"
        }
    }
    return highlightedPrompt
}

@Composable
fun AnimatedPromptText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    isDynamicColor: Boolean = false,
    isStatic: Boolean = false,
    useAccentColors: Boolean = true
) {
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val transition = rememberInfiniteTransition(label = "anim_transition")
    val phase by if (isStatic) remember { mutableStateOf(0f) } else transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * kotlin.math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "anim_phase"
    )
    
    val revealProgress = remember { androidx.compose.animation.core.Animatable(if (isStatic) 1f else 0f) }
    LaunchedEffect(text) {
        if (!isStatic) {
            revealProgress.snapTo(0f)
            revealProgress.animateTo(1f, tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
    }
    
    val textAlpha = if (revealProgress.value > 0.5f) ((revealProgress.value - 0.5f) * 2f).coerceIn(0f, 1f) else 0f
    val highlightAlpha = if (revealProgress.value < 0.5f) (revealProgress.value * 2f).coerceIn(0f, 1f) else 1f
    
    val systemPrimary = MaterialTheme.colorScheme.primary
    val neutralOutline = MaterialTheme.colorScheme.outline
    val processedText = remember(text) { extractHighlights(text) }
    val styleType = remember(text) { getHighlightStyle(text) }
    
    // Animate color in
    Text(
        text = buildAnnotatedString {
            val parts = processedText.split("*")
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1 && part.isNotEmpty()) {
                    pushStringAnnotation(tag = "highlight", annotation = part)
                    val wordHash = part.hashCode().absoluteValue
                    val wordPhaseOffset = (wordHash % 100) / 100f * kotlin.math.PI * 2f
                    val localPhase = phase + wordPhaseOffset
                    
                    val accent = if (useAccentColors) getPromptAccentColor(part, isDynamicColor, systemPrimary) else color
                    val animSin = if (isStatic) 0f else kotlin.math.sin(localPhase).toFloat()
                    
                    // Ink Shift: subtle color shift
                    val shiftColor = if (!useAccentColors) color else accent.copy(
                        red = (accent.red + animSin * 0.08f).coerceIn(0f, 1f),
                        green = (accent.green + animSin * 0.08f).coerceIn(0f, 1f),
                        blue = (accent.blue - animSin * 0.08f).coerceIn(0f, 1f)
                    )
                    
                    withStyle(style = SpanStyle(color = shiftColor.copy(alpha = shiftColor.alpha * highlightAlpha))) {
                        append(part)
                    }
                    pop()
                } else {
                    withStyle(style = SpanStyle(color = color.copy(alpha = color.alpha * textAlpha))) {
                        append(part)
                    }
                }
            }
        },
        style = style,
        color = Color.Transparent, // Ensure base text doesn't draw over annotated styles
        modifier = modifier
            .drawBehind {
                textLayoutResult?.let { layout ->
                    val highlights = layout.layoutInput.text.getStringAnnotations("highlight", 0, layout.layoutInput.text.length)

                    highlights.forEach { annotation ->
                        val wordStr = annotation.item
                        val baseColor = getPromptAccentColor(wordStr, isDynamicColor, systemPrimary)
                        val accentColor = if (useAccentColors) baseColor else neutralOutline
                        
                        val start = annotation.start
                        val end = annotation.end
                        val boundingBox = layout.getBoundingBox(start)
                        val endBox = layout.getBoundingBox(end - 1)
                        
                        val startX = boundingBox.left
                        val endX = startX + (endBox.right - startX) * highlightAlpha
                        val yBottom = boundingBox.bottom
                        
                        val wordHash = wordStr.hashCode().absoluteValue
                        val wordPhaseOffset = (wordHash % 100) / 100f * kotlin.math.PI * 2f
                        val localPhase = phase + wordPhaseOffset
                        val animOffset = kotlin.math.sin(localPhase).toFloat()

                        when (styleType) {
                            HighlightStyle.BRUSH -> {
                                val yOffset = yBottom + 2.dp.toPx()
                                drawLine(
                                    color = accentColor.copy(alpha = 0.5f + animOffset * 0.15f),
                                    start = androidx.compose.ui.geometry.Offset(startX, yOffset),
                                    end = androidx.compose.ui.geometry.Offset(endX, yOffset - 1.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            HighlightStyle.DOUBLE -> {
                                val yOff1 = yBottom + 2.dp.toPx()
                                val yOff2 = yBottom + 6.dp.toPx()
                                val drift = animOffset * 1.5.dp.toPx()
                                drawLine(
                                    color = accentColor.copy(alpha = 0.7f + animOffset * 0.1f),
                                    start = androidx.compose.ui.geometry.Offset(startX - drift, yOff1),
                                    end = androidx.compose.ui.geometry.Offset(endX + drift, yOff1),
                                    strokeWidth = 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = accentColor.copy(alpha = 0.5f - animOffset * 0.1f),
                                    start = androidx.compose.ui.geometry.Offset(startX + drift, yOff2),
                                    end = androidx.compose.ui.geometry.Offset(endX - drift, yOff2),
                                    strokeWidth = 1.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            HighlightStyle.DOTTED -> {
                                val yOffset = yBottom + 4.dp.toPx()
                                drawLine(
                                    color = accentColor.copy(alpha = 0.8f + animOffset * 0.2f),
                                    start = androidx.compose.ui.geometry.Offset(startX, yOffset),
                                    end = androidx.compose.ui.geometry.Offset(endX, yOffset),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                                        phase * 5f
                                    )
                                )
                            }
                            HighlightStyle.SOLID -> {
                                val yOffset = yBottom + 3.dp.toPx()
                                val widthShrink = (1f - animOffset.absoluteValue * 0.05f)
                                val w = endX - startX
                                val mid = startX + w / 2f
                                drawLine(
                                    color = accentColor.copy(alpha = 0.8f),
                                    start = androidx.compose.ui.geometry.Offset(mid - (w / 2f) * widthShrink, yOffset),
                                    end = androidx.compose.ui.geometry.Offset(mid + (w / 2f) * widthShrink, yOffset),
                                    strokeWidth = 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            HighlightStyle.WAVE -> {
                                val yOffset = yBottom + 4.dp.toPx()
                                val wavePath = Path()
                                val waveLength = 16.dp.toPx()
                                val amplitude = 1.2.dp.toPx()
                                
                                wavePath.moveTo(startX, yOffset)
                                var currentX = startX
                                while (currentX < endX) {
                                    val nextX = minOf(currentX + 2.dp.toPx(), endX)
                                    val sineY = yOffset + amplitude * kotlin.math.sin((currentX / waveLength) * 2 * kotlin.math.PI + localPhase).toFloat()
                                    wavePath.lineTo(nextX, sineY)
                                    currentX = nextX
                                }
                                
                                drawPath(
                                    path = wavePath,
                                    color = accentColor.copy(alpha = 0.6f + animOffset * 0.1f),
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                }
            }.let { base ->
                if (isStatic) modifier else base
            },
        textAlign = textAlign,
        onTextLayout = { textLayoutResult = it }
    )
}

