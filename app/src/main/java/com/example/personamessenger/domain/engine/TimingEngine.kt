package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.PersonaEntity
import kotlin.random.Random

class TimingEngine {

    /**
     * Calculates natural typing and delivery delays for a single or series of messages.
     * Takes into account persona typing speed, character count, and bounded randomization.
     */
    fun calculateDelays(
        messages: List<String>,
        persona: PersonaEntity
    ): List<MessageTiming> {
        val timings = mutableListOf<MessageTiming>()
        val wpm = persona.typingSpeedWpm.coerceIn(40, 140)
        // Average 5 chars per word -> characters per minute = wpm * 5 -> ms per char = 60000 / (wpm * 5)
        val msPerChar = 60000.0 / (wpm * 5.0)

        var cumulativeDelay = 0L

        messages.forEachIndexed { index, text ->
            val charCount = text.length.coerceAtLeast(1)
            // Base thinking delay (short pause before starting to type)
            val baseThinkingMs = if (index == 0) {
                Random.nextLong(persona.minDelayMs, (persona.minDelayMs + 1000L).coerceAtMost(persona.maxDelayMs))
            } else {
                // Shorter pause between consecutive split bursts (500ms - 1500ms)
                Random.nextLong(600L, 1600L)
            }

            // Typing duration simulation
            val typingDuration = (charCount * msPerChar).toLong() + Random.nextLong(-200L, 300L)
            val boundedTypingMs = typingDuration.coerceIn(800L, 8000L)

            val totalStepDelay = baseThinkingMs + boundedTypingMs
            cumulativeDelay += totalStepDelay

            timings.add(
                MessageTiming(
                    text = text,
                    stepDelayMs = totalStepDelay,
                    cumulativeDelayMs = cumulativeDelay,
                    typingDurationMs = boundedTypingMs
                )
            )
        }

        return timings
    }
}

data class MessageTiming(
    val text: String,
    val stepDelayMs: Long,
    val cumulativeDelayMs: Long,
    val typingDurationMs: Long
)
