package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import java.util.Locale

class MemoryRetrievalEngine {

    /**
     * Scores and retrieves the most relevant memories for the incoming message context.
     * Prevents dumping unlimited memories into the prompt.
     */
    fun retrieveRelevantMemories(
        incomingMessage: MessageEntity,
        allMemories: List<MemoryEntity>,
        currentContext: ContextEntity?,
        limit: Int = 6
    ): List<MemoryEntity> {
        val now = System.currentTimeMillis()
        // Filter out expired temporary memories
        val activeMemories = allMemories.filter {
            it.expirationTimestamp == null || it.expirationTimestamp > now
        }

        if (activeMemories.isEmpty()) return emptyList()

        // Extract key terms from incoming message
        val searchTokens = extractKeywords(
            "${incomingMessage.contentText} ${incomingMessage.transcript} ${incomingMessage.mediaDescription} ${incomingMessage.reactionEmoji}"
        )

        val scoredList = activeMemories.map { memory ->
            val score = calculateMemoryScore(memory, searchTokens, currentContext)
            Pair(memory, score)
        }

        // Return memories with non-zero relevance score or pinned memories, sorted by score
        return scoredList
            .filter { (memory, score) -> memory.isPinned || score > 1.2f }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun calculateMemoryScore(
        memory: MemoryEntity,
        searchTokens: Set<String>,
        currentContext: ContextEntity?
    ): Float {
        var baseScore = 0.0f

        // Base importance weighting (1..5)
        baseScore += (memory.importance * 1.5f)

        // Pinned memories get a heavy boost
        if (memory.isPinned) {
            baseScore += 5.0f
        }

        // Tag matching
        val memoryTags = memory.tags.split(",")
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }

        val contentLower = memory.content.lowercase(Locale.ROOT)

        for (token in searchTokens) {
            if (token.length < 3) continue

            if (contentLower.contains(token)) {
                baseScore += 3.5f
            }
            if (memoryTags.any { it.contains(token) || token.contains(it) }) {
                baseScore += 4.5f
            }
        }

        // Context alignment check
        if (currentContext != null) {
            val contextLower = "${currentContext.whatHappenedToday} ${currentContext.currentPlans}".lowercase(Locale.ROOT)
            for (tag in memoryTags) {
                if (contextLower.contains(tag)) {
                    baseScore += 1.5f
                }
            }
        }

        return baseScore
    }

    private fun extractKeywords(text: String): Set<String> {
        val stopWords = setOf(
            "the", "a", "an", "is", "are", "was", "were", "and", "or", "to", "in", "on", "at",
            "this", "that", "it", "for", "with", "you", "i", "me", "my", "we", "your", "of",
            "what", "how", "when", "why", "who", "be", "do", "did", "have", "has", "had"
        )
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length > 2 && it !in stopWords }
            .toSet()
    }
}
