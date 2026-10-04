package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scheduled_actions",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["contactId"]),
        Index(value = ["status"])
    ]
)
data class ScheduledActionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: Long,
    val contactId: Long,
    val actionType: String = "follow_up", // follow_up, check_in, context_reminder, scheduled_post, multi_burst
    val promptObjective: String,
    val proposedText: String,
    val targetSendTimeEpoch: Long,
    val status: String = "pending", // pending, executing, executed, cancelled, failed
    val cancelReason: String = "",
    val idempotencyKey: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)
