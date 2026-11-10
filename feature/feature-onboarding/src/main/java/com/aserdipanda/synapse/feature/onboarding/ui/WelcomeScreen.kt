package com.aserdipanda.synapse.feature.onboarding.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.components.RulePart
import com.aserdipanda.core.ui.components.RuleSentence
import com.aserdipanda.core.ui.components.SynapseLogo
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.onboarding.R

/**
 * First launch only: what Synapse does, a real trigger read as a sentence, then on to the
 * permission explanation.
 */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    OnboardingScaffold(
        onBack = null,
        actions = {
            LargeButton(text = stringResource(R.string.welcome_get_started), onClick = onGetStarted)
        },
    ) {
        SynapseLogo(contentDescription = stringResource(R.string.welcome_logo_description))
        ScreenTitle(
            title = stringResource(R.string.welcome_title),
            body = stringResource(R.string.welcome_body),
            titleStyle = MaterialTheme.typography.headlineMedium,
            bodyStyle = MaterialTheme.typography.bodyLarge,
        )
        ExampleTriggerCard()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FeatureRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.welcome_match_title),
                body = stringResource(R.string.welcome_match_body),
            )
            FeatureRow(
                icon = Icons.AutoMirrored.Outlined.Send,
                title = stringResource(R.string.welcome_webhook_title),
                body = stringResource(R.string.welcome_webhook_body),
            )
            FeatureRow(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.welcome_private_title),
                body = stringResource(R.string.welcome_private_body),
            )
        }
    }
}

// Reads like the trigger cards will, with the same component.
private val ExampleRule = listOf(
    RulePart.Words("When "),
    RulePart.Keyword("sender"),
    RulePart.Words(" is "),
    RulePart.Value("7777"),
    RulePart.Words(" and "),
    RulePart.Keyword("message"),
    RulePart.Words(" contains "),
    RulePart.Value("transaction"),
    RulePart.Words(", "),
    RulePart.Keyword("POST"),
    RulePart.Words(" to "),
    RulePart.Value("hooks.example.com/bank"),
)

@Composable
private fun ExampleTriggerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.welcome_example_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RuleSentence(parts = ExampleRule)
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconBadge(
            icon = icon,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 917)
@Preview(name = "Dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomeScreenPreview() {
    SynapseAppTheme(dynamicColor = false) {
        WelcomeScreen(onGetStarted = {})
    }
}
