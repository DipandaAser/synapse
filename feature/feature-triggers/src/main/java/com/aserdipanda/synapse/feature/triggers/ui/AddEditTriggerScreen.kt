package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTriggerScreen(
    trigger: TriggerEntity? = null,
    onSave: (TriggerEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(trigger?.name ?: "") }
    var senderPattern by remember { mutableStateOf(trigger?.senderPattern ?: "") }
    var messagePattern by remember { mutableStateOf(trigger?.messagePattern ?: "") }
    var webhookUrl by remember { mutableStateOf(trigger?.webhookUrl ?: "") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (trigger == null) "Add Trigger" else "Edit Trigger") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Trigger Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = senderPattern,
                onValueChange = { senderPattern = it },
                label = { Text("Sender Pattern (e.g., 7515)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = messagePattern,
                onValueChange = { messagePattern = it },
                label = { Text("Message Pattern (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { webhookUrl = it },
                label = { Text("Webhook URL") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val triggerEntity = TriggerEntity(
                            id = trigger?.id ?: 0,
                            name = name,
                            senderPattern = senderPattern,
                            messagePattern = messagePattern.takeIf { it.isNotBlank() },
                            webhookUrl = webhookUrl,
                            isActive = trigger?.isActive ?: true,
                            createdAt = trigger?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(triggerEntity)
                    },
                    enabled = name.isNotBlank() && senderPattern.isNotBlank() && webhookUrl.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Trigger Screen")
@Composable
fun AddTriggerScreenPreview() {
    AddEditTriggerScreen(
        trigger = null,
        onSave = {},
        onCancel = {}
    )
}

@Preview(showBackground = true, name = "Edit Trigger Screen")
@Composable
fun EditTriggerScreenPreview() {
    val sampleTrigger = TriggerEntity(
        id = 1,
        name = "Bank Alert",
        senderPattern = "7515",
        webhookUrl = "https://example.com/webhook",
        isActive = true
    )
    AddEditTriggerScreen(trigger = sampleTrigger, onSave = {}, onCancel = {})
}
