package com.example.personamessenger.domain.ai

import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import com.example.personamessenger.data.local.entity.PersonaEntity

data class AIRequest(
    val persona: PersonaEntity,
    val relevantMemories: List<MemoryEntity>,
    val currentContext: ContextEntity?,
    val recentHistory: List<MessageEntity>,
    val incomingMessage: MessageEntity,
    val contact: ContactEntity,
    val modelConfig: ModelConfigEntity,
    val availableAudios: List<AudioAssetEntity> = emptyList()
)

data class ScheduledMessage(
    val text: String,
    val delayMs: Long = 1800L,
    val typingDurationMs: Long = 1200L,
    val audioAssetId: Long? = null
)

data class MemoryToSave(
    val category: String = "fact", // fact, preference, relationship, event, plan, temporary
    val content: String,
    val importance: Int = 3,
    val tags: String = "",
    val isTemporary: Boolean = false,
    val ttlMinutes: Long? = null
)

data class AIResponse(
    val shouldRespond: Boolean = true,
    val messages: List<ScheduledMessage> = emptyList(),
    val confidence: Float = 0.92f,
    val operationalReason: String = "Natural conversational reply",
    val extractedMemories: List<MemoryToSave> = emptyList(),
    val newTopic: String? = null,
    val newSentiment: String? = null,
    val newMomentum: String? = null,
    val isRecipientWaitingForAnswer: Boolean = false,
    val debugInfo: OperationalDebugInfo? = null
)

data class EventClassification(
    val eventType: String,
    val isSocialInteraction: Boolean,
    val requiresResponse: Boolean,
    val emotionalTone: String,
    val isDirectQuestion: Boolean,
    val contextSummary: String
)

data class ProactiveProposal(
    val shouldSend: Boolean,
    val proposedText: String,
    val delayMinutes: Int = 15,
    val reason: String = "Conversation follow-up"
)

data class VoiceInterpretation(
    val transcript: String,
    val durationSeconds: Int,
    val toneHint: String,
    val suggestedAudioReplyId: Long? = null
)

data class OperationalDebugInfo(
    val eventType: String,
    val normalizedEvent: String,
    val selectedModel: String,
    val retrievedMemoriesCount: Int,
    val retrievedMemoriesSummary: List<String>,
    val contextPriorityState: String,
    val responseDecision: String,
    val generatedMessages: List<String>,
    val timingDelays: List<Long>,
    val memoryUpdates: List<String>,
    val latencyMs: Long,
    val validationPassed: Boolean
)
