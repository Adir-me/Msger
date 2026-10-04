package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["actionCategory"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionCategory: String, // AI_RESPONSE, PROACTIVE_MSG, MANUAL_OVERRIDE, MEMORY_EXTRACT, EMERGENCY_STOP, CALL_SESSION
    val contactName: String,
    val details: String,
    val modelUsed: String = "meta-llama/llama-3.3-70b-instruct:free",
    val tokensUsed: Int = 0,
    val latencyMs: Long = 0L,
    val safetyVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
