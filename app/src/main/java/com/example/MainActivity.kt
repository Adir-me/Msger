package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personamessenger.domain.audio.SpeechAudioService
import com.example.personamessenger.ui.components.EmergencyStopBar
import com.example.personamessenger.ui.screens.audiocall.AudioCallScreen
import com.example.personamessenger.ui.screens.conversations.ChatDetailScreen
import com.example.personamessenger.ui.screens.conversations.ConversationsScreen
import com.example.personamessenger.ui.screens.memory.MemoryScreen
import com.example.personamessenger.ui.screens.persona.PersonaScreen
import com.example.personamessenger.ui.screens.settings.ModelsSettingsScreen
import com.example.personamessenger.ui.theme.*
import com.example.personamessenger.ui.viewmodel.*

enum class AppleNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHATS("Chats", Icons.Default.ChatBubble),
    PERSONA("Persona", Icons.Default.Person),
    MEMORY("Memory", Icons.Default.Storage),
    CALLS("Voice", Icons.Default.GraphicEq),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var speechAudioService: SpeechAudioService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechAudioService = SpeechAudioService(applicationContext)
        val factory = AppViewModelFactory(applicationContext)

        setContent {
            val personaViewModel: PersonaViewModel by viewModels { factory }
            val memoryViewModel: MemoryViewModel by viewModels { factory }
            val contextViewModel: ContextViewModel by viewModels { factory }
            val chatViewModel: ChatViewModel by viewModels { factory }
            val simulatorViewModel: SimulatorViewModel by viewModels { factory }
            val audioCallViewModel: AudioCallViewModel by viewModels { factory }
            val settingsViewModel: SettingsViewModel by viewModels { factory }

            MyApplicationTheme {
                ApplePersonaMessengerApp(
                    personaViewModel = personaViewModel,
                    memoryViewModel = memoryViewModel,
                    contextViewModel = contextViewModel,
                    chatViewModel = chatViewModel,
                    simulatorViewModel = simulatorViewModel,
                    audioCallViewModel = audioCallViewModel,
                    settingsViewModel = settingsViewModel,
                    speechAudioService = speechAudioService
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechAudioService.shutdown()
    }
}

@Composable
fun ApplePersonaMessengerApp(
    personaViewModel: PersonaViewModel,
    memoryViewModel: MemoryViewModel,
    contextViewModel: ContextViewModel,
    chatViewModel: ChatViewModel,
    simulatorViewModel: SimulatorViewModel,
    audioCallViewModel: AudioCallViewModel,
    settingsViewModel: SettingsViewModel,
    speechAudioService: SpeechAudioService
) {
    var currentTab by remember { mutableStateOf(AppleNavTab.CHATS) }
    var inChatDetail by remember { mutableStateOf(false) }

    val conversations by chatViewModel.conversations.collectAsStateWithLifecycle()
    val activeConversation by chatViewModel.currentConversation.collectAsStateWithLifecycle()
    val currentMessages by chatViewModel.currentMessages.collectAsStateWithLifecycle()
    val isTypingMap by chatViewModel.isTypingMap.collectAsStateWithLifecycle()
    val lastDebugInfo by chatViewModel.lastDebugInfo.collectAsStateWithLifecycle()

    val personas by personaViewModel.allPersonas.collectAsStateWithLifecycle()
    val currentPersona by personaViewModel.currentPersona.collectAsStateWithLifecycle()

    val memories by memoryViewModel.memories.collectAsStateWithLifecycle()
    val selectedMemoryCategory by memoryViewModel.selectedCategory.collectAsStateWithLifecycle()
    val memorySearchQuery by memoryViewModel.searchQuery.collectAsStateWithLifecycle()

    val activeContext by contextViewModel.activeContext.collectAsStateWithLifecycle()

    val audioAssets by audioCallViewModel.audioAssets.collectAsStateWithLifecycle()
    val currentCallSession by audioCallViewModel.currentSession.collectAsStateWithLifecycle()
    val liveCallLogs by audioCallViewModel.liveCallLog.collectAsStateWithLifecycle()

    val modelConfig by settingsViewModel.modelConfig.collectAsStateWithLifecycle()
    val auditLogs by settingsViewModel.auditLogs.collectAsStateWithLifecycle()

    val isGlobalAutomationActive = modelConfig?.isGlobalAutomationActive ?: true

    BackHandler(enabled = inChatDetail) {
        inChatDetail = false
    }

    Scaffold(
        containerColor = AppleBackground,
        bottomBar = {
            if (!inChatDetail) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("apple_bottom_nav")
                ) {
                    val tabs = listOf(
                        AppleNavTab.CHATS,
                        AppleNavTab.PERSONA,
                        AppleNavTab.MEMORY,
                        AppleNavTab.CALLS,
                        AppleNavTab.SETTINGS
                    )
                    tabs.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) AppleGreen else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AppleGreenDark else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = AppleGreenLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Emergency Banner
            EmergencyStopBar(
                isAutomationActive = isGlobalAutomationActive,
                onEmergencyStop = { settingsViewModel.triggerEmergencyStop() },
                onResumeAutomation = { settingsViewModel.resumeGlobalAutomation() }
            )

            Box(modifier = Modifier.weight(1f)) {
                if (inChatDetail) {
                    val isChatTyping = isTypingMap[activeConversation?.id ?: 0L] ?: false
                    ChatDetailScreen(
                        conversation = activeConversation,
                        messages = currentMessages,
                        isTyping = isChatTyping,
                        lastDebugInfo = lastDebugInfo,
                        onBack = { inChatDetail = false },
                        onSendMessage = { text ->
                            activeConversation?.let {
                                chatViewModel.sendManualMessage(it.id, text)
                            }
                        },
                        onSimulateIncoming = { type, content, url, desc, transcript, emoji, dur ->
                            simulatorViewModel.simulateIncomingEvent(
                                eventType = type,
                                content = content,
                                mediaUrl = url,
                                mediaDesc = desc,
                                transcript = transcript,
                                reactionEmoji = emoji,
                                durationSeconds = dur
                            )
                        },
                        onPlayVoice = { text ->
                            speechAudioService.speak(text)
                        },
                        onToggleAi = { isActive ->
                            activeConversation?.let {
                                chatViewModel.toggleAiForConversation(it.id, isActive)
                            }
                        },
                        onScheduleProactive = { prompt, delayMin ->
                            activeConversation?.let {
                                chatViewModel.scheduleProactiveCheckIn(it.id, it.contactId, prompt, delayMin)
                            }
                        }
                    )
                } else {
                    Crossfade(targetState = currentTab, label = "apple_tab_transition") { tab ->
                        when (tab) {
                            AppleNavTab.CHATS -> {
                                ConversationsScreen(
                                    conversations = conversations,
                                    isGlobalAutomationActive = isGlobalAutomationActive,
                                    onSelectConversation = { id ->
                                        chatViewModel.selectConversation(id)
                                        inChatDetail = true
                                    },
                                    onEmergencyStop = {
                                        if (isGlobalAutomationActive) {
                                            settingsViewModel.triggerEmergencyStop()
                                        } else {
                                            settingsViewModel.resumeGlobalAutomation()
                                        }
                                    },
                                    onCreateNewChat = { title, platform ->
                                        chatViewModel.createNewConversation(title, platform)
                                        inChatDetail = true
                                    }
                                )
                            }

                            AppleNavTab.PERSONA -> {
                                PersonaScreen(
                                    personas = personas,
                                    selectedPersona = currentPersona,
                                    activeContext = activeContext,
                                    onSelectPersona = { personaViewModel.selectPersona(it) },
                                    onSavePersona = { personaViewModel.savePersona(it) },
                                    onSaveContext = { contextViewModel.saveContext(it) }
                                )
                            }

                            AppleNavTab.MEMORY -> {
                                MemoryScreen(
                                    memories = memories,
                                    selectedCategory = selectedMemoryCategory,
                                    searchQuery = memorySearchQuery,
                                    onSelectCategory = { memoryViewModel.setCategory(it) },
                                    onSearchQueryChange = { memoryViewModel.setSearchQuery(it) },
                                    onSaveMemory = { memoryViewModel.saveMemory(it) },
                                    onTogglePin = { memoryViewModel.togglePin(it) },
                                    onDeleteMemory = { memoryViewModel.deleteMemory(it) }
                                )
                            }

                            AppleNavTab.CALLS -> {
                                val callerName = activeConversation?.title ?: currentPersona?.name ?: "Contact"
                                AudioCallScreen(
                                    audioAssets = audioAssets,
                                    currentSession = currentCallSession,
                                    liveCallLogs = liveCallLogs,
                                    onStartCall = {
                                        audioCallViewModel.startCall(activeConversation?.contactId ?: 1L, callerName)
                                    },
                                    onAcceptCall = {
                                        audioCallViewModel.acceptCall()
                                        val greeting = audioAssets.firstOrNull { it.tags.contains("greeting") }?.transcript
                                            ?: currentPersona?.favoriteExpressions?.split(",")?.firstOrNull()
                                            ?: "Hello, how are you?"
                                        speechAudioService.speak(greeting)
                                    },
                                    onCallerSpoke = { text ->
                                        audioCallViewModel.callerSpoke(text)
                                        val matchedAudio = audioAssets.firstOrNull { asset ->
                                            asset.tags.split(",").any { tag -> text.contains(tag.trim(), ignoreCase = true) }
                                        }
                                        val replyText = matchedAudio?.transcript
                                            ?: currentPersona?.favoriteExpressions?.split(",")?.firstOrNull()
                                            ?: "Sounds good!"
                                        speechAudioService.speak(replyText)
                                    },
                                    onEndCall = {
                                        audioCallViewModel.endCall()
                                        speechAudioService.stop()
                                    },
                                    onPlayAudio = { text ->
                                        speechAudioService.speak(text)
                                    },
                                    onSaveAudio = { audioCallViewModel.saveAudioAsset(it) },
                                    onDeleteAudio = { audioCallViewModel.deleteAudioAsset(it) }
                                )
                            }

                            AppleNavTab.SETTINGS -> {
                                ModelsSettingsScreen(
                                    modelConfig = modelConfig,
                                    auditLogs = auditLogs,
                                    onSaveConfig = { settingsViewModel.saveConfig(it) },
                                    onEmergencyStop = { settingsViewModel.triggerEmergencyStop() },
                                    onResumeAutomation = { settingsViewModel.resumeGlobalAutomation() },
                                    onClearLogs = { settingsViewModel.clearAuditLogs() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
