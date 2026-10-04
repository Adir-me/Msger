package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.PersonaEntity
import kotlin.random.Random

class MessageSplittingEngine {

    /**
     * Naturally splits a generated text into 1 to N message bursts if appropriate.
     */
    fun splitResponseIfNeeded(
        rawResponse: String,
        persona: PersonaEntity
    ): List<String> {
        val trimmed = rawResponse.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Check if message is already short (under 40 chars)
        if (trimmed.length < 40) {
            return listOf(trimmed)
        }

        // Check persona splitting probability
        val shouldSplit = Random.nextFloat() < persona.splitProbability
        if (!shouldSplit || persona.maxMessagesPerResponse <= 1) {
            return listOf(trimmed)
        }

        // Try splitting on sentence boundaries, line breaks, or conversational punctuation
        val sentences = trimmed.split(Regex("(?<=[.!?\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (sentences.size in 2..persona.maxMessagesPerResponse) {
            return sentences
        }

        if (sentences.size > persona.maxMessagesPerResponse) {
            // Group sentences into max chunks
            val chunks = mutableListOf<String>()
            val groupSize = (sentences.size + persona.maxMessagesPerResponse - 1) / persona.maxMessagesPerResponse
            sentences.chunked(groupSize).forEach { group ->
                chunks.add(group.joinToString(" "))
            }
            return chunks.take(persona.maxMessagesPerResponse)
        }

        // If it's a single long sentence with commas / conjunctions
        val commaSplit = trimmed.split(Regex("(?<=[,;])\\s+(?=(and|but|also|so|haha|like|then)\\b)", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (commaSplit.size >= 2) {
            return commaSplit.take(persona.maxMessagesPerResponse)
        }

        return listOf(trimmed)
    }
}
