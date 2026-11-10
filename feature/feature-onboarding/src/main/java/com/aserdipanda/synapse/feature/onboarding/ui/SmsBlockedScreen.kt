package com.aserdipanda.synapse.feature.onboarding.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.feature.onboarding.R
import com.aserdipanda.synapse.feature.onboarding.isSmsPermissionGranted
import com.aserdipanda.synapse.feature.onboarding.openAppSettings

/**
 * SMS access was denied twice, so Android won't ask again. The app's settings page is the only
 * way back. Calls [onGranted] once, when Synapse comes back with access allowed.
 */
@Composable
fun SmsBlockedScreen(
    onBack: () -> Unit,
    onGranted: () -> Unit,
    onNotNow: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnGranted by rememberUpdatedState(onGranted)
    var grantedReported by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (!grantedReported && context.isSmsPermissionGranted()) {
            grantedReported = true
            currentOnGranted()
        }
        onPauseOrDispose { }
    }

    SmsBlockedContent(
        onBack = onBack,
        onOpenSettings = { context.openAppSettings() },
        onNotNow = onNotNow,
    )
}

@Composable
private fun SmsBlockedContent(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onNotNow: () -> Unit,
) {
    OnboardingScaffold(
        onBack = onBack,
        actions = {
            LargeButton(
                text = stringResource(R.string.sms_blocked_open_settings),
                onClick = onOpenSettings,
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
            )
            NotNowButton(onClick = onNotNow)
        },
    ) {
        ErrorHeaderBadge(icon = Icons.Outlined.Tune)
        ScreenTitle(
            title = stringResource(R.string.sms_blocked_title),
            body = stringResource(R.string.sms_blocked_body),
        )
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                StepRow(number = 1, html = stringResource(R.string.sms_blocked_step_open))
                StepRow(number = 2, html = stringResource(R.string.sms_blocked_step_permissions))
                StepRow(number = 3, html = stringResource(R.string.sms_blocked_step_allow))
            }
        }
        InfoNote(text = stringResource(R.string.sms_blocked_note))
    }
}

@Composable
private fun StepRow(number: Int, html: String) {
    CardRow(
        leading = {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    )
                }
            }
        },
    ) {
        Text(text = mediumWeightHtml(html), style = MaterialTheme.typography.bodyLarge)
    }
}

// The names of Android's buttons are in <b> in the strings. The design sets them in medium
// weight, which reads better than bold at body size.
private fun mediumWeightHtml(html: String): AnnotatedString {
    val parsed = AnnotatedString.fromHtml(html)
    return buildAnnotatedString {
        append(parsed.text)
        parsed.spanStyles.forEach { span ->
            val style = if (span.item.fontWeight == FontWeight.Bold) {
                span.item.copy(fontWeight = FontWeight.Medium)
            } else {
                span.item
            }
            addStyle(style, span.start, span.end)
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 917)
@Preview(name = "Dark", showBackground = true, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmsBlockedPreview() {
    SynapseAppTheme(dynamicColor = false) {
        SmsBlockedContent(onBack = {}, onOpenSettings = {}, onNotNow = {})
    }
}
