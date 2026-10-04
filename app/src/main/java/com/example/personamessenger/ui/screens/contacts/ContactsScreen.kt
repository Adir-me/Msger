package com.example.personamessenger.ui.screens.contacts

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<ContactEntity>,
    personas: List<PersonaEntity>,
    onToggleAi: (Long, Boolean) -> Unit,
    onUpdateContact: (ContactEntity) -> Unit
) {
    var editingContact by remember { mutableStateOf<ContactEntity?>(null) }

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Contacts & Controls", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleBackground)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AppleGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Consensual Automation: AI only responds to enabled contacts.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(contacts, key = { it.id }) { contact ->
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (contact.isAiEnabled) AppleGreenLight else LightPurpleBackground,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(contact.avatarEmoji, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (contact.isAuthorizedAccount) AppleGreenLight else LightPurpleBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (contact.isAuthorizedAccount) "AUTHORIZED" else "PAUSED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (contact.isAuthorizedAccount) AppleGreenDark else LightPurpleText,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "@${contact.username} • ${contact.platform}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = contact.isAiEnabled,
                            onCheckedChange = { onToggleAi(contact.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = AppleGreen),
                            modifier = Modifier.testTag("contact_switch_${contact.id}")
                        )
                    }
                }
            }
        }
    }
}
