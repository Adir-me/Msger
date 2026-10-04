package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.domain.ai.EventClassification
import java.util.Locale

class EventNormalizer {

    /**
     * Classifies and structures incoming events so the AI understands social interactions vs plain text.
     */
    fun classifyEvent(message: MessageEntity): EventClassification {
        val eventType = message.eventType.lowercase(Locale.ROOT)
        val text = message.contentText.trim()

        return when (eventType) {
            "reaction" -> {
                val emoji = message.reactionEmoji.ifBlank { "❤️" }
                EventClassification(
                    eventType = "reaction",
                    isSocialInteraction = true,
                    requiresResponse = false, // Reactions often don't strictly require an immediate text response unless part of active banter
                    emotionalTone = "Appreciation / Agreement",
                    isDirectQuestion = false,
                    contextSummary = "Sender reacted with $emoji to your previous message '${message.replyToMessageId.take(30)}...'"
                )
            }
            "reel" -> {
                val desc = message.mediaDescription.ifBlank { "Shared video reel" }
                EventClassification(
                    eventType = "reel",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Sharing entertaining content",
                    isDirectQuestion = false,
                    contextSummary = "Sender shared an Instagram/social media Reel: '$desc'"
                )
            }
            "voice" -> {
                val transcript = message.transcript.ifBlank { "Voice note without transcript" }
                val isQuestion = transcript.contains("?") || transcript.startsWith("what", ignoreCase = true) || transcript.startsWith("how", ignoreCase = true)
                EventClassification(
                    eventType = "voice",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Spoken audio message",
                    isDirectQuestion = isQuestion,
                    contextSummary = "Sender sent a ${message.durationSeconds}s voice message with transcript: '$transcript'"
                )
            }
            "image" -> {
                val desc = message.mediaDescription.ifBlank { "Shared photo/image" }
                val caption = if (text.isNotBlank()) " with caption: '$text'" else ""
                EventClassification(
                    eventType = "image",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Visual media sharing",
                    isDirectQuestion = text.contains("?"),
                    contextSummary = "Sender shared an image ($desc)$caption"
                )
            }
            "video" -> {
                EventClassification(
                    eventType = "video",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Video media sharing",
                    isDirectQuestion = text.contains("?"),
                    contextSummary = "Sender shared a video: '${message.mediaDescription.ifBlank { text }}'"
                )
            }
            "sticker" -> {
                EventClassification(
                    eventType = "sticker",
                    isSocialInteraction = true,
                    requiresResponse = false,
                    emotionalTone = "Playful sticker",
                    isDirectQuestion = false,
                    contextSummary = "Sender sent a playful sticker: '${message.mediaDescription.ifBlank { "Expressive sticker" }}'"
                )
            }
            "gif" -> {
                EventClassification(
                    eventType = "gif",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Humorous / expressive GIF",
                    isDirectQuestion = false,
                    contextSummary = "Sender shared a GIF: '${message.mediaDescription.ifBlank { "Reaction GIF" }}'"
                )
            }
            "shared_post" -> {
                EventClassification(
                    eventType = "shared_post",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Curated post sharing",
                    isDirectQuestion = false,
                    contextSummary = "Sender shared a post: '${message.mediaDescription.ifBlank { text }}'"
                )
            }
            "link" -> {
                EventClassification(
                    eventType = "link",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Information / link sharing",
                    isDirectQuestion = text.contains("?"),
                    contextSummary = "Sender sent a link preview: '${message.mediaUrl.ifBlank { text }}'"
                )
            }
            "system_event" -> {
                EventClassification(
                    eventType = "system_event",
                    isSocialInteraction = false,
                    requiresResponse = false,
                    emotionalTone = "System notification",
                    isDirectQuestion = false,
                    contextSummary = "System event: '$text'"
                )
            }
            else -> {
                val isQuestion = text.contains("?") ||
                        text.lowercase(Locale.ROOT).startsWith("what") ||
                        text.lowercase(Locale.ROOT).startsWith("where") ||
                        text.lowercase(Locale.ROOT).startsWith("when") ||
                        text.lowercase(Locale.ROOT).startsWith("how") ||
                        text.lowercase(Locale.ROOT).startsWith("why") ||
                        text.lowercase(Locale.ROOT).startsWith("kya") ||
                        text.lowercase(Locale.ROOT).startsWith("kab")

                EventClassification(
                    eventType = "text",
                    isSocialInteraction = true,
                    requiresResponse = true,
                    emotionalTone = "Casual text conversation",
                    isDirectQuestion = isQuestion,
                    contextSummary = "Text message: '$text'"
                )
            }
        }
    }
}
