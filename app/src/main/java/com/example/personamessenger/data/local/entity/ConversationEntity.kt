package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["contactId"]),
        Index(value = ["lastMessageTimestamp"])
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val platform: String = "Instagram",
    val title: String,
    val isAiActive: Boolean = true, // Live toggle for pause/resume AI in this chat
    val unreadCount: Int = 0,
    val lastMessageSnippet: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val currentTopic: String = "General chat",
    val unresolvedQuestion: String = "",
    val conversationalMomentum: String = "Active", // Low, Active, Fast, Concluded
    val sentiment: String = "Positive", // Positive, Neutral, Inquiring, Playful, Frustrated
    val isRecipientWaitingForAnswer: Boolean = false,
    val recentMemoryWindowSize: Int = 20, // 10, 20, 30, 50, 100
    val createdAt: Long = System.currentTimeMillis()
)
