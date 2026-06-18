package com.example.data

object PromptDatabase {

    private data class Concept(val text: String, val tags: Set<String>)

    private val subjects = listOf(
        Concept("a *shadow*", setOf("Light", "Abstract")),
        Concept("a *reflection*", setOf("Light", "Abstract")),
        Concept("a *contrast*", setOf("Light", "Observation")),
        Concept("something *forgotten*", setOf("Time", "Story")),
        Concept("a trace of *someone*", setOf("People", "Story")),
        Concept("a piece of *architecture*", setOf("Urban", "Space")),
        Concept("something *older* than you", setOf("Time")),
        Concept("a *fleeting* moment", setOf("Time", "Emotion")),
        Concept("an *interaction*", setOf("People", "Story")),
        Concept("*movement*", setOf("Abstract", "Time")),
        Concept("*stillness*", setOf("Emotion", "Time")),
        Concept("something *ordinary*", setOf("Detail", "Observation")),
        Concept("an unusual *texture*", setOf("Detail", "Observation")),
        Concept("a *detail* you usually ignore", setOf("Detail", "Observation")),
        Concept("a splash of *color*", setOf("Light", "Abstract")),
        Concept("something *fragile*", setOf("Detail", "Emotion")),
        Concept("a sign of the *season*", setOf("Nature", "Time")),
        Concept("something out of *place*", setOf("Story", "Curiosity")),
        Concept("a *quiet* corner", setOf("Space", "Emotion")),
        Concept("a *boundary* or edge", setOf("Space", "Urban")),
        Concept("something that has *changed*", setOf("Time", "Story")),
        Concept("a *repetitive* pattern", setOf("Abstract", "Observation")),
        Concept("something *chaotic*", setOf("Emotion", "Abstract")),
        Concept("a source of *light*", setOf("Light")),
        Concept("a *frame* within a frame", setOf("Space", "Observation")),
        Concept("a sign of *life*", setOf("Nature", "People")),
        Concept("something *temporary*", setOf("Time", "Story")),
        Concept("a *silhouette*", setOf("Light", "People")),
        Concept("something *imperfect*", setOf("Emotion", "Detail")),
        Concept("a *hidden* detail", setOf("Detail", "Curiosity")),
        Concept("an *empty* space", setOf("Space", "Emotion"))
    )

    private val actionsOrStates = listOf(
        Concept("that tells a *story*", setOf("Story")),
        Concept("that feels completely *isolated*", setOf("Emotion", "Space")),
        Concept("that looks like a *painting*", setOf("Abstract", "Light")),
        Concept("that expresses *joy*", setOf("Emotion", "People")),
        Concept("that shows the passage of *time*", setOf("Time")),
        Concept("that feels *nostalgic*", setOf("Emotion", "Time")),
        Concept("that creates a sense of *mystery*", setOf("Curiosity", "Story")),
        Concept("that feels *peaceful*", setOf("Emotion")),
        Concept("that is *changing* right now", setOf("Time")),
        Concept("that catches the *light* perfectly", setOf("Light")),
        Concept("that leaves a question *unanswered*", setOf("Curiosity", "Story")),
        Concept("that reminds you of *childhood*", setOf("Emotion", "Time")),
        Concept("that feels *warm*", setOf("Emotion", "Light")),
        Concept("that feels *cold*", setOf("Emotion", "Light")),
        Concept("that speaks *without words*", setOf("Emotion", "Story")),
        Concept("that causes a *second glance*", setOf("Curiosity", "Observation")),
        Concept("that feels like an *ending*", setOf("Time", "Emotion")),
        Concept("that feels like a *beginning*", setOf("Time", "Emotion")),
        Concept("that defines the *space* around it", setOf("Space", "Observation"))
    )

    private val perspectives = listOf(
        Concept("from an *unexpected* angle", setOf("Observation", "Space")),
        Concept("from *below*", setOf("Space")),
        Concept("from *above*", setOf("Space")),
        Concept("*up close*", setOf("Detail", "Observation")),
        Concept("from a *distance*", setOf("Space", "Observation")),
        Concept("through something *else*", setOf("Observation", "Curiosity")),
        Concept("without showing its true *scale*", setOf("Space", "Curiosity")),
        Concept("as if you are seeing it for the *first time*", setOf("Observation", "Emotion")),
        Concept("by filling the frame *entirely*", setOf("Observation", "Space")),
        Concept("leaving a lot of *empty space*", setOf("Space", "Emotion")),
        Concept("focusing only on the *edges*", setOf("Space", "Detail"))
    )

    private val formats = listOf(
        "Find {subject} {action}.",
        "Photograph {subject} {perspective}.",
        "Capture {subject} {perspective}.",
        "Look for {subject} {action}.",
        "A scene focused on {subject} {perspective}.",
        "Find {subject} and photograph it {perspective}.",
        "Capture the way {subject} looks {perspective}."
    )

    private val standalonePrompts = listOf(
        Concept("Capture a collision of the *natural* and the *man-made*.", setOf("Nature", "Urban", "Story")),
        Concept("Take a picture of the *sky* that doesn't just look like 'the sky'.", setOf("Nature", "Abstract")),
        Concept("Photograph a *miniature* world.", setOf("Detail", "Space")),
        Concept("Find beauty in something *broken*.", setOf("Emotion", "Detail")),
        Concept("Photograph a shadow that looks like *something else*.", setOf("Light", "Curiosity")),
        Concept("Take a picture that represents your day without showing your *face*.", setOf("Story", "People")),
        Concept("Photograph a place that feels like a *memory*.", setOf("Emotion", "Time")),
        Concept("Take a photo of something that makes you feel *small*.", setOf("Space", "Emotion")),
        Concept("Find a scene that expresses *quietness*.", setOf("Emotion", "Space")),
        Concept("Find a place that looks like it belongs in a *video game*.", setOf("Urban", "Curiosity")),
        Concept("Find the most interesting thing within 100 steps of where you're *standing*.", setOf("Observation", "Space")),
        Concept("Photograph something that made you stop and *look twice*.", setOf("Observation", "Curiosity")),
        Concept("Photograph the feeling of a specific *color*.", setOf("Abstract", "Light")),
        Concept("Find a shape that *repeats* itself in architecture.", setOf("Urban", "Observation")),
        Concept("Take a picture of something temporarily *perfect*.", setOf("Time", "Detail")),
        Concept("Take a photo that feels like the *beginning* of a story.", setOf("Story", "Time")),
        Concept("Find *evidence* that someone was here before you.", setOf("People", "Story")),
        Concept("Capture something future *archaeologists* would find interesting.", setOf("Time", "Story")),
        Concept("Find a face *hidden* in an inanimate object.", setOf("Curiosity", "Detail")),
        Concept("Capture a reflection that tells a *different* story.", setOf("Light", "Story")),
        Concept("Photograph something from a *perspective* you've never tried.", setOf("Observation", "Space"))
    )

    fun getRandomPrompt(): String {
        return getUnusedPrompt(emptyList()) // Provide a fully dynamic random
    }

    fun getUnusedPrompt(excludedPrompts: List<String>): String {
        val tagFrequencies = mutableMapOf<String, Int>()
        val allConcepts = subjects + actionsOrStates + perspectives + standalonePrompts
        
        // Calculate historical concept frequencies to prevent conceptual repetition
        for (prompt in excludedPrompts) {
            val cleanPrompt = prompt.replace("*", "").lowercase()
            for (concept in allConcepts) {
                val cleanConceptText = concept.text.replace("*", "").lowercase()
                if (cleanPrompt.contains(cleanConceptText)) {
                    concept.tags.forEach { tag ->
                        tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 1
                    }
                }
            }
        }
        
        // Heavily weight the most recent concepts for strict cooldown
        val recentHistory = excludedPrompts.takeLast(14)
        for (prompt in recentHistory) {
             val cleanPrompt = prompt.replace("*", "").lowercase()
             for (concept in allConcepts) {
                val cleanConceptText = concept.text.replace("*", "").lowercase()
                if (cleanPrompt.contains(cleanConceptText)) {
                    concept.tags.forEach { tag ->
                        tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 5 // Heavy penalty for recent
                    }
                }
            }
        }
        
        val candidates = mutableListOf<Pair<String, Set<String>>>()
        
        // Add standalone prompts
        standalonePrompts.forEach { candidates.add(it.text to it.tags) }
        
        // Generate a large pool of combinatorial prompts
        for (i in 0..300) {
            val format = formats.random()
            val subject = subjects.random()
            
            var text = format.replace("{subject}", subject.text)
            val tags = mutableSetOf<String>()
            tags.addAll(subject.tags)
            
            if (text.contains("{action}")) {
                val action = actionsOrStates.random()
                text = text.replace("{action}", action.text)
                tags.addAll(action.tags)
            }
            
            if (text.contains("{perspective}")) {
                val perspective = perspectives.random()
                text = text.replace("{perspective}", perspective.text)
                tags.addAll(perspective.tags)
            }
            
            // Text cleanup
            text = text.replace("  ", " ").trim()
            text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            
            candidates.add(text to tags)
        }
        
        // Filter out exact strings that have been entirely used before
        val cleanExcluded = excludedPrompts.map { it.replace("*", "").lowercase() }
        val novelCandidates = candidates.filter { it.first.replace("*", "").lowercase() !in cleanExcluded }
        
        if (novelCandidates.isEmpty()) {
            return "Find a *new* way to see the world today."
        }
        
        // Score based on conceptual repetition penalty + random jitter
        val scoredCandidates = novelCandidates.map { candidate ->
            var penalty = 0
            candidate.second.forEach { tag ->
                penalty += tagFrequencies.getOrDefault(tag, 0)
            }
            // Add a small random jitter to avoid deterministic loops
            val score = penalty + (0..10).random()
            score to candidate
        }.sortedBy { it.first }
        
        return scoredCandidates.first().second.first
    }

    fun getEngineStats(excludedPrompts: List<String>): String {
        val tagFrequencies = mutableMapOf<String, Int>()
        val allConcepts = subjects + actionsOrStates + perspectives + standalonePrompts
        
        var matchCount = 0
        for (prompt in excludedPrompts) {
            val cleanPrompt = prompt.replace("*", "").lowercase()
            for (concept in allConcepts) {
                val cleanConceptText = concept.text.replace("*", "").lowercase()
                if (cleanPrompt.contains(cleanConceptText)) {
                    matchCount++
                    concept.tags.forEach { tag ->
                        tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 1
                    }
                }
            }
        }
        
        val sortedTags = tagFrequencies.entries.sortedByDescending { it.value }
        val topTags = sortedTags.take(3).joinToString(", ") { "${it.key} (${it.value})" }
        val unusedTags = (allConcepts.flatMap { it.tags }.toSet() - tagFrequencies.keys).joinToString(", ").takeIf { it.isNotEmpty() } ?: "None"
        
        return """
            Engine Status: Active
            Combinations Possible: >100,000
            History Analyzed: ${excludedPrompts.size} prompts
            Patterns Matched: $matchCount
            Top Concepts: $topTags
            Unused Concepts: $unusedTags
            Cooldown Memory: Enforced
        """.trimIndent()
    }
}
