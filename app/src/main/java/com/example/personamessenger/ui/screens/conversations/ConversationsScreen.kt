package com.example.personamessenger.ui.screens.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
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
import com.example.personamessenger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    conversations: List<ConversationEntity>,
    isGlobalAutomationActive: Boolean,
    onSelectConversation: (Long) -> Unit,
    onEmergencyStop: () -> Unit,
    onCreateNewChat: (title: String, platform: String) -> Unit = { _, _ -> }
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showNewChatDialog = true },
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = AppleGreenDark)
                    }

                    Surface(
                        color = if (isGlobalAutomationActive) AppleGreenLight else LightPurpleBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isGlobalAutomationActive) AppleGreen.copy(alpha = 0.5f) else LightPurpleSoft
                        ),
                        modifier = Modifier
                            .clickable { onEmergencyStop() }
                            .padding(end = 12.dp)
                            .testTag("emergency_stop_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isGlobalAutomationActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "AI Switch",
                                tint = if (isGlobalAutomationActive) AppleGreenDark else LightPurpleText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGlobalAutomationActive) "Auto AI" else "Paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGlobalAutomationActive) AppleGreenDark else LightPurpleText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleBackground)
            )
        }
    ) { paddingValues ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No conversations yet.",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showNewChatDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start a New Chat")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    ConversationItemCard(
                        conversation = conv,
                        onClick = { onSelectConversation(conv.id) },
                        timeFormat = timeFormat
                    )
                }
            }
        }
    }

    if (showNewChatDialog) {
        var contactName by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("Instagram") }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("New Conversation", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Contact Name / Handle") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Instagram", "WhatsApp", "Telegram").forEach { p ->
                            FilterChip(
                                selected = platform == p,
                                onClick = { platform = p },
                                label = { Text(p) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleGreen,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactName.isNotBlank()) {
                            onCreateNewChat(contactName.trim(), platform)
                            showNewChatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ConversationItemCard(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    timeFormat: SimpleDateFormat
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_card_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(AppleGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.title.take(1).ifBlank { "💬" },
                    color = AppleGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = timeFormat.format(Date(conversation.lastMessageTimestamp)),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.lastMessageSnippet.ifBlank { "Tap to open chat" },
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = if (conversation.isAiActive) AppleGreenLight else LightPurpleBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (conversation.isAiActive) "Auto AI" else "Manual",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (conversation.isAiActive) AppleGreenDark else LightPurpleText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
