package com.aserdipanda.synapse.feature.onboarding.ui

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.onboarding.R
import com.aserdipanda.synapse.feature.onboarding.SmsAccess
import com.aserdipanda.synapse.feature.onboarding.areNotificationsAllowed
import com.aserdipanda.synapse.feature.onboarding.asksForNotifications
import com.aserdipanda.synapse.feature.onboarding.smsAccess

/**
 * Explains the permissions right before Android asks for them. Continue asks for SMS, then for
 * notifications on Android 13 and later, and reports what happened to SMS access.
 */
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onResult: (SmsAccess) -> Unit,
    onNotNow: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)

    // Read from Android once both prompts are answered, so it holds even if the app was
    // recreated between them.
    fun reportSmsAccess() {
        currentOnResult(activity?.smsAccess(askedBefore = true) ?: SmsAccess.Denied)
    }

    // Notifications are optional: whatever the answer, report what happened to SMS.
    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { reportSmsAccess() }

    // One prompt at a time, in the order the screen lists them. Asked together, Android
    // would show the notification prompt first.
    val requestSms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (asksForNotifications && !context.areNotificationsAllowed()) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            reportSmsAccess()
        }
    }

    PermissionsContent(
        asksForNotifications = asksForNotifications,
        onBack = onBack,
        onContinue = { requestSms.launch(Manifest.permission.RECEIVE_SMS) },
        onNotNow = onNotNow,
    )
}

@Composable
private fun PermissionsContent(
    asksForNotifications: Boolean,
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
            title = stringResource(
                if (asksForNotifications) R.string.permissions_title_both else R.string.permissions_title_sms
            ),
            body = stringResource(
                if (asksForNotifications) R.string.permissions_body_both else R.string.permissions_body_sms
            ),
        )
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PermissionRow(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = stringResource(R.string.onboarding_sms),
                    body = stringResource(R.string.permissions_sms_body),
                )
                if (asksForNotifications) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    PermissionRow(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.onboarding_notifications),
                        tag = stringResource(R.string.permissions_notifications_tag),
                        body = stringResource(R.string.permissions_notifications_body),
                    )
                }
            }
        }
        InfoNote(
            text = stringResource(
                if (asksForNotifications) R.string.permissions_note_both else R.string.permissions_note_sms
            )
        )
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, title: String, body: String, tag: String? = null) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (tag != null) Tag(text = tag)
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Android 13+", showBackground = true, heightDp = 917)
@Preview(name = "Dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionsPreview() {
    SynapseAppTheme(dynamicColor = false) {
        PermissionsContent(asksForNotifications = true, onBack = {}, onContinue = {}, onNotNow = {})
    }
}

@Preview(name = "Android 12 and earlier", showBackground = true, heightDp = 917)
@Composable
private fun PermissionsSmsOnlyPreview() {
    SynapseAppTheme(dynamicColor = false) {
        PermissionsContent(asksForNotifications = false, onBack = {}, onContinue = {}, onNotNow = {})
    }
}
