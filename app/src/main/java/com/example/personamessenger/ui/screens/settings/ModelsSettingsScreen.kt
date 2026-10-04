package com.example.personamessenger.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.data.local.entity.AuditLogEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import com.example.personamessenger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsSettingsScreen(
    modelConfig: ModelConfigEntity?,
    auditLogs: List<AuditLogEntity>,
    onSaveConfig: (ModelConfigEntity) -> Unit,
    onEmergencyStop: () -> Unit,
    onResumeAutomation: () -> Unit,
    onClearLogs: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var apiKey by remember(modelConfig) { mutableStateOf(modelConfig?.openRouterApiKey ?: "") }
    var defaultModel by remember(modelConfig) { mutableStateOf(modelConfig?.defaultModel ?: "meta-llama/llama-3.3-70b-instruct:free") }
    var fallbackModel by remember(modelConfig) { mutableStateOf(modelConfig?.fallbackModel ?: "google/gemini-2.0-flash-exp:free") }
    var temperature by remember(modelConfig) { mutableStateOf(modelConfig?.temperature ?: 0.75f) }
    var maxTokens by remember(modelConfig) { mutableStateOf(modelConfig?.maxTokens ?: 800) }
    var isDisclosureEnabled by remember(modelConfig) { mutableStateOf(modelConfig?.isAiDisclosureEnabled ?: true) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val popularModels = listOf(
        "meta-llama/llama-3.3-70b-instruct:free",
        "google/gemini-2.0-flash-exp:free",
        "qwen/qwen-2.5-72b-instruct:free",
        "anthropic/claude-3.5-sonnet",
        "openai/gpt-4o-mini"
    )

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Safety",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        Button(
                            onClick = {
                                val config = modelConfig?.copy(
                                    openRouterApiKey = apiKey,
                                    defaultModel = defaultModel,
                                    fallbackModel = fallbackModel,
                                    temperature = temperature,
                                    maxTokens = maxTokens,
                                    isAiDisclosureEnabled = isDisclosureEnabled,
                                    updatedAt = System.currentTimeMillis()
                                ) ?: ModelConfigEntity(
                                    openRouterApiKey = apiKey,
                                    defaultModel = defaultModel,
                                    fallbackModel = fallbackModel,
                                    temperature = temperature,
                                    maxTokens = maxTokens,
                                    isAiDisclosureEnabled = isDisclosureEnabled
                                )
                                onSaveConfig(config)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("save_model_config_button")
                        ) {
                            Text("Save")
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
                                text = "AI & Models",
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
                                text = "Audit Trail (${auditLogs.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) PureWhite else TextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Master Automation Control Group
                    Surface(
                        color = if (modelConfig?.isGlobalAutomationActive == true) AppleGreenLight else LightPurpleBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (modelConfig?.isGlobalAutomationActive == true) AppleGreen.copy(alpha = 0.5f) else LightPurpleSoft
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (modelConfig?.isGlobalAutomationActive == true) "Master AI Switch: ON" else "Master AI Switch: PAUSED",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (modelConfig?.isGlobalAutomationActive == true) AppleGreenDark else LightPurpleText
                                )
                                Text(
                                    text = "Emergency killswitch for all active chats.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Switch(
                                checked = modelConfig?.isGlobalAutomationActive == true,
                                onCheckedChange = {
                                    if (it) onResumeAutomation() else onEmergencyStop()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = AppleGreen)
                            )
                        }
                    }

                    // OpenRouter API Key Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. OpenRouter API Key", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppleGreenDark)

                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                placeholder = { Text("Optional - Local engine active by default", fontSize = 13.sp) },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Model Selection Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("2. Model Selection", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightPurpleText)

                            popularModels.forEach { m ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { defaultModel = m }
                                ) {
                                    RadioButton(
                                        selected = defaultModel == m,
                                        onClick = { defaultModel = m },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppleGreen)
                                    )
                                    Text(m, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Parameters Group
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("3. AI Parameters & Disclosure", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppleGreenDark)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Temperature: ${(temperature * 100).toInt() / 100f}", fontSize = 13.sp)
                                Slider(
                                    value = temperature,
                                    onValueChange = { temperature = it },
                                    valueRange = 0.1f..1.5f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(thumbColor = AppleGreen, activeTrackColor = AppleGreen)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Display 'AI Persona' badge", fontSize = 13.sp)
                                Switch(
                                    checked = isDisclosureEnabled,
                                    onCheckedChange = { isDisclosureEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = AppleGreen)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // Audit Trail
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audit Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        TextButton(onClick = onClearLogs) {
                            Text("Clear", color = TextSecondary)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        items(auditLogs, key = { it.id }) { log ->
                            Surface(
                                color = PureWhite,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = if (log.actionCategory.contains("EMERGENCY")) LightPurpleBackground else AppleGreenLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = log.actionCategory,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (log.actionCategory.contains("EMERGENCY")) LightPurpleText else AppleGreenDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(timeFormat.format(Date(log.timestamp)), fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(log.details, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
