package com.example.personamessenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.domain.ai.OperationalDebugInfo
import com.example.personamessenger.ui.theme.*

@Composable
fun DebugPanelDrawer(
    debugInfo: OperationalDebugInfo?,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = PureWhite,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // iOS drag pill
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppleBorder)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Debug",
                        tint = LightPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Operational Trace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            HorizontalDivider(color = AppleBorder, modifier = Modifier.padding(vertical = 8.dp))

            if (debugInfo == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No operational events processed yet.\nSend a message to view the trace.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DebugStatChip("Event", debugInfo.eventType, LightPurple, Modifier.weight(1f))
                        DebugStatChip("Latency", "${debugInfo.latencyMs}ms", AppleGreen, Modifier.weight(1f))
                        DebugStatChip("Memories", "${debugInfo.retrievedMemoriesCount} hits", LightPurpleText, Modifier.weight(1f))
                    }

                    // Selected Model
                    DebugSectionCard("Model Engine", debugInfo.selectedModel, Icons.Default.Memory)

                    // Normalized Event
                    DebugSectionCard("Normalized Event", debugInfo.normalizedEvent, Icons.Default.ChatBubbleOutline)

                    // Retrieved Memories
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppleBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = LightPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retrieved Memories (${debugInfo.retrievedMemoriesCount})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (debugInfo.retrievedMemoriesSummary.isEmpty()) {
                                Text("None injected", fontSize = 12.sp, color = TextSecondary)
                            } else {
                                debugInfo.retrievedMemoriesSummary.forEach { mem ->
                                    Text("• $mem", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Response Decision Rationale
                    DebugSectionCard("Response Intent & Rationale", debugInfo.responseDecision, Icons.Default.EmojiObjects)

                    // Multi-message Timings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppleBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Burst Splitting & Timing Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            debugInfo.generatedMessages.forEachIndexed { i, msg ->
                                val delay = debugInfo.timingDelays.getOrNull(i) ?: 0L
                                Text("Burst ${i + 1} (${delay}ms delay): \"$msg\"", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                            }
                        }
                    }

                    if (debugInfo.memoryUpdates.isNotEmpty()) {
                        DebugSectionCard("Extracted Durable Memories", debugInfo.memoryUpdates.joinToString("\n"), Icons.Default.AddBox)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugStatChip(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun DebugSectionCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppleBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = LightPurple, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, fontSize = 12.sp, color = TextPrimary)
        }
    }
}
