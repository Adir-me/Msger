package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_configs")
data class ModelConfigEntity(
    @PrimaryKey
    val id: String = "primary_config",
    val openRouterApiKey: String = "",
    val defaultModel: String = "meta-llama/llama-3.3-70b-instruct:free",
    val fallbackModel: String = "google/gemini-2.0-flash-exp:free",
    val manualModelId: String = "",
    val isManualModelActive: Boolean = false,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 800,
    val contextWindowTokens: Int = 8000,
    val thinkingBudgetTokens: Int = 0,
    val isGlobalAutomationActive: Boolean = true, // Global Master Emergency Stop
    val isAiDisclosureEnabled: Boolean = true, // Consensual AI disclosure tag
    val disclosureFormat: String = "badge", // badge, footer, subtle
    val updatedAt: Long = System.currentTimeMillis()
)
