package com.example.personamessenger.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personamessenger.data.local.AppDatabase
import com.example.personamessenger.data.repository.*
import com.example.personamessenger.domain.ai.EngineAIProvider
import com.example.personamessenger.domain.ai.OpenRouterAIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val applicationScope = CoroutineScope(Dispatchers.IO)
    private val database = AppDatabase.getDatabase(context, applicationScope)

    private val localEngine = EngineAIProvider()
    private val openRouterAiProvider = OpenRouterAIProvider(localEngine)

    private val personaRepo = PersonaRepository(database.personaDao())
    private val memoryRepo = MemoryRepository(database.memoryDao())
    private val contextRepo = ContextRepository(database.contextDao())
    private val settingsRepo = SettingsRepository(database.modelConfigDao(), database.scheduledActionDao(), database.auditLogDao())
    private val audioCallRepo = AudioCallRepository(database.audioAssetDao(), database.callSessionDao())

    private val messagingRepo = MessagingRepository(
        conversationDao = database.conversationDao(),
        messageDao = database.messageDao(),
        contactDao = database.contactDao(),
        personaDao = database.personaDao(),
        memoryDao = database.memoryDao(),
        contextDao = database.contextDao(),
        modelConfigDao = database.modelConfigDao(),
        scheduledActionDao = database.scheduledActionDao(),
        auditLogDao = database.auditLogDao(),
        aiProvider = openRouterAiProvider
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PersonaViewModel::class.java) -> {
                PersonaViewModel(personaRepo) as T
            }
            modelClass.isAssignableFrom(MemoryViewModel::class.java) -> {
                MemoryViewModel(memoryRepo) as T
            }
            modelClass.isAssignableFrom(ContextViewModel::class.java) -> {
                ContextViewModel(contextRepo) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(messagingRepo) as T
            }
            modelClass.isAssignableFrom(SimulatorViewModel::class.java) -> {
                SimulatorViewModel(messagingRepo) as T
            }
            modelClass.isAssignableFrom(AudioCallViewModel::class.java) -> {
                AudioCallViewModel(audioCallRepo) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
