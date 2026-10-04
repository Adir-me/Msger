package com.example.personamessenger.domain.ai

import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.PersonaEntity

interface AIProvider {
    suspend fun generateResponse(request: AIRequest): AIResponse
    suspend fun generateMultipleMessages(request: AIRequest): List<ScheduledMessage>
    suspend fun analyzeIncomingEvent(message: MessageEntity): EventClassification
    suspend fun summarizeConversation(messages: List<MessageEntity>): String
    suspend fun generateProactiveMessage(
        contact: ContactEntity,
        persona: PersonaEntity,
        context: ContextEntity?,
        memories: List<MemoryEntity>
    ): ProactiveProposal
    suspend fun analyzeVoiceContext(
        transcript: String,
        durationSeconds: Int,
        availableAudios: List<AudioAssetEntity>
    ): VoiceInterpretation
}
