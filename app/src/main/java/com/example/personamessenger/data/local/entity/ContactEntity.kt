package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["isAiEnabled"])
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val platform: String = "Instagram", // Instagram, WhatsApp, Telegram, Simulator
    val avatarEmoji: String = "👤",
    val avatarUrl: String = "",
    val isAiEnabled: Boolean = true, // Contact-level authorization switch
    val assignedPersonaId: Long = 1,
    val assignedModelId: String = "default",
    val customContextNotes: String = "Close friend from college, talks about coding and music",
    val quietHoursEnabled: Boolean = true,
    val quietHoursStartHour: Int = 23, // 11 PM
    val quietHoursEndHour: Int = 8,    // 8 AM
    val maxMessagesPerHour: Int = 15,
    val proactiveMessagingAllowed: Boolean = true,
    val voiceHandlingEnabled: Boolean = true,
    val splitMessageOverride: Boolean = true,
    val responseStyleOverride: String = "default", // default, concise, extra_casual, formal
    val isAuthorizedAccount: Boolean = true, // explicit user consent compliance
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
