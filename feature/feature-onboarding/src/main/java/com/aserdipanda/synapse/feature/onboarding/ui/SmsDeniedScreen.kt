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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.onboarding.R
import com.aserdipanda.synapse.feature.onboarding.SmsAccess
import com.aserdipanda.synapse.feature.onboarding.isSmsPermissionGranted
import com.aserdipanda.synapse.feature.onboarding.shouldShowSmsRationale
import com.aserdipanda.synapse.feature.onboarding.smsAccessOf

/**
 * SMS access was denied once. Android still shows its prompt, so the main action asks again.
 * Reports [SmsAccess.Granted] at most once, including when access is allowed from Settings.
 */
@Composable
fun SmsDeniedScreen(
    onBack: () -> Unit,
    onResult: (SmsAccess) -> Unit,
    onNotNow: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)
    var grantedReported by remember { mutableStateOf(false) }

    fun report(access: SmsAccess) {
        if (grantedReported) return
        if (access == SmsAccess.Granted) grantedReported = true
        currentOnResult(access)
    }

    val requestSms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        report(
            smsAccessOf(
                isGranted = granted,
                shouldShowRationale = activity?.shouldShowSmsRationale() ?: true,
                askedBefore = true,
            )
        )
    }

    // Access may also change in Settings while Synapse is in the background.
    LifecycleResumeEffect(Unit) {
        if (context.isSmsPermissionGranted()) report(SmsAccess.Granted)
        onPauseOrDispose { }
    }

    SmsDeniedContent(
        onBack = onBack,
        onAllow = { requestSms.launch(Manifest.permission.RECEIVE_SMS) },
        onNotNow = onNotNow,
    )
}

@Composable
private fun SmsDeniedContent(
    onBack: () -> Unit,
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
) {
    OnboardingScaffold(
        onBack = onBack,
        actions = {
            LargeButton(text = stringResource(R.string.sms_denied_allow), onClick = onAllow)
            NotNowButton(onClick = onNotNow)
        },
    ) {
        ErrorHeaderBadge(icon = Icons.Outlined.CommentsDisabled)
        ScreenTitle(
            title = stringResource(R.string.sms_denied_title),
            body = stringResource(R.string.sms_denied_body),
        )
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PermissionStatusRow(title = stringResource(R.string.onboarding_sms), allowed = false)
            }
        }
    }
}

/** "Allowed" or "Not allowed" in text, icon and color together, never color alone. */
@Composable
private fun PermissionStatusRow(title: String, allowed: Boolean) {
    val colors = MaterialTheme.colorScheme
    CardRow(
        trailing = {
            Icon(
                imageVector = if (allowed) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = if (allowed) colors.primary else colors.error,
            )
        },
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(if (allowed) R.string.onboarding_allowed else R.string.onboarding_not_allowed),
            style = MaterialTheme.typography.bodyMedium,
            color = if (allowed) colors.onSurfaceVariant else colors.error,
        )
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 917)
@Preview(name = "Dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmsDeniedPreview() {
    SynapseAppTheme(dynamicColor = false) {
        SmsDeniedContent(onBack = {}, onAllow = {}, onNotNow = {})
    }
}
