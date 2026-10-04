package com.example.personamessenger.domain.ai

import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.domain.engine.EventNormalizer
import com.example.personamessenger.domain.engine.MessageSplittingEngine
import com.example.personamessenger.domain.engine.TimingEngine
import java.util.Locale

class EngineAIProvider(
    private val eventNormalizer: EventNormalizer = EventNormalizer(),
    private val splittingEngine: MessageSplittingEngine = MessageSplittingEngine(),
    private val timingEngine: TimingEngine = TimingEngine()
) : AIProvider {

    override suspend fun generateResponse(request: AIRequest): AIResponse {
        val startTime = System.currentTimeMillis()
        val classification = eventNormalizer.classifyEvent(request.incomingMessage)

        if (!classification.requiresResponse && request.incomingMessage.eventType == "system_event") {
            return AIResponse(
                shouldRespond = false,
                operationalReason = "System event acknowledged, no response required",
                confidence = 0.98f
            )
        }

        val generatedContent = synthesizePersonaReply(request, classification)
        val splitMessages = splittingEngine.splitResponseIfNeeded(generatedContent.rawText, request.persona)
        val messageTimings = timingEngine.calculateDelays(splitMessages, request.persona)

        val scheduledMessages = messageTimings.map { timing ->
            ScheduledMessage(
                text = timing.text,
                delayMs = timing.stepDelayMs,
                typingDurationMs = timing.typingDurationMs
            )
        }

        val extractedMemories = extractPotentialMemories(request)
        val latency = System.currentTimeMillis() - startTime

        val debugInfo = OperationalDebugInfo(
            eventType = classification.eventType,
            normalizedEvent = classification.contextSummary,
            selectedModel = if (request.modelConfig.isManualModelActive && request.modelConfig.manualModelId.isNotBlank())
                request.modelConfig.manualModelId else request.modelConfig.defaultModel,
            retrievedMemoriesCount = request.relevantMemories.size,
            retrievedMemoriesSummary = request.relevantMemories.map { "[${it.category}] ${it.content}" },
            contextPriorityState = request.currentContext?.title ?: "No active context",
            responseDecision = generatedContent.operationalReason,
            generatedMessages = splitMessages,
            timingDelays = messageTimings.map { it.stepDelayMs },
            memoryUpdates = extractedMemories.map { it.content },
            latencyMs = latency,
            validationPassed = true
        )

        return AIResponse(
            shouldRespond = true,
            messages = scheduledMessages,
            confidence = 0.94f,
            operationalReason = generatedContent.operationalReason,
            extractedMemories = extractedMemories,
            newTopic = generatedContent.suggestedTopic,
            newSentiment = generatedContent.sentiment,
            newMomentum = "Active",
            isRecipientWaitingForAnswer = false,
            debugInfo = debugInfo
        )
    }

    override suspend fun generateMultipleMessages(request: AIRequest): List<ScheduledMessage> {
        val response = generateResponse(request)
        return response.messages
    }

    override suspend fun analyzeIncomingEvent(message: MessageEntity): EventClassification {
        return eventNormalizer.classifyEvent(message)
    }

    override suspend fun summarizeConversation(messages: List<MessageEntity>): String {
        if (messages.isEmpty()) return "No conversation history."
        val recentTopics = messages.takeLast(5).map { "${it.senderName}: ${it.contentText.ifBlank { it.eventType }}" }
        return recentTopics.joinToString(" | ")
    }

    override suspend fun generateProactiveMessage(
        contact: ContactEntity,
        persona: PersonaEntity,
        context: ContextEntity?,
        memories: List<MemoryEntity>
    ): ProactiveProposal {
        val topMemory = memories.maxByOrNull { it.importance }
        val expression = persona.favoriteExpressions.split(",").firstOrNull()?.trim() ?: "Hey"

        val messageText = when {
            context != null && context.whatHappenedToday.isNotBlank() -> {
                "$expression ${contact.name}! ${context.whatHappenedToday.take(60)}. How are things going with you?"
            }
            topMemory != null -> {
                "$expression ${contact.name}! Was just thinking about ${topMemory.content.take(50)}. How are you?"
            }
            persona.languageStyle.equals("Hinglish", ignoreCase = true) -> {
                "Hey ${contact.name}! Kya scene hai? Catch up karte hai."
            }
            else -> {
                "Hey ${contact.name}! Hope you're having a good day, let's catch up soon."
            }
        }

        return ProactiveProposal(
            shouldSend = true,
            proposedText = messageText,
            delayMinutes = 15,
            reason = "Contextual follow-up generated from active persona state"
        )
    }

    override suspend fun analyzeVoiceContext(
        transcript: String,
        durationSeconds: Int,
        availableAudios: List<AudioAssetEntity>
    ): VoiceInterpretation {
        val lower = transcript.lowercase(Locale.ROOT)
        val tone = if (lower.contains("?")) "Inquiring" else "Casual Spoken"

        val matchedAudio = availableAudios.firstOrNull { audio ->
            audio.tags.split(",").any { tag -> lower.contains(tag.trim().lowercase(Locale.ROOT)) }
        } ?: availableAudios.firstOrNull()

        return VoiceInterpretation(
            transcript = transcript,
            durationSeconds = durationSeconds,
            toneHint = tone,
            suggestedAudioReplyId = matchedAudio?.id
        )
    }

    private data class SynthesisResult(
        val rawText: String,
        val operationalReason: String,
        val suggestedTopic: String,
        val sentiment: String
    )

    private fun synthesizePersonaReply(
        request: AIRequest,
        classification: EventClassification
    ): SynthesisResult {
        val persona = request.persona
        val context = request.currentContext
        val memories = request.relevantMemories
        val msg = request.incomingMessage
        val text = msg.contentText.lowercase(Locale.ROOT)
        val isHinglish = persona.languageStyle.equals("Hinglish", ignoreCase = true)
        val preferredEmoji = persona.preferredEmojis.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val sampleEmoji = if (persona.emojiFrequency != "none" && preferredEmoji.isNotEmpty()) {
            preferredEmoji.random()
        } else ""

        val catchphrase = persona.favoriteExpressions.split(",").map { it.trim() }.filter { it.isNotBlank() }.firstOrNull()
            ?: if (isHinglish) "sahi hai" else "sounds good"

        when (classification.eventType) {
            "reaction" -> {
                val emoji = msg.reactionEmoji.ifBlank { "❤️" }
                val reply = if (isHinglish) "haha $sampleEmoji" else "haha $sampleEmoji"
                return SynthesisResult(
                    rawText = reply,
                    operationalReason = "Acknowledged reaction ($emoji)",
                    suggestedTopic = "Reaction",
                    sentiment = "Playful"
                )
            }

            "reel" -> {
                val desc = msg.mediaDescription.ifBlank { "shared video" }
                val reply = if (isHinglish) {
                    "bhai this is crazy $sampleEmoji $catchphrase"
                } else {
                    "wait this is so good $sampleEmoji $catchphrase"
                }
                return SynthesisResult(
                    rawText = reply,
                    operationalReason = "Reacted to shared reel: '$desc'",
                    suggestedTopic = "Reel",
                    sentiment = "Engaged"
                )
            }

            "voice" -> {
                val transcript = msg.transcript.ifBlank { "voice note" }
                val reply = if (isHinglish) {
                    "sun liya! $sampleEmoji $catchphrase"
                } else {
                    "just listened! $sampleEmoji $catchphrase"
                }
                return SynthesisResult(
                    rawText = reply,
                    operationalReason = "Responded to voice message",
                    suggestedTopic = "Voice Response",
                    sentiment = "Attentive"
                )
            }

            "image" -> {
                val reply = if (isHinglish) {
                    "looks great $sampleEmoji"
                } else {
                    "looks great $sampleEmoji"
                }
                return SynthesisResult(
                    rawText = reply,
                    operationalReason = "Acknowledged shared photo",
                    suggestedTopic = "Photo",
                    sentiment = "Positive"
                )
            }

            "sticker", "gif" -> {
                val reply = "haha $sampleEmoji"
                return SynthesisResult(
                    rawText = reply,
                    operationalReason = "Playful response to sticker/gif",
                    suggestedTopic = "Banter",
                    sentiment = "Playful"
                )
            }

            else -> {
                // If text aligns with Current Context
                if (context != null && (text.contains("doing") || text.contains("today") || text.contains("plans") || text.contains("kya"))) {
                    val whatDoing = if (context.whatHappenedToday.isNotBlank()) context.whatHappenedToday else context.currentPlans
                    val reply = if (whatDoing.isNotBlank()) {
                        "$whatDoing $sampleEmoji"
                    } else {
                        "$catchphrase $sampleEmoji"
                    }
                    return SynthesisResult(
                        rawText = reply,
                        operationalReason = "Answered using live context",
                        suggestedTopic = "Current Context",
                        sentiment = "Engaging"
                    )
                }

                // If text matches any retrieved memory
                val matchingMemory = memories.firstOrNull { mem ->
                    mem.tags.split(",").any { tag -> tag.isNotBlank() && text.contains(tag.trim().lowercase(Locale.ROOT)) }
                }

                if (matchingMemory != null) {
                    val reply = "${matchingMemory.content} $sampleEmoji"
                    return SynthesisResult(
                        rawText = reply,
                        operationalReason = "Referenced relevant memory: ${matchingMemory.category}",
                        suggestedTopic = matchingMemory.category,
                        sentiment = "Thoughtful"
                    )
                }

                // Fallback to Persona tone
                val fallbackReply = "$catchphrase $sampleEmoji"
                return SynthesisResult(
                    rawText = fallbackReply,
                    operationalReason = "Generated response aligned with persona tone and vocabulary",
                    suggestedTopic = "Conversation",
                    sentiment = "Friendly"
                )
            }
        }
    }

    private fun extractPotentialMemories(request: AIRequest): List<MemoryToSave> {
        val msg = request.incomingMessage
        val text = msg.contentText.lowercase(Locale.ROOT)
        val memories = mutableListOf<MemoryToSave>()

        if (text.contains("i like") || text.contains("i love") || text.contains("my favorite")) {
            memories.add(
                MemoryToSave(
                    category = "preference",
                    content = "${request.contact.name} mentioned: '${msg.contentText.take(80)}'",
                    importance = 3,
                    tags = "preference, ${request.contact.username}"
                )
            )
        }

        return memories
    }
}
