package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_assets")
data class AudioAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val transcript: String,
    val contextDescription: String,
    val tags: String, // comma-separated e.g. "greeting, casual, excited"
    val durationSeconds: Int = 4,
    val audioFileName: String = "",
    val intendedSituation: String = "Casual greeting when starting conversation",
    val language: String = "English",
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
