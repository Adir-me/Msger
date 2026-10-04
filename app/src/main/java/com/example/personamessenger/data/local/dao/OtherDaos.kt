package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.AuditLogEntity
import com.example.personamessenger.data.local.entity.CallSessionEntity
import com.example.personamessenger.data.local.entity.ScheduledActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledActionDao {
    @Query("SELECT * FROM scheduled_actions ORDER BY targetSendTimeEpoch ASC")
    fun getAllScheduledActions(): Flow<List<ScheduledActionEntity>>

    @Query("SELECT * FROM scheduled_actions WHERE status = 'pending' AND targetSendTimeEpoch <= :now")
    suspend fun getDueScheduledActions(now: Long = System.currentTimeMillis()): List<ScheduledActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ScheduledActionEntity)

    @Update
    suspend fun updateAction(action: ScheduledActionEntity)

    @Query("UPDATE scheduled_actions SET status = 'cancelled', cancelReason = :reason WHERE id = :id")
    suspend fun cancelAction(id: String, reason: String = "Manual human override")

    @Query("UPDATE scheduled_actions SET status = 'cancelled', cancelReason = 'Emergency Stop' WHERE status = 'pending'")
    suspend fun cancelAllPending()
}

@Dao
interface AudioAssetDao {
    @Query("SELECT * FROM audio_assets ORDER BY usageCount DESC, createdAt DESC")
    fun getAllAudioAssets(): Flow<List<AudioAssetEntity>>

    @Query("SELECT * FROM audio_assets WHERE tags LIKE '%' || :tag || '%' OR intendedSituation LIKE '%' || :tag || '%'")
    suspend fun findMatchingAudios(tag: String): List<AudioAssetEntity>

    @Query("SELECT * FROM audio_assets WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: Long): AudioAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: AudioAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAudios(audios: List<AudioAssetEntity>)

    @Update
    suspend fun updateAudio(audio: AudioAssetEntity)

    @Query("UPDATE audio_assets SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM audio_assets WHERE id = :id")
    suspend fun deleteAudio(id: Long)
}

@Dao
interface CallSessionDao {
    @Query("SELECT * FROM call_sessions ORDER BY startedAtEpoch DESC")
    fun getAllCallSessions(): Flow<List<CallSessionEntity>>

    @Query("SELECT * FROM call_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): CallSessionEntity?

    @Query("SELECT * FROM call_sessions WHERE id = :id LIMIT 1")
    fun getSessionFlow(id: String): Flow<CallSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CallSessionEntity)

    @Update
    suspend fun updateSession(session: CallSessionEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}
