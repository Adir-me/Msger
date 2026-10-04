package com.example.personamessenger.ui.screens.context

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentContextScreen(
    activeContext: ContextEntity?,
    onSaveContext: (ContextEntity) -> Unit
) {
    var todayEvents by remember(activeContext) { mutableStateOf(activeContext?.whatHappenedToday ?: "") }
    var plans by remember(activeContext) { mutableStateOf(activeContext?.currentPlans ?: "") }
    var emotionalState by remember(activeContext) { mutableStateOf(activeContext?.temporaryEmotionalState ?: "") }
    var immediateThings by remember(activeContext) { mutableStateOf(activeContext?.immediateThingsToKnow ?: "") }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Current Context", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                },
                actions = {
                    Button(
                        onClick = {
                            val updated = activeContext?.copy(
                                whatHappenedToday = todayEvents,
                                currentPlans = plans,
                                temporaryEmotionalState = emotionalState,
                                immediateThingsToKnow = immediateThings,
                                updatedAt = System.currentTimeMillis()
                            ) ?: ContextEntity(
                                personaId = 1,
                                whatHappenedToday = todayEvents,
                                currentPlans = plans,
                                temporaryEmotionalState = emotionalState,
                                immediateThingsToKnow = immediateThings
                            )
                            onSaveContext(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Save")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        label = { Text("Current Plans") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = emotionalState,
                        onValueChange = { emotionalState = it },
                        label = { Text("Temporary Mood") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = immediateThings,
                        onValueChange = { immediateThings = it },
                        label = { Text("Immediate Constraints") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
