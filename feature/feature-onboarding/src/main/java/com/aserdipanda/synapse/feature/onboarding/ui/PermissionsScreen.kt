package com.aserdipanda.synapse.feature.onboarding.ui

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.onboarding.R
import com.aserdipanda.synapse.feature.onboarding.SmsAccess
import com.aserdipanda.synapse.feature.onboarding.smsAccess

/** Explains SMS access right before Android asks for it. Continue asks, then reports the answer. */
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onResult: (SmsAccess) -> Unit,
    onNotNow: () -> Unit,
) {
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)

    // The prompt only says whether access was granted. Asking Android again also tells a
    // denial apart from a block, once it won't show the prompt anymore.
    val requestSms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        currentOnResult(activity?.smsAccess(askedBefore = true) ?: SmsAccess.Denied)
    }

    PermissionsContent(
        onBack = onBack,
        onContinue = { requestSms.launch(Manifest.permission.RECEIVE_SMS) },
        onNotNow = onNotNow,
    )
}

@Composable
private fun PermissionsContent(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onNotNow: () -> Unit,
) {
    OnboardingScaffold(
        onBack = onBack,
        actions = {
            LargeButton(text = stringResource(R.string.permissions_continue), onClick = onContinue)
            NotNowButton(onClick = onNotNow)
        },
    ) {
        ScreenTitle(
            title = stringResource(R.string.permissions_title),
            body = stringResource(R.string.permissions_body),
        )
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PermissionRow(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = stringResource(R.string.onboarding_sms),
                    body = stringResource(R.string.permissions_sms_body),
                )
            }
        }
        InfoNote(text = stringResource(R.string.permissions_note))
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, title: String, body: String) {
    CardRow(
        alignTop = true,
        leading = {
            IconBadge(
                icon = icon,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 917)
@Preview(name = "Dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionsPreview() {
    SynapseAppTheme(dynamicColor = false) {
        PermissionsContent(onBack = {}, onContinue = {}, onNotNow = {})
    }
}
