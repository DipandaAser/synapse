package com.aserdipanda.synapse.feature.triggers.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.components.SynapseGlyph
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.triggers.R
import com.aserdipanda.synapse.feature.triggers.TriggerTemplate

/**
 * First run, or after the last trigger is deleted: what a trigger is, and examples to start from.
 * The New trigger button stays the main action; the examples are a shortcut.
 */
@Composable
fun NoTriggersYet(onUseTemplate: (TriggerTemplate) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    // Full width, so the content centers on the screen rather than at the start.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Decorative: the headline says it all.
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            SynapseGlyph(
                lineColor = colors.onPrimaryContainer,
                smsColor = colors.tertiary,
                modifier = Modifier.size(56.dp),
            )
        }
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp),
        )
        Text(
            text = stringResource(R.string.empty_examples_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TriggerTemplate.entries.forEach { template ->
                SuggestionChip(
                    onClick = { onUseTemplate(template) },
                    label = { Text(stringResource(template.title)) },
                    icon = {
                        Icon(
                            imageVector = template.icon,
                            contentDescription = null,
                            modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                        )
                    },
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoTriggersYetPreview() {
    SynapseAppTheme(dynamicColor = false) {
        Surface {
            NoTriggersYet(onUseTemplate = {}, modifier = Modifier.padding(vertical = 24.dp))
        }
    }
}
