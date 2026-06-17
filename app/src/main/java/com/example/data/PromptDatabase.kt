package com.example.data

import java.util.Calendar

object PromptDatabase {
    private val mondayPrompts = listOf( // Discovery
        "Find a place that looks like it belongs in a *video game*.",
        "Find the most interesting thing within 100 steps of where you're *standing*.",
        "Photograph something that made you stop and *look twice*."
    )
    
    private val tuesdayPrompts = listOf( // Color and design
        "Photograph the feeling of a specific *color*.",
        "Find a shape that *repeats* itself in architecture.",
        "Take a picture of something temporarily *perfect*."
    )
    
    private val wednesdayPrompts = listOf( // Storytelling
        "Take a photo that feels like the *beginning* of a story.",
        "Find *evidence* that someone was here before you.",
        "Capture something future *archaeologists* would find interesting."
    )
    
    private val thursdayPrompts = listOf( // Creativity
        "Find a face *hidden* in an inanimate object.",
        "Capture a reflection that tells a *different* story.",
        "Photograph something from a *perspective* you've never tried."
    )
    
    private val fridayPrompts = listOf( // Adventure
        "Capture a collision of the *natural* and the man-made.",
        "Take a picture of the *sky* that doesn't just look like 'the sky'.",
        "Photograph a *miniature* world."
    )
    
    private val saturdayPrompts = listOf( // Exploration
        "Find beauty in something *broken*.",
        "Photograph a shadow that looks like *something else*.",
        "Take a picture that represents your day without showing your *face*."
    )
    
    private val sundayPrompts = listOf( // Reflection and nostalgia
        "Photograph a place that feels like a *memory*.",
        "Take a photo of something that makes you feel *small*.",
        "Find a scene that expresses *quietness*."
    )

    fun getRandomPrompt(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> mondayPrompts.random()
            Calendar.TUESDAY -> tuesdayPrompts.random()
            Calendar.WEDNESDAY -> wednesdayPrompts.random()
            Calendar.THURSDAY -> thursdayPrompts.random()
            Calendar.FRIDAY -> fridayPrompts.random()
            Calendar.SATURDAY -> saturdayPrompts.random()
            Calendar.SUNDAY -> sundayPrompts.random()
            else -> mondayPrompts.random()
        }
    }
}
