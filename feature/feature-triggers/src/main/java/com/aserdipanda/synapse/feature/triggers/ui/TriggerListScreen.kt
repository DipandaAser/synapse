package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aserdipanda.core.ui.theme.SynapseAppTheme


data class Trigger(
    val id: Int,
    val name: String,
    val sender: String,
    val contains: String,
    val url: String,
    val lastTriggered: String?,
    val isEnabled: Boolean
)

/**
 * The main screen that holds the Scaffold (top bar, content, and FAB).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerListScreen(
    isServiceActive: Boolean,
    triggers: List<Trigger>,
    onToggleService: (Boolean) -> Unit,
    onAddTrigger: () -> Unit,
    onEditTrigger: (Trigger) -> Unit,
    onToggleTrigger: (Trigger, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TriggerTopAppBar(
                isListening = isServiceActive,
                onListenToggled = onToggleService
            )
        },
        floatingActionButton = {
            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text("Add Trigger") } },
                state = tooltipState,
            ) {
                FloatingActionButton(onClick = onAddTrigger) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Trigger"
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatusBanner(
                    isActive = isServiceActive,
                    enabledTriggerCount = triggers.count { it.isEnabled }
                )
            }
            items(triggers, key = { it.id }) { trigger ->
                TriggerCard(
                    trigger = trigger,
                    onClick = { onEditTrigger(trigger) },
                    onToggle = { isEnabled -> onToggleTrigger(trigger, isEnabled) }
                )
            }
        }
    }
}

/**
 * The custom Top App Bar that matches your design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerTopAppBar(
    isListening: Boolean,
    onListenToggled: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "My SMS Triggers",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            Text("Listen")
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Switch(
                checked = isListening,
                onCheckedChange = onListenToggled
            )
        },
    )
}

/**
 * The green/red status banner
 */
@Composable
fun StatusBanner(isActive: Boolean, enabledTriggerCount: Int) {
    val backgroundColor = if (isActive) Color(0xFFE6F7EA) else Color(0xFFFFF0F0)
    val contentColor = if (isActive) Color(0xFF34A853) else Color.Red
    val text = if (isActive) {
        "SMS Listener Active • $enabledTriggerCount triggers enabled"
    } else {
        "SMS Listener Inactive"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * A single card representing a trigger
 */
@Composable
fun TriggerCard(
    trigger: Trigger,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trigger.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = trigger.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TriggerInfoRow(
                    icon = Icons.Default.Phone,
                    text = "From: ${trigger.sender}"
                )
                TriggerInfoRow(
                    icon = Icons.Default.CheckCircle,
                    text = "Contains: ${trigger.contains}"
                )
                TriggerInfoRow(
                    icon = Icons.Default.ArrowForward,
                    text = "POST → ${trigger.url}"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TriggerStatusText(trigger = trigger)
            }
        }
    }
}

/**
 * A helper composable for an icon + text row in the card
 */
@Composable
fun TriggerInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A helper composable for the status text in the card footer
 */
@Composable
fun TriggerStatusText(trigger: Trigger) {
    val (icon, text, color) = when {
        !trigger.isEnabled -> Triple(
            Icons.Outlined.KeyboardArrowUp,
            "Disabled",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        trigger.lastTriggered != null -> Triple(
            Icons.Outlined.CheckCircle,
            "Last triggered ${trigger.lastTriggered}",
            Color(0xFF34A853)
        )
        else -> Triple(
            Icons.Outlined.CheckCircle,
            "Never triggered",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}



@Preview(showBackground = true)
@Composable
fun TriggerListScreenPreview() {
    val fakeTriggers = listOf(
        Trigger(1, "Bank Alert Forwarder", "7777", "\"transaction\"", "api.webhook.com/bank", "2h ago", true),
        Trigger(2, "OTP Extractor", "Any", "\"OTP\" or \"code\"", "myapp.com/otp", null, true),
        Trigger(3, "Order Updates", "AMAZON", "\"delivered\"", "orders.api.com/status", null, false)
    )

    SynapseAppTheme {
        TriggerListScreen(
            isServiceActive = true,
            triggers = fakeTriggers,
            onToggleService = {},
            onAddTrigger = {},
            onEditTrigger = {},
            onToggleTrigger = { _, _ -> }
        )
    }
}