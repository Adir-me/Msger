package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contexts",
    indices = [
        Index(value = ["personaId"]),
        Index(value = ["isActive"])
    ]
)
data class ContextEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long = 1,
    val title: String = "Today's Live Context",
    val whatHappenedToday: String = "Working on creative project at the local cafe, met with Alex for lunch, feeling productive.",
    val currentPlans: String = "Heading to gym around 6 PM, then dinner with family at 8 PM.",
    val relationshipSituation: String = "Good vibes, planning a weekend road trip.",
    val temporaryEmotionalState: String = "Energetic, cheerful, slightly busy but responsive.",
    val thisWeekEvents: String = "Tech conference on Thursday, movie night on Friday.",
    val immediateThingsToKnow: String = "Phone battery is at 25%, might reply a bit slower later tonight.",
    val isActive: Boolean = true,
    val priorityWeight: Int = 10, // Always higher priority than long-term memories
    val updatedAt: Long = System.currentTimeMillis()
)
