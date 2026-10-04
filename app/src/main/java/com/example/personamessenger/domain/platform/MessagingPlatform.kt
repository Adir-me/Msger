package com.example.personamessenger.domain.platform

import com.example.personamessenger.data.local.entity.ConversationEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Portable clean architecture interface for social messaging platforms.
 * Permitted for official/authorized APIs (Instagram permitted webhooks, WhatsApp Cloud API, Telegram Bot API, Simulator).
 */
interface MessagingPlatform {
    val platformName: String

    fun receiveEvents(): Flow<MessageEntity>
    suspend fun sendText(conversationId: Long, text: String, isAiGenerated: Boolean, operationalReason: String): MessageEntity
    suspend fun sendMedia(conversationId: Long, mediaUrl: String, mediaDescription: String, caption: String): MessageEntity
    suspend fun sendAudio(conversationId: Long, audioAssetId: Long, transcript: String): MessageEntity
    suspend fun sendReaction(conversationId: Long, targetMessageId: String, emoji: String): MessageEntity
    suspend fun getConversation(conversationId: Long): ConversationEntity?
    suspend fun getMessage(messageId: String): MessageEntity?
    suspend fun markProcessed(eventId: String)
}
