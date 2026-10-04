package com.example.personamessenger.ui.screens.persona

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaScreen(
    personas: List<PersonaEntity>,
    selectedPersona: PersonaEntity?,
    activeContext: ContextEntity?,
    onSelectPersona: (Long) -> Unit,
    onSavePersona: (PersonaEntity) -> Unit,
    onSaveContext: (ContextEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Persona, 1: Live Context

    // Persona states
    var name by remember(selectedPersona) { mutableStateOf(selectedPersona?.name ?: "") }
    var avatarEmoji by remember(selectedPersona) { mutableStateOf(selectedPersona?.avatarEmoji ?: "✨") }
    var personality by remember(selectedPersona) { mutableStateOf(selectedPersona?.personalityDescription ?: "") }
    var ageRange by remember(selectedPersona) { mutableStateOf(selectedPersona?.ageRange ?: "22-26") }
    var interests by remember(selectedPersona) { mutableStateOf(selectedPersona?.interests ?: "") }
    var commStyle by remember(selectedPersona) { mutableStateOf(selectedPersona?.communicationStyle ?: "") }
    var humor by remember(selectedPersona) { mutableStateOf(selectedPersona?.humorStyle ?: "") }
    var vocab by remember(selectedPersona) { mutableStateOf(selectedPersona?.vocabulary ?: "") }
    var favoriteExpressions by remember(selectedPersona) { mutableStateOf(selectedPersona?.favoriteExpressions ?: "") }
    var emojiFreq by remember(selectedPersona) { mutableStateOf(selectedPersona?.emojiFrequency ?: "medium") }
    var preferredEmojis by remember(selectedPersona) { mutableStateOf(selectedPersona?.preferredEmojis ?: "😂,✨,☕,💀,🔥") }
    var languageStyle by remember(selectedPersona) { mutableStateOf(selectedPersona?.languageStyle ?: "English") }
    var typingSpeedWpm by remember(selectedPersona) { mutableStateOf(selectedPersona?.typingSpeedWpm ?: 80) }
    var splitProb by remember(selectedPersona) { mutableStateOf(selectedPersona?.splitProbability ?: 0.65f) }

    // Context states
    var todayEvents by remember(activeContext) { mutableStateOf(activeContext?.whatHappenedToday ?: "") }
    var plans by remember(activeContext) { mutableStateOf(activeContext?.currentPlans ?: "") }
    var emotionalState by remember(activeContext) { mutableStateOf(activeContext?.temporaryEmotionalState ?: "") }
    var immediateThings by remember(activeContext) { mutableStateOf(activeContext?.immediateThingsToKnow ?: "") }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Persona & Context",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            if (selectedTab == 0 && selectedPersona != null) {
                                val updated = selectedPersona.copy(
                                    name = name,
                                    avatarEmoji = avatarEmoji,
                                    personalityDescription = personality,
                                    ageRange = ageRange,
                                    interests = interests,
                                    communicationStyle = commStyle,
                                    humorStyle = humor,
                                    vocabulary = vocab,
                                    favoriteExpressions = favoriteExpressions,
                                    emojiFrequency = emojiFreq,
                                    preferredEmojis = preferredEmojis,
                                    languageStyle = languageStyle,
                                    typingSpeedWpm = typingSpeedWpm,
                                    splitProbability = splitProb,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSavePersona(updated)
                            } else if (selectedTab == 1) {
                                val updated = activeContext?.copy(
                                    whatHappenedToday = todayEvents,
                                    currentPlans = plans,
                                    temporaryEmotionalState = emotionalState,
                                    immediateThingsToKnow = immediateThings,
                                    updatedAt = System.currentTimeMillis()
                                ) ?: ContextEntity(
                                    personaId = selectedPersona?.id ?: 1,
                                    whatHappenedToday = todayEvents,
                                    currentPlans = plans,
                                    temporaryEmotionalState = emotionalState,
                                    immediateThingsToKnow = immediateThings
                                )
                                onSaveContext(updated)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("save_persona_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
            // iOS Segmented Picker
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
                                text = "Persona Definition",
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
                                text = "Live Context (High Priority)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) PureWhite else TextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Persona Definition Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Persona Switcher Cards
                    Text("Select Preset Persona", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        personas.forEach { p ->
                            val isSelected = p.id == selectedPersona?.id
                            Surface(
                                color = if (isSelected) AppleGreenLight else PureWhite,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AppleGreen else AppleBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectPersona(p.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(p.avatarEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(p.languageStyle, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // Identity Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. Character & Tone", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppleGreenDark)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Name") },
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = avatarEmoji,
                                    onValueChange = { avatarEmoji = it },
                                    label = { Text("Emoji") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = personality,
                                onValueChange = { personality = it },
                                label = { Text("Persona Description & Vibe") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 3
                            )

                            OutlinedTextField(
                                value = interests,
                                onValueChange = { interests = it },
                                label = { Text("Interests (Coffee, Tech, Art)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Vocabulary & Slang Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("2. Slang & Expressions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightPurpleText)

                            OutlinedTextField(
                                value = vocab,
                                onValueChange = { vocab = it },
                                label = { Text("Vocabulary / Slang (lowkey, tbh, legit)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = favoriteExpressions,
                                onValueChange = { favoriteExpressions = it },
                                label = { Text("Favorite Expressions") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("English", "Hinglish").forEach { lang ->
                                    FilterChip(
                                        selected = languageStyle == lang,
                                        onClick = { languageStyle = lang },
                                        label = { Text(lang) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LightPurple,
                                            selectedLabelColor = PureWhite
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Timing & Splitting Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("3. Typing Speed & Burst Splitting", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppleGreenDark)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Typing Speed: $typingSpeedWpm WPM", fontSize = 13.sp, color = TextPrimary)
                                Slider(
                                    value = typingSpeedWpm.toFloat(),
                                    onValueChange = { typingSpeedWpm = it.toInt() },
                                    valueRange = 40f..140f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(thumbColor = AppleGreen, activeTrackColor = AppleGreen)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Split Bursts: ${(splitProb * 100).toInt()}%", fontSize = 13.sp, color = TextPrimary)
                                Slider(
                                    value = splitProb,
                                    onValueChange = { splitProb = it },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(thumbColor = AppleGreen, activeTrackColor = AppleGreen)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // Live Context Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = LightPurpleBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightPurpleSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = LightPurple)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Current situation takes strict priority over old memories when replying.",
                                fontSize = 13.sp,
                                color = LightPurpleText
                            )
                        }
                    }

                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = todayEvents,
                                onValueChange = { todayEvents = it },
                                label = { Text("What Happened Today") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 3
                            )

                            OutlinedTextField(
                                value = plans,
                                onValueChange = { plans = it },
                                label = { Text("Current Plans (Tonight / Weekend)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2
                            )

                            OutlinedTextField(
                                value = emotionalState,
                                onValueChange = { emotionalState = it },
                                label = { Text("Current Mood / Vibe") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = immediateThings,
                                onValueChange = { immediateThings = it },
                                label = { Text("Immediate Constraints (e.g. low battery, noisy cafe)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
