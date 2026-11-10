package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.synapse.data.triggers.local.ActionEntity
import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.Operator
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import com.aserdipanda.synapse.feature.triggers.TriggerTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTriggerScreen(
    modifier: Modifier = Modifier,
    trigger: TriggerEntity? = null,
    senderCondition: ConditionEntity? = null,
    messageCondition: ConditionEntity? = null,
    actionEntity: ActionEntity? = null,
    // A new trigger started from an example: its name and message rule are filled in, and it
    // matches any sender.
    template: TriggerTemplate? = null,
    onBack: () -> Unit,
    onSave: (trigger: TriggerEntity, senderCondition: ConditionEntity?, messageCondition: ConditionEntity?, action: ActionEntity) -> Unit,
) {
    val jsonBodyExample = """
        {
            "sender": "{{sender}}",
            "message": "{{message}}"
        }
    """.trimIndent()
    val templateName = template?.let { stringResource(it.title) }
    var name by remember { mutableStateOf(trigger?.name ?: templateName ?: "") }

    var senderPattern by remember {
        mutableStateOf(senderCondition?.value ?: if (template != null) "" else "+1 246 ")
    }
    var senderConditionOperator by remember { mutableStateOf(senderCondition?.operator ?: Operator.EQUALS) }
    val senderInputTextLabel = senderConditionOperator.description()


    var messageConditionOperator by remember { mutableStateOf(messageCondition?.operator ?: Operator.CONTAINS) }
    // Starts empty: the field's "e.g., bills" label is the hint, and sample text must never be saved as a rule.
    var messagePattern by remember { mutableStateOf(messageCondition?.value ?: template?.messageContains ?: "") }

    val savedWebhook = remember { actionEntity?.let { WebhookConfig.fromArg(it.arg) } }
    var webhookUrl by remember { mutableStateOf(savedWebhook?.url ?: "https://") }
    var webhookMethod by remember { mutableStateOf(savedWebhook?.method ?: "POST") }
    var webhookBody by remember {
        mutableStateOf(savedWebhook?.body ?: if (trigger == null) jsonBodyExample else "")
    }




    val operators = Operator.entries



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (trigger == null) "Add Trigger" else "Edit Trigger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val triggerEntity = TriggerEntity(
                            id = trigger?.id ?: 0,
                            name = name,
                            enabled = trigger?.enabled ?: true,
                            createdAt = trigger?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        // A blank pattern means "no rule on this field". Saving it would create a
                        // rule that matches nothing (EQUALS "") or everything (CONTAINS "").
                        val senderConditionEntity = senderPattern.takeIf { it.isNotBlank() }?.let {
                            ConditionEntity(
                                id = senderCondition?.id ?: 0,
                                triggerId = triggerEntity.id,
                                field = "sender",
                                operator = senderConditionOperator,
                                value = it
                            )
                        }
                        val messageConditionEntity = messagePattern.takeIf { it.isNotBlank() }?.let {
                            ConditionEntity(
                                id = messageCondition?.id ?: 0,
                                triggerId = triggerEntity.id,
                                field = "message_body",
                                operator = messageConditionOperator,
                                value = it
                            )
                        }
                        val webhookConfig = WebhookConfig(
                            url = webhookUrl.trim(),
                            method = webhookMethod.trim().uppercase(),
                            body = webhookBody.takeIf { it.isNotBlank() }
                        )
                        val webhookAction = ActionEntity(
                            id = actionEntity?.id ?: 0,
                            triggerId = triggerEntity.id,
                            type = "WEBHOOK",
                            arg = webhookConfig.toArg()
                        )
                        onSave(triggerEntity, senderConditionEntity, messageConditionEntity, webhookAction)
                    },
                        enabled = name.isNotBlank() && (
                                senderPattern.isNotBlank() || messagePattern.isNotBlank()
                                ) && webhookUrl.isNotBlank() && webhookMethod.isNotBlank()
                    )
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
                    optionLabel = { it.displayName },
                    label = "Sender",
                    modifier = Modifier.weight(0.65f)
                )
                OutlinedTextField(
                    value = senderPattern,
                    onValueChange = { senderPattern = it },
                    label = { Text(senderInputTextLabel) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }


                MyExposedDropdownMenu(
                    options = operators,
                    selectedValue = messageConditionOperator,
                    onValueChange = { messageConditionOperator = it },
                    optionLabel = { it.displayName },
                    label = "Message",
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                )
                OutlinedTextField(
                    value = messagePattern,
                    onValueChange = { messagePattern = it },
                    label = { Text("e.g., bills") },
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    minLines = 1
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
fun <T> MyExposedDropdownMenu(
    modifier: Modifier = Modifier,
    options: List<T>,
    selectedValue: T,
    onValueChange: (T) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            value = optionLabel(selectedValue),
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
                    text = { Text(optionLabel(selectionOption)) },
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
        onBack = {},
        onSave = { _, _, _, _ -> },
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
    AddEditTriggerScreen(trigger = sampleTrigger, onBack = {}, onSave = { _, _, _, _ -> })
}
