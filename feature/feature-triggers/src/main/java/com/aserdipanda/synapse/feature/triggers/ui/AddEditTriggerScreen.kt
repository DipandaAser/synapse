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
    modifier: Modifier = Modifier,
    trigger: TriggerEntity? = null,
    onSave: (TriggerEntity) -> Unit,
) {
    var name by remember { mutableStateOf(trigger?.name ?: "") }
    var senderPattern by remember { mutableStateOf("") }
    var messagePattern by remember { mutableStateOf("") }
    var webhookUrl by remember { mutableStateOf( "") }
    var webhookMethod by remember { mutableStateOf( "POST") }
    var webhookBody by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (trigger == null) "Add Trigger" else "Edit Trigger") },
                actions = {
                    TextButton(onClick = {
                        val triggerEntity = TriggerEntity(
                            id = trigger?.id ?: 0,
                            name = name,
                            enabled = trigger?.enabled ?: true,
                            createdAt = trigger?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(triggerEntity)
                    },
                        enabled = name.isNotBlank() && senderPattern.isNotBlank() && webhookUrl.isNotBlank())
                    {
                        Text("Save")
                    }
                }
            )
        },
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = senderPattern,
                onValueChange = { senderPattern = it },
                label = { Text("Sender Pattern (e.g., 7515)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = messagePattern,
                onValueChange = { messagePattern = it },
                label = { Text("Message Pattern (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = webhookMethod,
                onValueChange = { webhookMethod = it },
                label = { Text("Webhook Method") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { webhookUrl = it },
                label = { Text("Webhook -URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = webhookBody,
                onValueChange = { webhookBody = it },
                label = { Text("Webhook Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )
        }
    }
}

@Preview(showBackground = true, name = "Add Trigger Screen")
@Composable
fun AddTriggerScreenPreview() {
    AddEditTriggerScreen(
        trigger = null,
        onSave = {},
    )
}

@Preview(showBackground = true, name = "Edit Trigger Screen")
@Composable
fun EditTriggerScreenPreview() {
    val sampleTrigger = TriggerEntity(
        id = 1,
        name = "Bank Alert",
        enabled = true
    )
    AddEditTriggerScreen(trigger = sampleTrigger, onSave = {})
}
