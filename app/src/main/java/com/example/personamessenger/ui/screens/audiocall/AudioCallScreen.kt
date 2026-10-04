package com.example.personamessenger.ui.screens.audiocall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.CallSessionEntity
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCallScreen(
    audioAssets: List<AudioAssetEntity>,
    currentSession: CallSessionEntity?,
    liveCallLogs: List<String>,
    onStartCall: () -> Unit,
    onAcceptCall: () -> Unit,
    onCallerSpoke: (String) -> Unit,
    onEndCall: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onSaveAudio: (AudioAssetEntity) -> Unit,
    onDeleteAudio: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddAudioDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voice & Calls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        Button(
                            onClick = { showAddAudioDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("add_audio_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Clip")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // iOS Segmented Bar
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        color = if (selectedTab == 0) AppleGreen else PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "Audio Clips (${audioAssets.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) PureWhite else TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = if (selectedTab == 1) LightPurple else PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "Call State Simulator",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) PureWhite else TextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(audioAssets, key = { it.id }) { audio ->
                        Surface(
                            color = PureWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onPlayAudio(audio.transcript) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(AppleGreenLight, CircleShape)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AppleGreenDark)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(audio.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                            Text("${audio.durationSeconds}s • ${audio.language}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    IconButton(onClick = { onDeleteAudio(audio.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Transcript: \"${audio.transcript}\"", fontSize = 13.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Context: ${audio.contextDescription}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            } else {
                // Call State Machine View
                val state = currentSession?.state ?: "IDLE"
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(if (state != "IDLE" && state != "ENDED") AppleGreen else LightPurple)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (state != "IDLE" && state != "ENDED") "Call: Active ($state)" else "Ready to Call",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                }

                                if (state == "IDLE" || state == "ENDED") {
                                    Button(
                                        onClick = onStartCall,
                                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
                                    ) {
                                        Text("Simulate Call")
                                    }
                                } else {
                                    Button(
                                        onClick = onEndCall,
                                        colors = ButtonDefaults.buttonColors(containerColor = LightPurple)
                                    ) {
                                        Text("End Call")
                                    }
                                }
                            }

                            if (state == "INCOMING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onAcceptCall,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accept Call (Play Greeting)")
                                }
                            }
                        }
                    }

                    if (state == "LISTENING" || state == "GREETING" || state == "PROCESSING") {
                        Text("Simulate Speech Input", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onCallerSpoke("Hey! What are you doing today?") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ask Activity", fontSize = 11.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = { onCallerSpoke("Let's meet up for coffee!") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Coffee Plans", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }

                    Text("Call Audio & State Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (liveCallLogs.isEmpty()) {
                                item {
                                    Text("Click 'Simulate Call' to test the state machine.", fontSize = 13.sp, color = TextSecondary)
                                }
                            } else {
                                items(liveCallLogs) { log ->
                                    Text("• $log", fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAudioDialog) {
        AddAudioClipDialog(
            onDismiss = { showAddAudioDialog = false },
            onSave = {
                onSaveAudio(it)
                showAddAudioDialog = false
            }
        )
    }
}

@Composable
fun AddAudioClipDialog(
    onDismiss: () -> Unit,
    onSave: (AudioAssetEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var contextDesc by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Audio Clip", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Clip Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = transcript,
                    onValueChange = { transcript = it },
                    label = { Text("Spoken Transcript") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = contextDesc,
                    onValueChange = { contextDesc = it },
                    label = { Text("Context & Situation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (greeting, excited)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && transcript.isNotBlank()) {
                        onSave(
                            AudioAssetEntity(
                                name = name,
                                transcript = transcript,
                                contextDescription = contextDesc,
                                tags = tags,
                                durationSeconds = 4,
                                language = "English"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
