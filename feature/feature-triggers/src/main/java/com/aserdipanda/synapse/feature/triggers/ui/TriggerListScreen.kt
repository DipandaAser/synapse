package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity

@Composable
fun TriggerListScreen(
    triggers: List<TriggerEntity>,
    isLoading: Boolean,
    onAddTrigger: () -> Unit,
    onEditTrigger: (TriggerEntity) -> Unit,
    onDeleteTrigger: (TriggerEntity) -> Unit,
    onToggleTrigger: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrigger) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                triggers.isEmpty() -> {
                    Text(
                        text = "No triggers yet. Tap + to add one.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(triggers) { trigger ->
                            TriggerItem(
                                trigger = trigger,
                                onEdit = { onEditTrigger(trigger) },
                                onDelete = { onDeleteTrigger(trigger) },
                                onToggle = { onToggleTrigger(trigger.id, !trigger.isActive) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TriggerItem(
    trigger: TriggerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trigger.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = trigger.isActive,
                    onCheckedChange = { onToggle() }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sender: ${trigger.senderPattern}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (trigger.messagePattern != null) {
                Text(
                    text = "Message: ${trigger.messagePattern}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "Targets: ${trigger.targetPhoneNumbers.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
