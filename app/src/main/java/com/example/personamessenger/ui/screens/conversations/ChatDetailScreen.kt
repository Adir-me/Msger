package com.example.personamessenger.ui.screens.conversations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.data.local.entity.ConversationEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.domain.ai.OperationalDebugInfo
import com.example.personamessenger.ui.components.DebugPanelDrawer
import com.example.personamessenger.ui.components.EventBubble
import com.example.personamessenger.ui.components.TypingIndicator
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: ConversationEntity?,
    messages: List<MessageEntity>,
    isTyping: Boolean,
    lastDebugInfo: OperationalDebugInfo?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSimulateIncoming: (type: String, content: String, mediaUrl: String, mediaDesc: String, transcript: String, emoji: String, dur: Int) -> Unit,
    onPlayVoice: (String) -> Unit,
    onToggleAi: (Boolean) -> Unit,
    onScheduleProactive: (String, Int) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var showDebugDrawer by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppleGreenDark)
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = conversation?.title ?: "Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (conversation?.isAiActive == true) AppleGreen else LightPurple)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (conversation?.isAiActive == true) "AI Persona Active" else "Manual Takeover",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (conversation != null) {
                                onToggleAi(!conversation.isAiActive)
                            }
                        },
                        modifier = Modifier.testTag("toggle_ai_button")
                    ) {
                        Icon(
                            imageVector = if (conversation?.isAiActive == true) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Toggle AI",
                            tint = if (conversation?.isAiActive == true) AppleGreenDark else LightPurple
                        )
                    }

                    IconButton(onClick = { showDebugDrawer = true }) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Trace",
                            tint = LightPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Quick Incoming Simulation Pills
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            Surface(
                                color = LightPurpleBackground,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LightPurpleSoft),
                                modifier = Modifier.clickable {
                                    onSimulateIncoming("reel", "Look at this cafe!", "", "Aesthetic rooftop siphon coffee cafe", "", "", 0)
                                }
                            ) {
                                Text(
                                    text = "🎬 Send Reel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LightPurpleText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        item {
                            Surface(
                                color = AppleGreenLight,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    onSimulateIncoming("voice", "", "", "", "Hey! Are you free for coffee today?", "", 4)
                                }
                            ) {
                                Text(
                                    text = "🎙️ Send Voice Note",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleGreenDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        item {
                            Surface(
                                color = LightPurpleBackground,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LightPurpleSoft),
                                modifier = Modifier.clickable {
                                    onSimulateIncoming("reaction", "", "", "", "", "❤️", 0)
                                }
                            ) {
                                Text(
                                    text = "❤️ React",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LightPurpleText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        item {
                            Surface(
                                color = AppleGreenLight,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    onSimulateIncoming("text", "what are you doing right now?", "", "", "", "", 0)
                                }
                            ) {
                                Text(
                                    text = "💬 'What are you doing?'",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleGreenDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // iOS iMessage Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("iMessage / Reply as you...", fontSize = 14.sp, color = TextSecondary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AppleBackground,
                                unfocusedContainerColor = AppleBackground,
                                focusedBorderColor = AppleGreen,
                                unfocusedBorderColor = AppleBorder
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSendMessage(textInput.trim())
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(AppleGreen, CircleShape)
                                .testTag("send_manual_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    EventBubble(
                        message = msg,
                        onPlayVoice = onPlayVoice,
                        onInspectDebug = { showDebugDrawer = true }
                    )
                }

                if (isTyping) {
                    item {
                        TypingIndicator(senderName = conversation?.title ?: "Persona")
                    }
                }
            }

            if (showDebugDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    DebugPanelDrawer(
                        debugInfo = lastDebugInfo,
                        onClose = { showDebugDrawer = false }
                    )
                }
            }
        }
    }

    if (showScheduleDialog) {
        var proactivePrompt by remember { mutableStateOf("Check in about coffee plans") }
        var delayMinutes by remember { mutableStateOf(10) }

        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule Proactive Message", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("The AI will follow persona rules and timing before reaching out.")
                    OutlinedTextField(
                        value = proactivePrompt,
                        onValueChange = { proactivePrompt = it },
                        label = { Text("Intent / Context") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("In $delayMinutes mins")
                        Slider(
                            value = delayMinutes.toFloat(),
                            onValueChange = { delayMinutes = it.toInt() },
                            valueRange = 1f..60f,
                            modifier = Modifier.width(160.dp),
                            colors = SliderDefaults.colors(thumbColor = AppleGreen, activeTrackColor = AppleGreen)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onScheduleProactive(proactivePrompt, delayMinutes)
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
