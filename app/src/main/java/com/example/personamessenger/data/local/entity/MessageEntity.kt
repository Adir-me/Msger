package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"]),
        Index(value = ["eventId"], unique = false)
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: Long,
    val eventId: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val isFromMe: Boolean,
    val isAiGenerated: Boolean = false,
    val isManualOverride: Boolean = false,
    val eventType: String = "text", // text, image, video, reel, voice, sticker, gif, reaction, shared_post, link, system_event, unknown
    val contentText: String = "",
    val transcript: String = "",
    val mediaUrl: String = "",
    val mediaDescription: String = "",
    val durationSeconds: Int = 0,
    val reactionEmoji: String = "",
    val replyToMessageId: String = "",
    val deliveryStatus: String = "delivered", // sending, sent, delivered, read, scheduled, failed
    val scheduledSendTimeEpoch: Long? = null,
    val operationalReason: String = "",
    val confidence: Float = 1.0f,
    val memoryReferencesJson: String = "[]",
    val isDisclosureShown: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
