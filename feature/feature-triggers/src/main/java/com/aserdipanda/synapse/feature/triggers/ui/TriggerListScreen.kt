package com.aserdipanda.synapse.feature.triggers.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.triggers.R
import com.aserdipanda.synapse.feature.triggers.TriggerTemplate


data class Trigger(
    val id: Int,
    val name: String,
    val sender: String,
    val contains: String,
    val url: String,
    val lastTriggered: String?,
    val isEnabled: Boolean
)

// Room below the content for the New trigger button, so it never covers the last item.
private val FabClearance = 88.dp

/**
 * Home: whether Synapse is listening, then the triggers, or examples to start from when there
 * are none. [triggers] is null until they load, so the empty state never flashes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerListScreen(
    listenerStatus: ListenerStatus,
    triggers: List<Trigger>?,
    onListeningChange: (Boolean) -> Unit,
    onAllowSmsAccess: () -> Unit,
    onAddTrigger: () -> Unit,
    onUseTemplate: (TriggerTemplate) -> Unit,
    onEditTrigger: (Trigger) -> Unit,
    onToggleTrigger: (Trigger, Boolean) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // The bar takes the surfaceContainer color once content scrolls under it.
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val newTrigger = stringResource(R.string.home_new_trigger)
            ExtendedFloatingActionButton(
                onClick = onAddTrigger,
                // This button hides its text from accessibility services (the text goes away
                // when it collapses), so the icon carries the label.
                icon = { Icon(Icons.Default.Add, contentDescription = newTrigger) },
                text = { Text(newTrigger) }
            )
        }
    ) { innerPadding ->
        if (triggers == null) return@Scaffold

        val statusCard = @Composable {
            ListenerStatusCard(
                status = listenerStatus,
                triggerCount = triggers.size,
                enabledTriggerCount = triggers.count { it.isEnabled },
                onListeningChange = onListeningChange,
                onAllowSmsAccess = onAllowSmsAccess
            )
        }

        if (triggers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, bottom = FabClearance)
            ) {
                statusCard()
                // Centers the empty state in the space left, and lets it scroll on small screens.
                Spacer(Modifier.weight(1f))
                NoTriggersYet(
                    onUseTemplate = onUseTemplate,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                Spacer(Modifier.weight(1f))
            }
        } else {
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + FabClearance
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { statusCard() }
                item {
                    Text(
                        text = stringResource(R.string.home_triggers_heading),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 4.dp, top = 4.dp, end = 4.dp)
                            .semantics { heading() }
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
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
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



@Preview(name = "Listening", showBackground = true)
@Composable
fun TriggerListScreenPreview() {
    val fakeTriggers = listOf(
        Trigger(1, "Bank Alert Forwarder", "7777", "\"transaction\"", "api.webhook.com/bank", "2h ago", true),
        Trigger(2, "OTP Extractor", "Any", "\"OTP\" or \"code\"", "myapp.com/otp", null, true),
        Trigger(3, "Order Updates", "AMAZON", "\"delivered\"", "orders.api.com/status", null, false)
    )

    SynapseAppTheme(dynamicColor = false) {
        TriggerListScreen(
            listenerStatus = ListenerStatus.Listening,
            triggers = fakeTriggers,
            onListeningChange = {},
            onAllowSmsAccess = {},
            onAddTrigger = {},
            onUseTemplate = {},
            onEditTrigger = {},
            onToggleTrigger = { _, _ -> }
        )
    }
}

@Preview(name = "No triggers, light", showBackground = true, heightDp = 917)
@Preview(name = "No triggers, dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TriggerListScreenEmptyPreview() {
    SynapseAppTheme(dynamicColor = false) {
        TriggerListScreen(
            listenerStatus = ListenerStatus.Listening,
            triggers = emptyList(),
            onListeningChange = {},
            onAllowSmsAccess = {},
            onAddTrigger = {},
            onUseTemplate = {},
            onEditTrigger = {},
            onToggleTrigger = { _, _ -> }
        )
    }
}
