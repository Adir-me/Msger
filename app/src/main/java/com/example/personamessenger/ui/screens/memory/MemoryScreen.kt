package com.example.personamessenger.ui.screens.memory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    memories: List<MemoryEntity>,
    selectedCategory: String,
    searchQuery: String,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSaveMemory: (MemoryEntity) -> Unit,
    onTogglePin: (MemoryEntity) -> Unit,
    onDeleteMemory: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<MemoryEntity?>(null) }

    val categories = listOf("all", "fact", "preference", "relationship", "event", "plan", "temporary")

    Scaffold(
        containerColor = AppleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Memory Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            editingMemory = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("add_memory_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
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
            // iOS Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search memories and facts...", fontSize = 14.sp, color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedBorderColor = AppleGreen,
                    unfocusedBorderColor = AppleBorder
                )
            )

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) AppleGreen else PureWhite,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AppleGreen else AppleBorder),
                        modifier = Modifier.clickable { onSelectCategory(cat) }
                    ) {
                        Text(
                            text = cat.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) PureWhite else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No memories found.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memories, key = { it.id }) { mem ->
                        MemoryCardItem(
                            memory = mem,
                            onTogglePin = { onTogglePin(mem) },
                            onEdit = {
                                editingMemory = mem
                                showAddDialog = true
                            },
                            onDelete = { onDeleteMemory(mem.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MemoryDialog(
            memoryToEdit = editingMemory,
            onDismiss = { showAddDialog = false },
            onSave = {
                onSaveMemory(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MemoryCardItem(
    memory: MemoryEntity,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (memory.isPinned) LightPurpleSoft else AppleBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (memory.isPinned) LightPurpleBackground else AppleGreenLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = memory.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (memory.isPinned) LightPurpleText else AppleGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "★".repeat(memory.importance),
                        fontSize = 12.sp,
                        color = AppleGreen
                    )
                }

                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (memory.isPinned) LightPurple else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppleGreenDark, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.content,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = TextPrimary
            )

            if (memory.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    memory.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            color = AppleBackground,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryDialog(
    memoryToEdit: MemoryEntity?,
    onDismiss: () -> Unit,
    onSave: (MemoryEntity) -> Unit
) {
    var content by remember { mutableStateOf(memoryToEdit?.content ?: "") }
    var category by remember { mutableStateOf(memoryToEdit?.category ?: "fact") }
    var importance by remember { mutableStateOf(memoryToEdit?.importance ?: 3) }
    var tags by remember { mutableStateOf(memoryToEdit?.tags ?: "") }
    var isPinned by remember { mutableStateOf(memoryToEdit?.isPinned ?: false) }

    val categories = listOf("fact", "preference", "relationship", "event", "plan", "temporary")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (memoryToEdit == null) "Add Memory" else "Edit Memory", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleGreen,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleGreen,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Importance: $importance/5", fontSize = 13.sp, color = TextPrimary)
                    Slider(
                        value = importance.toFloat(),
                        onValueChange = { importance = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.width(140.dp),
                        colors = SliderDefaults.colors(thumbColor = AppleGreen, activeTrackColor = AppleGreen)
                    )
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = CheckboxDefaults.colors(checkedColor = LightPurple)
                    )
                    Text("Pin memory (High retrieval priority)", fontSize = 12.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        val memory = memoryToEdit?.copy(
                            content = content,
                            category = category,
                            importance = importance,
                            tags = tags,
                            isPinned = isPinned,
                            updatedAt = System.currentTimeMillis()
                        ) ?: MemoryEntity(
                            personaId = 1,
                            content = content,
                            category = category,
                            importance = importance,
                            tags = tags,
                            isPinned = isPinned
                        )
                        onSave(memory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
