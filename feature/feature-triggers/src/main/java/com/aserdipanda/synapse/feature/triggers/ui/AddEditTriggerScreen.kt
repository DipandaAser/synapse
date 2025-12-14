package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTriggerScreen(
    modifier: Modifier = Modifier,
    trigger: TriggerEntity? = null,
    onSave: (TriggerEntity) -> Unit,
) {
    val jsonBodyExample = """
        {
            "sender": "{{sender}}",
            "message": "{{message}}"
        }
    """.trimIndent()
    var name by remember { mutableStateOf(trigger?.name ?: "") }
    var senderPattern by remember { mutableStateOf("") }
    var senderConditionOperator by remember { mutableStateOf("EQUAL") }
    var messageConditionOperator by remember { mutableStateOf("CONTAINS") }
    var messagePattern by remember { mutableStateOf("") }
    var webhookUrl by remember { mutableStateOf( "") }
    var webhookMethod by remember { mutableStateOf( "POST") }
    var webhookBody by remember { mutableStateOf(if (trigger == null) jsonBodyExample else "") }

    val operators = listOf("EQUAL", "CONTAINS", "START WITH", "END WITH")



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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyExposedDropdownMenu(
                    options = operators,
                    selectedValue = senderConditionOperator,
                    onValueChange = { senderConditionOperator = it },
                    label = "Sender",
                    modifier = Modifier.weight(0.65f)
                )
                OutlinedTextField(
                    value = senderPattern,
                    onValueChange = { senderPattern = it },
                    label = { Text("e.g., 7515") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyExposedDropdownMenu(
                    options = operators,
                    selectedValue = messageConditionOperator,
                    onValueChange = { messageConditionOperator = it },
                    label = "Message",
                    modifier = Modifier.weight(0.65f).height(IntrinsicSize.Min)
                )
                OutlinedTextField(
                    value = messagePattern,
                    onValueChange = { messagePattern = it },
                    label = { Text("e.g., bills") },
                    modifier = Modifier.weight(1f).height(IntrinsicSize.Min),
                    singleLine = true
                )
            }

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
                minLines = 5,
            )

        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyExposedDropdownMenu(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    label: String = "",
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = if (label.isNotEmpty()) {{ Text(label) }} else null,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }
                )
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
