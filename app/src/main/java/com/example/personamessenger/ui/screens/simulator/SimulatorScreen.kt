package com.example.personamessenger.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.domain.ai.OperationalDebugInfo
import com.example.personamessenger.ui.components.DebugPanelDrawer
import com.example.personamessenger.ui.components.EventBubble
import com.example.personamessenger.ui.components.TypingIndicator
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    messages: List<MessageEntity>,
    isTyping: Boolean,
    lastDebugInfo: OperationalDebugInfo?,
    onSimulateEvent: (eventType: String, content: String, mediaUrl: String, mediaDesc: String, transcript: String, reactionEmoji: String, duration: Int) -> Unit
) {
    var customText by remember { mutableStateOf("") }
    var selectedEventType by remember { mutableStateOf("text") }
    var showDebugDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Simulator", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                },
                actions = {
                    IconButton(
                        onClick = { showDebugDrawer = true },
                        modifier = Modifier.testTag("simulator_debug_button")
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = "Debug", tint = LightPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleBackground)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        EventBubble(message = msg, onInspectDebug = { showDebugDrawer = true })
                    }

                    if (isTyping) {
                        item {
                            TypingIndicator(senderName = "Persona")
                        }
                    }
                }

                Surface(
                    color = PureWhite,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = { Text("Simulate message...", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customText.isNotBlank()) {
                                    onSimulateEvent(selectedEventType, customText, "", "", "", "", 0)
                                    customText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Send")
                        }
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
}
