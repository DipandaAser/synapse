package com.aserdipanda.synapse.feature.triggers.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.triggers.R

/** What the listener can do right now, as Home shows it. */
enum class ListenerStatus {
    /** Listening for SMS. */
    Listening,

    /** Turned off by the user. A choice, not an error. */
    Paused,

    /** SMS access is missing, whatever the saved setting: Synapse can't listen. */
    NeedsSmsAccess,
}

/**
 * Answers "is Synapse listening?" before the list starts. Listening and paused are one toggle
 * row; without SMS access, the switch gives way to the one action that helps.
 */
@Composable
fun ListenerStatusCard(
    status: ListenerStatus,
    triggerCount: Int,
    enabledTriggerCount: Int,
    onListeningChange: (Boolean) -> Unit,
    onAllowSmsAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (status) {
        ListenerStatus.Listening -> colors.primaryContainer to colors.onPrimaryContainer
        ListenerStatus.Paused -> colors.surfaceContainerHighest to colors.onSurface
        ListenerStatus.NeedsSmsAccess -> colors.errorContainer to colors.onErrorContainer
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        when (status) {
            ListenerStatus.Listening, ListenerStatus.Paused -> ListeningRow(
                listening = status == ListenerStatus.Listening,
                triggerCount = triggerCount,
                enabledTriggerCount = enabledTriggerCount,
                onListeningChange = onListeningChange,
            )
            ListenerStatus.NeedsSmsAccess -> NeedsSmsAccess(
                triggerCount = triggerCount,
                onAllowSmsAccess = onAllowSmsAccess,
            )
        }
    }
}

// The whole row toggles, so TalkBack reads it as one switch with its explanation.
@Composable
private fun ListeningRow(
    listening: Boolean,
    triggerCount: Int,
    enabledTriggerCount: Int,
    onListeningChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = listening, role = Role.Switch, onValueChange = onListeningChange)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (listening) {
            StatusDisc(Icons.Outlined.Sensors, colors.primary, colors.onPrimary)
        } else {
            StatusDisc(Icons.Outlined.PauseCircle, colors.surface, colors.onSurfaceVariant)
        }
        StatusText(
            title = stringResource(if (listening) R.string.listener_on_title else R.string.listener_paused_title),
            body = when {
                listening && triggerCount == 0 -> stringResource(R.string.listener_on_no_triggers)
                listening -> pluralStringResource(
                    R.plurals.listener_on_triggers, triggerCount, enabledTriggerCount, triggerCount
                )
                enabledTriggerCount == 0 -> stringResource(R.string.listener_paused_body)
                else -> pluralStringResource(
                    R.plurals.listener_paused_triggers, enabledTriggerCount, enabledTriggerCount
                )
            },
            bodyColor = if (listening) LocalContentColor.current else colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = listening, onCheckedChange = null)
    }
}

@Composable
private fun NeedsSmsAccess(triggerCount: Int, onAllowSmsAccess: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDisc(Icons.Outlined.Warning, colors.error, colors.onError)
            StatusText(
                title = stringResource(R.string.listener_no_access_title),
                body = if (triggerCount == 0) {
                    stringResource(R.string.listener_no_access_body)
                } else {
                    pluralStringResource(R.plurals.listener_no_access_triggers, triggerCount, triggerCount)
                },
                bodyColor = LocalContentColor.current,
                titleIsHeading = true,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = onAllowSmsAccess,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
        ) {
            Text(stringResource(R.string.listener_allow_sms))
        }
    }
}

@Composable
private fun StatusDisc(icon: ImageVector, containerColor: Color, contentColor: Color) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}

@Composable
private fun StatusText(
    title: String,
    body: String,
    bodyColor: Color,
    modifier: Modifier = Modifier,
    titleIsHeading: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = if (titleIsHeading) Modifier.semantics { heading() } else Modifier,
        )
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = bodyColor)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListenerStatusCardPreview() {
    SynapseAppTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ListenerStatus.entries.forEach { status ->
                    ListenerStatusCard(
                        status = status,
                        triggerCount = 4,
                        enabledTriggerCount = 3,
                        onListeningChange = {},
                        onAllowSmsAccess = {},
                    )
                }
            }
        }
    }
}
