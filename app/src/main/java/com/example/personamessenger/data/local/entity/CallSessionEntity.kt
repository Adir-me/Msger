package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "call_sessions",
    indices = [
        Index(value = ["contactId"]),
        Index(value = ["state"])
    ]
)
data class CallSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contactId: Long,
    val contactName: String,
    val state: String = "IDLE", // IDLE, INCOMING, ACCEPTED, GREETING, LISTENING, PROCESSING, RESPONSE, ENDED
    val activeSpeaker: String = "none", // caller, ai_assistant, none
    val latestTranscript: String = "",
    val transcriptHistoryJson: String = "[]",
    val selectedAudioId: Long? = null,
    val selectedAudioName: String = "",
    val durationSeconds: Int = 0,
    val startedAtEpoch: Long = System.currentTimeMillis(),
    val endedAtEpoch: Long? = null
)
