package com.example.personamessenger.data.repository

import com.example.personamessenger.data.local.dao.AudioAssetDao
import com.example.personamessenger.data.local.dao.CallSessionDao
import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.CallSessionEntity
import com.example.personamessenger.domain.engine.CallEvent
import com.example.personamessenger.domain.engine.CallStateMachine
import kotlinx.coroutines.flow.Flow

class AudioCallRepository(
    private val audioAssetDao: AudioAssetDao,
    private val callSessionDao: CallSessionDao,
    private val callStateMachine: CallStateMachine = CallStateMachine()
) {
    val allAudioAssets: Flow<List<AudioAssetEntity>> = audioAssetDao.getAllAudioAssets()
    val allCallSessions: Flow<List<CallSessionEntity>> = callSessionDao.getAllCallSessions()

    fun getSessionFlow(sessionId: String): Flow<CallSessionEntity?> = callSessionDao.getSessionFlow(sessionId)

    suspend fun saveAudio(audio: AudioAssetEntity): Long = audioAssetDao.insertAudio(audio)
    suspend fun updateAudio(audio: AudioAssetEntity) = audioAssetDao.updateAudio(audio)
    suspend fun deleteAudio(id: Long) = audioAssetDao.deleteAudio(id)

    suspend fun startCallSession(contactId: Long, contactName: String): CallSessionEntity {
        val session = CallSessionEntity(
            contactId = contactId,
            contactName = contactName,
            state = "INCOMING"
        )
        callSessionDao.insertSession(session)
        return session
    }

    suspend fun processCallEvent(
        sessionId: String,
        event: CallEvent
    ): CallStateMachine.TransitionResult? {
        val session = callSessionDao.getSessionById(sessionId) ?: return null
        val audios = audioAssetDao.findMatchingAudios("") // gets all
        val result = callStateMachine.processEvent(session, event, audios)

        callSessionDao.updateSession(result.newSession)
        if (result.selectedAudio != null) {
            audioAssetDao.incrementUsage(result.selectedAudio.id)
        }
        return result
    }
}
