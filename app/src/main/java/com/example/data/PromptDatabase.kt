package com.example.data

object PromptDatabase {

    data class Concept(val text: String, val tags: Set<String>)

    val allPrompts = listOf(
        // Observation
        Concept("What's directly below you?", setOf("Observation", "Simple")),
        Concept("Photograph your current view exactly as it is.", setOf("Observation", "Simple")),
        Concept("Find something perfectly ordinary and make it look monumental.", setOf("Observation", "Perspective")),
        Concept("Look up. Keep looking up until you find something worth capturing.", setOf("Observation", "Exploration")),
        Concept("Photograph a crack, a flaw, or a mistake that has accidentally become beautiful.", setOf("Observation", "Detail")),
        Concept("Follow a shadow to its source.", setOf("Observation", "Light")),
        Concept("Find a splash of color in an otherwise monochromatic space.", setOf("Observation", "Color")),
        Concept("Photograph a piece of trash that somehow looks cinematic.", setOf("Observation", "Urban")),
        Concept("Find a surface that has been worn completely smooth by time and touch.", setOf("Observation", "Time", "Texture")),
        Concept("Look for an object that is perfectly camouflaged in its environment.", setOf("Observation", "Pattern")),
        Concept("Find a place where two very different materials meet.", setOf("Observation", "Texture")),
        Concept("Photograph the ugliest thing in the room from an angle that makes it look interesting.", setOf("Observation", "Perspective")),

        // Storytelling
        Concept("Capture evidence that something just happened here.", setOf("Storytelling", "Mystery")),
        Concept("Photograph an object that clearly tells the story of its owner.", setOf("Storytelling", "Human")),
        Concept("Find a place that feels like the beginning of an adventure.", setOf("Storytelling", "Adventure")),
        Concept("Capture a scene that feels like the final shot of a movie.", setOf("Storytelling", "Emotion")),
        Concept("Photograph two objects that seem to be having a conversation.", setOf("Storytelling", "Humor")),
        Concept("Find something that was left behind in a hurry.", setOf("Storytelling", "Mystery")),
        Concept("Photograph a scene that makes you wonder 'Why?'", setOf("Storytelling", "Curiosity")),
        Concept("Find a discarded item and photograph it like it's a priceless artifact.", setOf("Storytelling", "Perspective")),
        Concept("Capture a moment that feels like an interruption.", setOf("Storytelling", "Time")),

        // Adventure & Exploration
        Concept("Find something worth turning around for.", setOf("Adventure", "Perspective")),
        Concept("Walk roughly 100 steps in a random direction. Photograph the most interesting thing you find there.", setOf("Adventure", "Exploration")),
        Concept("Take a different route than you normally would and document the detour.", setOf("Exploration", "Adventure")),
        Concept("Find a doorway that invites you to enter, even if you can't.", setOf("Exploration", "Architecture")),
        Concept("Go to the highest point you can easily access right now.", setOf("Exploration", "Perspective")),
        Concept("Find a path that isn't on any map.", setOf("Exploration", "Nature")),
        Concept("Photograph a place where boundaries blur.", setOf("Exploration", "Abstract")),
        Concept("Find the darkest safely accessible corner of your environment.", setOf("Exploration", "Light")),
        Concept("Go to a window you rarely look out of and capture the view.", setOf("Exploration", "Perspective")),

        // Emotional & Nostalgia
        Concept("Photograph something that makes you feel a sudden wave of nostalgia.", setOf("Nostalgia", "Emotion")),
        Concept("Find a space that feels incredibly quiet.", setOf("Emotion", "Space")),
        Concept("Capture a moment of absolute stillness.", setOf("Emotion", "Time")),
        Concept("Photograph a texture that you can almost feel just by looking at it.", setOf("Emotion", "Detail")),
        Concept("Find the most beautiful thing in the least beautiful place.", setOf("Emotion", "Perspective")),
        Concept("Photograph a color that reminds you of childhood.", setOf("Nostalgia", "Color")),
        Concept("Capture a scene that feels vaguely threatening but is completely safe.", setOf("Emotion", "Mystery")),
        Concept("Find something that represents comfort.", setOf("Emotion", "Home")),
        Concept("Photograph a place where you used to spend a lot of time.", setOf("Nostalgia", "Memory")),

        // Time-based
        Concept("Photograph something that won't be here tomorrow.", setOf("Time", "Storytelling")),
        Concept("Capture a scene that is actively changing.", setOf("Time", "Observation")),
        Concept("Find an object that wears its age proudly.", setOf("Time", "Detail")),
        Concept("Take a picture that captures the exact feeling of the current hour.", setOf("Time", "Emotion")),
        Concept("Photograph something that moves faster than you can perceive.", setOf("Time", "Movement")),
        Concept("Capture a clock or timepiece in an unexpected context.", setOf("Time", "Observation")),
        Concept("Find evidence of a season that has already passed.", setOf("Time", "Nature")),
        Concept("Photograph something that takes years to form.", setOf("Time", "Nature")),

        // Human-focused
        Concept("Photograph someone's hands performing a routine task.", setOf("Human", "Detail")),
        Concept("Capture a candid moment of joy or frustration from a distance.", setOf("Human", "Emotion")),
        Concept("Find a trace left behind by a stranger.", setOf("Human", "Mystery")),
        Concept("Photograph a crowd as a single organism.", setOf("Human", "Movement", "Abstract")),
        Concept("Capture a portrait of someone without showing their face.", setOf("Human", "Storytelling")),
        Concept("Find an object that someone tried to fix themselves.", setOf("Human", "Storytelling")),
        Concept("Photograph a purely human mistake.", setOf("Human", "Humor")),

        // Architecture & Space
        Concept("Photograph a space that feels distinctly intended for someone else.", setOf("Architecture", "Space")),
        Concept("Look for a pattern in the architecture around you that repeats three times.", setOf("Architecture", "Observation")),
        Concept("Capture the way light interacts with a building's geometry.", setOf("Architecture", "Light")),
        Concept("Find a sharp corner or an abrupt edge that divides the visible world.", setOf("Architecture", "Perspective")),
        Concept("Photograph a building that looks like it doesn't belong where it is.", setOf("Architecture", "Urban")),
        Concept("Capture a reflection of a building in something other than glass or water.", setOf("Architecture", "Reflection")),
        Concept("Find a detail on a building that serves zero functional purpose.", setOf("Architecture", "Detail")),

        // Nature & Weather
        Concept("Photograph something growing where it shouldn't.", setOf("Nature", "Survival")),
        Concept("Capture the visual evidence of today's weather.", setOf("Weather", "Observation")),
        Concept("Find a piece of nature that looks like human art.", setOf("Nature", "Abstract")),
        Concept("Notice the sky today. What's the most interesting part of it?", setOf("Weather", "Sky")),
        Concept("Photograph a single leaf or blade of grass as if it's the protagonist.", setOf("Nature", "Perspective")),
        Concept("Capture the wind without showing the sky.", setOf("Nature", "Weather")),
        Concept("Find a perfect geometric shape occurring naturally.", setOf("Nature", "Abstract")),

        // Mystery & Imaginative Scenarios
        Concept("Photograph something future archaeologists would find deeply confusing.", setOf("Imaginative", "Humor", "Storytelling")),
        Concept("Capture a scene that looks like a glitch in reality.", setOf("Mystery", "Abstract")),
        Concept("Find a secret message hiding in plain sight.", setOf("Mystery", "Observation")),
        Concept("Take a photo of something you'd want to remember if you were leaving Earth tomorrow.", setOf("Imaginative", "Emotion")),
        Concept("Photograph a portal to another dimension (or something that looks like one).", setOf("Imaginative", "Mystery")),
        Concept("Find something that looks like it fell from space.", setOf("Mystery", "Imaginative")),
        Concept("Capture an object that looks like it has a secret life when you aren't looking.", setOf("Imaginative", "Humor")),

        // Creative Constraints
        Concept("Take a photo using only one hand, without looking at the screen.", setOf("Constraint", "Random")),
        Concept("Get impossibly close to an ordinary object.", setOf("Constraint", "Detail")),
        Concept("Photograph an invisible force (wind, gravity, heat).", setOf("Constraint", "Nature")),
        Concept("Capture a reflection, but not the object reflecting.", setOf("Constraint", "Light", "Observation")),
        Concept("Take a photograph using an object as a makeshift filter.", setOf("Constraint", "Abstract")),
        Concept("Find a way to frame a shot using your surroundings instead of the camera edge.", setOf("Constraint", "Perspective")),
        Concept("Photograph a primary color in an otherwise dull environment.", setOf("Constraint", "Color")),

        // Humor
        Concept("Find an inanimate object that looks hopelessly confused.", setOf("Humor", "Pareidolia")),
        Concept("Photograph a very serious sign being ignored.", setOf("Humor", "Human")),
        Concept("Capture an awkward juxtaposition between two completely unrelated things.", setOf("Humor", "Observation")),
        Concept("Take a picture of something trying its best, but failing.", setOf("Humor", "Storytelling")),
        Concept("Find something aggressively ugly and photograph it lovingly.", setOf("Humor", "Emotion")),
        Concept("Capture the aftermath of a minor, harmless disaster.", setOf("Humor", "Storytelling")),

        // Creativity & Imagination
        Concept("Find something that looks like it's waiting for someone.", setOf("Imaginative", "Emotion", "Storytelling")),
        Concept("Photograph something that would make a great setting for a science fiction story.", setOf("Imaginative", "Urban")),
        Concept("Capture an object that looks secretly dangerous.", setOf("Imaginative", "Detail")),
        Concept("Photograph a shadow that is more interesting than the object casting it.", setOf("Light", "Observation")),
        Concept("Find a space that feels like a secret.", setOf("Mystery", "Space")),
        
        // Perspective shifts
        Concept("Get as low as you possibly can. What do you see?", setOf("Perspective", "Exploration")),
        Concept("Look closely at a surface you walk on every day but never notice.", setOf("Detail", "Observation")),
        Concept("Photograph something through a gap, a hole, or a window.", setOf("Perspective", "Observation")),
        Concept("Find a small detail that fundamentally changes the meaning of its surroundings.", setOf("Detail", "Storytelling")),
        Concept("Frame your shot so that the background is perfectly blank.", setOf("Perspective", "Abstract")),
        
        // Interactions and traces
        Concept("Find a place where nature is slowly reclaiming something man-made.", setOf("Nature", "Time")),
        Concept("Photograph something built to last forever that is falling apart.", setOf("Architecture", "Time", "Irony")),
        Concept("Capture evidence of a choice someone made.", setOf("Human", "Storytelling")),
        Concept("Find an accident or a spill that created an interesting pattern.", setOf("Abstract", "Observation")),
        Concept("Photograph an object that has clearly been forgotten.", setOf("Storytelling", "Emotion")),
        
        // Sensory & Emotion
        Concept("Photograph the feeling of isolation in a busy space.", setOf("Emotion", "Human", "Space")),
        Concept("Find something that vividly reminds you of a childhood summer.", setOf("Nostalgia", "Time")),
        Concept("Capture something so perfectly organized it feels unnatural.", setOf("Observation", "Order")),
        Concept("Find evidence of someone's frustration.", setOf("Human", "Storytelling")),
        Concept("Capture an object looking tired after a long day.", setOf("Humor", "Emotion", "Pareidolia")),
        
        // Micro & Macro
        Concept("Fill the frame entirely with a single texture.", setOf("Detail", "Abstract")),
        Concept("Photograph the sky reflected in something that isn't a mirror or a window.", setOf("Reflection", "Light", "Sky")),
        Concept("Find a tiny ecosystem operating completely unnoticed.", setOf("Nature", "Detail")),
        Concept("Capture the empty space *between* two objects.", setOf("Space", "Perspective")),
        Concept("Photograph an entire world contained within one foot of space.", setOf("Detail", "Space")),
        
        // Urban & Society
        Concept("Photograph something designed to go unnoticed, doing its job perfectly.", setOf("Urban", "Observation")),
        Concept("Find an interaction between artificial light and nature.", setOf("Light", "Nature", "Urban")),
        Concept("Capture a temporary fix that became permanent.", setOf("Storytelling", "Urban", "Time")),
        Concept("Find a path created by continuous human use, not by design.", setOf("Human", "Observation")),
        Concept("Photograph a clash of two different eras of design.", setOf("Urban", "Time")),
        
        // Conceptual
        Concept("Photograph 'waiting'.", setOf("Concept", "Emotion", "Time")),
        Concept("Photograph 'speed' without using motion blur.", setOf("Concept", "Observation")),
        Concept("Capture 'silence'.", setOf("Concept", "Emotion")),
        Concept("Photograph an apology, real or metaphorical.", setOf("Concept", "Storytelling")),
        Concept("Find visual proof of gravity.", setOf("Concept", "Physics")),
        Concept("Photograph 'chaos'.", setOf("Concept", "Abstract")),
        Concept("Capture a boundary that nobody is allowed to cross.", setOf("Concept", "Space")),

        // Light & Shadow
        Concept("Photograph a shadow that completely changes the shape of its owner.", setOf("Light", "Abstract")),
        Concept("Find intensely harsh light and capture what it reveals.", setOf("Light", "Observation")),
        Concept("Capture an area illuminated solely by screen light.", setOf("Light", "Urban", "Human")),
        Concept("Photograph a beam of light hitting the seemingly wrong spot.", setOf("Light", "Observation")),
        Concept("Find a spot of perfect shade on a bright day.", setOf("Light", "Space")),

        // Color & Pattern
        Concept("Find two colors that shouldn't look good together but do.", setOf("Color", "Observation")),
        Concept("Photograph a pattern that is abruptly interrupted.", setOf("Pattern", "Abstract")),
        Concept("Find predominantly blue in a place that has a warm atmosphere.", setOf("Color", "Emotion")),
        Concept("Capture a gradient occurring naturally.", setOf("Color", "Nature", "Abstract")),
        Concept("Photograph an object painted a color that betrays its purpose.", setOf("Color", "Storytelling")),

        // Movement & Stillness
        Concept("Photograph the wake of something moving.", setOf("Movement", "Trace")),
        Concept("Capture an object suspended mid-fall or mid-flight.", setOf("Movement", "Time")),
        Concept("Find something that refuses to move despite everything around it.", setOf("Movement", "Observation")),
        Concept("Photograph wind.", setOf("Movement", "Nature", "Concept")),

        // Sound representation
        Concept("Take a picture of an incredibly loud object while it's completely silent.", setOf("Sound", "Observation")),
        Concept("Capture something that looks like the visual equivalent of a hum.", setOf("Sound", "Abstract")),
        Concept("Photograph a place where a famous song could have been written.", setOf("Sound", "Imaginative")),

        // Memory & Transformation
        Concept("Photograph something that reminds you of a friend you haven't spoken to in years.", setOf("Memory", "Human")),
        Concept("Find an object that has been transformed by a weather event.", setOf("Weather", "Transformation")),
        Concept("Capture an object that is clearly no longer used for its original purpose.", setOf("Transformation", "Storytelling")),

        // Special / Rare Events
        Concept("Photograph something you'll never see again.", setOf("Time", "Rare", "Memory")),
        Concept("Find a place you've walked past for years but never truly noticed.", setOf("Observation", "Rare", "Urban")),
        Concept("Capture something worth showing your younger self.", setOf("Emotion", "Rare", "Nostalgia")),
        Concept("Ask someone to tell you a story and photograph what remains after they leave.", setOf("Human", "Rare", "Storytelling")),
        Concept("Photograph something that deserves to be remembered.", setOf("Memory", "Rare", "Emotion"))
    )

    fun getRandomPrompt(): String {
        return getUnusedPrompt(emptyList())
    }

    fun getUnusedPrompt(excludedPrompts: List<String>): String {
        val tagFrequencies = mutableMapOf<String, Int>()
        
        // Lowercase versions for matching
        val cleanExcluded = excludedPrompts.map { it.lowercase() }

        // Analyze history to determine tag frequencies (penalize concepts we've seen)
        for (historicText in cleanExcluded) {
            val matchingConcept = allPrompts.find { it.text.lowercase() == historicText }
            if (matchingConcept != null) {
                for (tag in matchingConcept.tags) {
                    tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 1
                }
            } else {
                // If it's an old prompt that used the combinatorial logic, try finding matching words
                for (concept in allPrompts) {
                    val words = concept.text.lowercase().split(" ")
                    val historicWords = historicText.split(" ")
                    val common = words.intersect(historicWords.toSet())
                    if (common.size > 3) {
                        for (tag in concept.tags) {
                            tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 1
                        }
                    }
                }
            }
        }
        
        // Heavily weight the most recent concepts for strict cooldown
        val recentHistory = cleanExcluded.takeLast(7)
        for (historicText in recentHistory) {
            val matchingConcept = allPrompts.find { it.text.lowercase() == historicText }
            if (matchingConcept != null) {
                for (tag in matchingConcept.tags) {
                    tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 5 // Heavy penalty for recent
                }
            }
        }
        
        // Filter out strings that have been entirely used before
        val novelCandidates = allPrompts.filter { it.text.lowercase() !in cleanExcluded }
        
        if (novelCandidates.isEmpty()) {
            return "Find a new way to see the world today."
        }
        
        // 10% chance to consider rare prompts, otherwise only consider regular prompts
        val allowRare = (0..100).random() < 10
        val filteredCandidates = if (allowRare) {
            novelCandidates.filter { it.tags.contains("Rare") }
        } else {
            novelCandidates.filter { !it.tags.contains("Rare") }
        }
        
        val finalCandidates = filteredCandidates.ifEmpty { novelCandidates }
        
        // Score based on conceptual repetition penalty + random jitter
        val scoredCandidates = finalCandidates.map { candidate ->
            var penalty = 0
            candidate.tags.forEach { tag ->
                penalty += tagFrequencies.getOrDefault(tag, 0)
            }
            // Add a small random jitter to avoid deterministic loops
            val score = penalty + (0..10).random()
            score to candidate
        }.sortedBy { it.first }
        
        return scoredCandidates.first().second.text
    }

    fun isRarePrompt(promptText: String): Boolean {
        return allPrompts.find { it.text.equals(promptText, ignoreCase = true) }?.tags?.contains("Rare") == true
    }

    fun getEngineStats(excludedPrompts: List<String>): String {
        val tagFrequencies = mutableMapOf<String, Int>()
        
        var matchCount = 0
        for (prompt in excludedPrompts) {
            val cleanPrompt = prompt.lowercase()
            val match = allPrompts.find { it.text.lowercase() == cleanPrompt }
            if (match != null) {
                matchCount++
                match.tags.forEach { tag ->
                    tagFrequencies[tag] = tagFrequencies.getOrDefault(tag, 0) + 1
                }
            }
        }
        
        val sortedTags = tagFrequencies.entries.sortedByDescending { it.value }
        val topTags = sortedTags.take(3).joinToString(", ") { "${it.key} (${it.value})" }
        val allTags = allPrompts.flatMap { it.tags }.toSet()
        val unusedTags = (allTags - tagFrequencies.keys).joinToString(", ").takeIf { it.isNotEmpty() } ?: "None"
        
        return """
            Engine Status: Handcrafted Pool
            Prompts Available: ${allPrompts.size}
            History Analyzed: ${excludedPrompts.size} valid prompts
            Exact Matches: $matchCount
            Top Concepts Avoided: $topTags
            Unused Concepts: $unusedTags
            Cooldown Memory: Enforced via Conceptual Scoring
        """.trimIndent()
    }
}
