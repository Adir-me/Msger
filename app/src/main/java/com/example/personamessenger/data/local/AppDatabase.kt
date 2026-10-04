package com.example.personamessenger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.personamessenger.data.local.dao.AudioAssetDao
import com.example.personamessenger.data.local.dao.AuditLogDao
import com.example.personamessenger.data.local.dao.CallSessionDao
import com.example.personamessenger.data.local.dao.ContactDao
import com.example.personamessenger.data.local.dao.ContextDao
import com.example.personamessenger.data.local.dao.ConversationDao
import com.example.personamessenger.data.local.dao.MemoryDao
import com.example.personamessenger.data.local.dao.MessageDao
import com.example.personamessenger.data.local.dao.ModelConfigDao
import com.example.personamessenger.data.local.dao.PersonaDao
import com.example.personamessenger.data.local.dao.ScheduledActionDao
import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.AuditLogEntity
import com.example.personamessenger.data.local.entity.CallSessionEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.ConversationEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.data.local.entity.ScheduledActionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonaEntity::class,
        MemoryEntity::class,
        ContextEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ModelConfigEntity::class,
        ScheduledActionEntity::class,
        AudioAssetEntity::class,
        CallSessionEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun memoryDao(): MemoryDao
    abstract fun contextDao(): ContextDao
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun scheduledActionDao(): ScheduledActionDao
    abstract fun audioAssetDao(): AudioAssetDao
    abstract fun callSessionDao(): CallSessionDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "persona_messenger_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        initializeEmptyDatabase(database)
                    }
                }
            }
        }

        private suspend fun initializeEmptyDatabase(db: AppDatabase) {
            // Only initialize basic operational config without any example personas, memories, voices, or messages
            val initialModelConfig = ModelConfigEntity(
                id = "primary_config",
                openRouterApiKey = "",
                defaultModel = "meta-llama/llama-3.3-70b-instruct:free",
                fallbackModel = "google/gemini-2.0-flash-exp:free",
                manualModelId = "",
                isManualModelActive = false,
                temperature = 0.75f,
                maxTokens = 800,
                contextWindowTokens = 8000,
                thinkingBudgetTokens = 0,
                isGlobalAutomationActive = true,
                isAiDisclosureEnabled = true,
                disclosureFormat = "badge"
            )
            db.modelConfigDao().saveConfig(initialModelConfig)

            val initialAudit = AuditLogEntity(
                actionCategory = "SYSTEM_INIT",
                contactName = "System",
                details = "Persona Messenger database initialized.",
                modelUsed = "meta-llama/llama-3.3-70b-instruct:free",
                tokensUsed = 0,
                latencyMs = 10L,
                safetyVerified = true
            )
            db.auditLogDao().insertLog(initialAudit)
        }
    }
}
