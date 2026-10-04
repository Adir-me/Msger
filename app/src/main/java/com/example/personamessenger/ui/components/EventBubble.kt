package com.example.personamessenger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventBubble(
    message: MessageEntity,
    onPlayVoice: (String) -> Unit = {},
    onInspectDebug: () -> Unit = {}
) {
    val isMe = message.isFromMe
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // iOS Style Badges (AI / Human Override)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            if (message.isAiGenerated) {
                Surface(
                    color = LightPurpleBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightPurpleSoft),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = LightPurple,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "AI Persona",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightPurpleText
                        )
                    }
                }
            } else if (message.isManualOverride) {
                Surface(
                    color = AppleGreenLight,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "You",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${message.senderName} • ${timeFormat.format(Date(message.timestamp))}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Main iOS iMessage Bubble
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isMe) AppleGreen else PureWhite
                )
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("message_bubble_${message.id}")
        ) {
            Column {
                when (message.eventType) {
                    "reaction" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.reactionEmoji.ifBlank { "❤️" },
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Reacted to message",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMe) PureWhite else TextPrimary
                                )
                                if (message.replyToMessageId.isNotBlank()) {
                                    Text(
                                        text = "\"${message.replyToMessageId.take(30)}...\"",
                                        fontSize = 11.sp,
                                        color = if (isMe) PureWhite.copy(alpha = 0.85f) else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    "reel" -> {
                        Column {
                            Surface(
                                color = if (isMe) PureWhite.copy(alpha = 0.2f) else LightPurpleBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = "Reel",
                                        tint = if (isMe) PureWhite else LightPurple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Instagram Reel",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isMe) PureWhite else TextPrimary
                                        )
                                        Text(
                                            text = message.mediaDescription.ifBlank { "Shared Reel" },
                                            fontSize = 11.sp,
                                            color = if (isMe) PureWhite.copy(alpha = 0.9f) else TextSecondary
                                        )
                                    }
                                }
                            }
                            if (message.contentText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.contentText,
                                    fontSize = 15.sp,
                                    color = if (isMe) PureWhite else TextPrimary
                                )
                            }
                        }
                    }

                    "voice" -> {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        val textToSpeak = if (message.transcript.isNotBlank()) message.transcript else message.contentText
                                        onPlayVoice(textToSpeak)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isMe) PureWhite else AppleGreenLight, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play voice",
                                        tint = if (isMe) AppleGreen else AppleGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // iOS Waveform bars
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(8, 16, 22, 14, 26, 12, 20, 10, 18, 24, 14, 8)
                                    barHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(h.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (isMe) PureWhite.copy(alpha = 0.75f) else AppleGreen)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${message.durationSeconds}s",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isMe) PureWhite else TextSecondary
                                )
                            }
                            if (message.transcript.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Transcript: \"${message.transcript}\"",
                                    fontSize = 13.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = if (isMe) PureWhite.copy(alpha = 0.9f) else TextSecondary
                                )
                            }
                        }
                    }

                    "image" -> {
                        Column {
                            Surface(
                                color = if (isMe) PureWhite.copy(alpha = 0.2f) else AppleBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Photo",
                                        tint = if (isMe) PureWhite else LightPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            if (message.contentText.isNotBlank() || message.mediaDescription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.contentText.ifBlank { message.mediaDescription },
                                    fontSize = 14.sp,
                                    color = if (isMe) PureWhite else TextPrimary
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = message.contentText,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = if (isMe) PureWhite else TextPrimary
                        )
                    }
                }

                // Expandable iOS Style Operational Intent
                AnimatedVisibility(visible = isExpanded && message.operationalReason.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = if (isMe) PureWhite.copy(alpha = 0.25f) else LightPurpleBackground,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 Operational Rationale:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) PureWhite else LightPurpleText
                        )
                        Text(
                            text = message.operationalReason,
                            fontSize = 11.sp,
                            color = if (isMe) PureWhite else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
