package com.example.personamessenger.data.repository

import com.example.personamessenger.data.local.dao.AuditLogDao
import com.example.personamessenger.data.local.dao.ModelConfigDao
import com.example.personamessenger.data.local.dao.ScheduledActionDao
import com.example.personamessenger.data.local.entity.AuditLogEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val modelConfigDao: ModelConfigDao,
    private val scheduledActionDao: ScheduledActionDao,
    private val auditLogDao: AuditLogDao
) {
    val primaryConfigFlow: Flow<ModelConfigEntity?> = modelConfigDao.getPrimaryConfigFlow()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

    suspend fun getPrimaryConfig(): ModelConfigEntity {
        return modelConfigDao.getPrimaryConfig() ?: ModelConfigEntity()
    }

    suspend fun saveConfig(config: ModelConfigEntity) {
        modelConfigDao.saveConfig(config)
        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "SETTINGS_UPDATE",
                contactName = "System",
                details = "AI Model configuration updated. Default model: ${config.defaultModel}",
                safetyVerified = true
            )
        )
    }

    /**
     * Emergency Master Stop: immediately pauses all automation and cancels pending scheduled actions.
     */
    suspend fun triggerEmergencyStop() {
        modelConfigDao.setGlobalAutomationState(false)
        scheduledActionDao.cancelAllPending()
        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "EMERGENCY_STOP",
                contactName = "GLOBAL",
                details = "EMERGENCY MASTER STOP TRIGGERED: All automations paused, scheduled messages cancelled.",
                safetyVerified = true
            )
        )
    }

    suspend fun resumeGlobalAutomation() {
        modelConfigDao.setGlobalAutomationState(true)
        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "AUTOMATION_RESUMED",
                contactName = "GLOBAL",
                details = "Global automation master switch re-enabled.",
                safetyVerified = true
            )
        )
    }

    suspend fun clearAuditLogs() {
        auditLogDao.clearLogs()
    }
}
