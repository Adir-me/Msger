package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memories",
    indices = [
        Index(value = ["personaId"]),
        Index(value = ["category"]),
        Index(value = ["isPinned"])
    ]
)
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long = 1,
    val category: String = "fact", // fact, preference, relationship, event, plan, person, place, conversation, temporary, important
    val content: String,
    val importance: Int = 3, // 1 to 5 scale
    val source: String = "conversation", // conversation, manual, extracted, system
    val confidence: Float = 0.95f,
    val isPinned: Boolean = false,
    val expirationTimestamp: Long? = null, // null for durable, timestamp for temporary TTL
    val tags: String = "", // comma-separated keywords for fast matching
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
