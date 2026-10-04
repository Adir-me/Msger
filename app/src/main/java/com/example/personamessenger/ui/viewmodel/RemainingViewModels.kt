package com.example.personamessenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ConversationEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import com.example.personamessenger.data.local.entity.ScheduledActionEntity
import com.example.personamessenger.data.repository.AudioCallRepository
import com.example.personamessenger.data.repository.MessagingRepository
import com.example.personamessenger.data.repository.SettingsRepository
import com.example.personamessenger.domain.ai.OperationalDebugInfo
import com.example.personamessenger.domain.engine.CallEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    val conversations: StateFlow<List<ConversationEntity>> = messagingRepository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledActions: StateFlow<List<ScheduledActionEntity>> = messagingRepository.scheduledActions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = messagingRepository.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<Long>(1)
    val activeConversationId: StateFlow<Long> = _activeConversationId.asStateFlow()

    val currentConversation: StateFlow<ConversationEntity?> = _activeConversationId
        .flatMapLatest { id -> messagingRepository.getConversationFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id -> messagingRepository.getConversationMessages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isTypingMap: StateFlow<Map<Long, Boolean>> = messagingRepository.isTypingMap
    val lastDebugInfo: StateFlow<OperationalDebugInfo?> = messagingRepository.lastDebugInfo

    fun selectConversation(id: Long) {
        _activeConversationId.value = id
    }

    fun createNewConversation(title: String, platform: String) {
        viewModelScope.launch {
            val newId = messagingRepository.createConversation(title, platform)
            _activeConversationId.value = newId
        }
    }

    fun toggleAiForConversation(conversationId: Long, isActive: Boolean) {
        viewModelScope.launch {
            messagingRepository.toggleAiForConversation(conversationId, isActive)
        }
    }

    fun sendManualMessage(conversationId: Long, text: String) {
        viewModelScope.launch {
            messagingRepository.sendManualMessage(conversationId, text)
        }
    }

    fun scheduleProactiveCheckIn(conversationId: Long, contactId: Long, prompt: String, delayMin: Int) {
        viewModelScope.launch {
            messagingRepository.scheduleProactiveMessage(conversationId, contactId, prompt, delayMin)
        }
    }

    fun cancelScheduledAction(actionId: String) {
        viewModelScope.launch {
            messagingRepository.cancelScheduledAction(actionId)
        }
    }

    fun executeActionNow(action: ScheduledActionEntity) {
        viewModelScope.launch {
            messagingRepository.executePendingActionNow(action)
        }
    }

    fun toggleContactAi(contactId: Long, enabled: Boolean) {
        viewModelScope.launch {
            messagingRepository.toggleAiForContact(contactId, enabled)
        }
    }

    fun updateContact(contact: ContactEntity) {
        viewModelScope.launch {
            messagingRepository.updateContact(contact)
        }
    }
}

class SimulatorViewModel(
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    val lastDebugInfo: StateFlow<OperationalDebugInfo?> = messagingRepository.lastDebugInfo
    val simulatorMessages: StateFlow<List<MessageEntity>> = messagingRepository.getConversationMessages(3)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isTyping: StateFlow<Map<Long, Boolean>> = messagingRepository.isTypingMap

    fun simulateIncomingEvent(
        eventType: String,
        content: String,
        mediaUrl: String = "",
        mediaDesc: String = "",
        transcript: String = "",
        reactionEmoji: String = "",
        durationSeconds: Int = 0
    ) {
        viewModelScope.launch {
            val event = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = 3, // Simulator Conversation
                eventId = UUID.randomUUID().toString(),
                senderId = "simulator_tester",
                senderName = "Test Contact",
                isFromMe = false,
                isAiGenerated = false,
                eventType = eventType,
                contentText = content,
                transcript = transcript,
                mediaUrl = mediaUrl,
                mediaDescription = mediaDesc,
                durationSeconds = durationSeconds,
                reactionEmoji = reactionEmoji,
                deliveryStatus = "delivered",
                timestamp = System.currentTimeMillis()
            )

            messagingRepository.processIncomingEvent(event)
        }
    }
}

class AudioCallViewModel(
    private val audioCallRepository: AudioCallRepository
) : ViewModel() {

    val audioAssets = audioCallRepository.allAudioAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callSessions = audioCallRepository.allCallSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    val currentSession = _activeSessionId
        .flatMapLatest { id ->
            if (id != null) audioCallRepository.getSessionFlow(id) else MutableStateFlow(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _liveCallLog = MutableStateFlow<List<String>>(emptyList())
    val liveCallLog: StateFlow<List<String>> = _liveCallLog.asStateFlow()

    fun startCall(contactId: Long, contactName: String) {
        viewModelScope.launch {
            val session = audioCallRepository.startCallSession(contactId, contactName)
            _activeSessionId.value = session.id
            _liveCallLog.value = listOf("Incoming call simulation started with $contactName")
        }
    }

    fun acceptCall() {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            val res = audioCallRepository.processCallEvent(sid, CallEvent.AcceptCall)
            if (res != null) {
                _liveCallLog.value = _liveCallLog.value + res.logEvent
                if (res.outputSpeechTranscript != null) {
                    _liveCallLog.value = _liveCallLog.value + "AI Audio: \"${res.outputSpeechTranscript}\""
                }
            }
        }
    }

    fun callerSpoke(speechTranscript: String) {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            _liveCallLog.value = _liveCallLog.value + "Caller: \"$speechTranscript\""
            val res = audioCallRepository.processCallEvent(sid, CallEvent.CallerSpoke(speechTranscript))
            if (res != null) {
                _liveCallLog.value = _liveCallLog.value + res.logEvent
                if (res.outputSpeechTranscript != null) {
                    _liveCallLog.value = _liveCallLog.value + "AI Response Audio: \"${res.outputSpeechTranscript}\""
                }
            }
        }
    }

    fun endCall() {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            val res = audioCallRepository.processCallEvent(sid, CallEvent.EndCall)
            if (res != null) {
                _liveCallLog.value = _liveCallLog.value + res.logEvent
            }
            _activeSessionId.value = null
        }
    }

    fun saveAudioAsset(asset: com.example.personamessenger.data.local.entity.AudioAssetEntity) {
        viewModelScope.launch {
            if (asset.id == 0L) {
                audioCallRepository.saveAudio(asset)
            } else {
                audioCallRepository.updateAudio(asset)
            }
        }
    }

    fun deleteAudioAsset(id: Long) {
        viewModelScope.launch {
            audioCallRepository.deleteAudio(id)
        }
    }
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val modelConfig: StateFlow<ModelConfigEntity?> = settingsRepository.primaryConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val auditLogs = settingsRepository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveConfig(config: ModelConfigEntity) {
        viewModelScope.launch {
            settingsRepository.saveConfig(config)
        }
    }

    fun triggerEmergencyStop() {
        viewModelScope.launch {
            settingsRepository.triggerEmergencyStop()
        }
    }

    fun resumeGlobalAutomation() {
        viewModelScope.launch {
            settingsRepository.resumeGlobalAutomation()
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            settingsRepository.clearAuditLogs()
        }
    }
}
