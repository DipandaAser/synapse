package com.aserdipanda.synapse.feature.triggers.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.theme.SynapseAppTheme

/**
 * The main screen that holds the Scaffold (top bar, content, and FAB).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerListScreen(
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onAddTrigger: () -> Unit,
) {
    // This state would eventually come from your ViewModel
    var isListening by remember { mutableStateOf(true) }
    Scaffold(
        topBar = {
            TriggerTopAppBar(
                isListening = isListening,
                onListenToggled = { enabled ->
                    isListening = enabled
                    if (enabled) {
                        onStartListening()
                    } else {
                        onStopListening()
                    }
                }
            )
        },
        floatingActionButton = {
            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text("Add Trigger")
                    }
                },
                state = tooltipState,
            ) {
            FloatingActionButton(onClick = onAddTrigger) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Trigger",
                )
            }}
        }
    ) { innerPadding ->
        // The main content of your screen (like the list of triggers)
        // will go here. For now, it's just a placeholder.
        Text(
            modifier = Modifier.padding(innerPadding),
            text = "Your trigger list will go here."
        )
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
                text = "Synapse",
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
 * A preview function so you can see your UI in Android Studio's design panel.
 */
@Preview(showBackground = true)
@Composable
fun TriggerListScreenPreview() {
    SynapseAppTheme {
        TriggerListScreen(
            onStartListening = {},
            onStopListening = {},
            onAddTrigger = {}
        )
    }
}