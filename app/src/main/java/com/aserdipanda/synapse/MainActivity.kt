package com.aserdipanda.synapse

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import com.aserdipanda.synapse.feature.onboarding.OnboardingViewModel
import com.aserdipanda.synapse.feature.onboarding.OnboardingViewModelFactory
import com.aserdipanda.synapse.feature.onboarding.SmsAccess
import com.aserdipanda.synapse.feature.onboarding.isSmsPermissionGranted
import com.aserdipanda.synapse.feature.onboarding.smsAccess
import com.aserdipanda.synapse.feature.onboarding.ui.PermissionsScreen
import com.aserdipanda.synapse.feature.onboarding.ui.SmsBlockedScreen
import com.aserdipanda.synapse.feature.onboarding.ui.SmsDeniedScreen
import com.aserdipanda.synapse.feature.onboarding.ui.WelcomeScreen
import com.aserdipanda.synapse.feature.triggers.TriggersViewModel
import com.aserdipanda.synapse.feature.triggers.TriggersViewModelFactory
import com.aserdipanda.synapse.feature.triggers.ui.AddEditTriggerScreen
import com.aserdipanda.synapse.feature.triggers.ui.Trigger
import com.aserdipanda.synapse.feature.triggers.ui.TriggerListScreen
import com.aserdipanda.synapse.service.sms.SmsListenerService
import kotlinx.coroutines.launch
import com.aserdipanda.synapse.feature.onboarding.R as OnboardingR

private object Routes {
    const val WELCOME = "welcome"
    const val PERMISSIONS = "permissions"
    const val SMS_DENIED = "sms_denied"
    const val SMS_BLOCKED = "sms_blocked"
    const val HOME = "trigger_list"
    const val ADD_TRIGGER = "add_trigger"
    const val EDIT_TRIGGER = "edit_trigger/{triggerId}"
}

class MainActivity : ComponentActivity() {

    private val viewModel: TriggersViewModel by viewModels {
        TriggersViewModelFactory((application as SynapseApp).triggersRepository)
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory((application as SynapseApp).triggersRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynapseAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val onboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState()
                    // Waits for the saved value, so returning users never see the welcome screen.
                    onboardingCompleted?.let { completed ->
                        val startDestination = remember { if (completed) Routes.HOME else Routes.WELCOME }
                        SynapseNavHost(startDestination)
                    }
                }
            }
        }
    }

    @Composable
    private fun SynapseNavHost(startDestination: String) {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val smsAllowedMessage = stringResource(OnboardingR.string.sms_access_allowed)

        // Collect state from ViewModels
        val isListenerEnabled by viewModel.isSmsListenerEnabled.collectAsState()
        val triggers by viewModel.triggers.collectAsState()
        val hasRequestedSms by onboardingViewModel.hasRequestedSmsPermission.collectAsState()

        // Android decides whether Synapse can listen, and the user can revoke SMS access in
        // Settings at any time, so check again whenever the app comes back.
        var smsGranted by remember { mutableStateOf(isSmsPermissionGranted()) }
        LifecycleResumeEffect(Unit) {
            smsGranted = isSmsPermissionGranted()
            onPauseOrDispose { }
        }

        // Listening is on and allowed, but Android stopped the service (after an app update,
        // say) or access was just allowed in Settings: start it, so "on" stays true.
        LaunchedEffect(isListenerEnabled, smsGranted) {
            if (isListenerEnabled && smsGranted && !SmsListenerService.isRunning) {
                startSmsListenerService(showToast = false)
            }
        }

        // Leaves onboarding or the permission screens for Home, with nothing to go back to.
        // Whatever happened with SMS access, onboarding is then done.
        fun goHome() {
            onboardingViewModel.completeOnboarding()
            navController.navigate(Routes.HOME) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }

        fun finishWithSmsGranted(showToast: Boolean = true) {
            smsGranted = true
            startListening(showToast)
            goHome()
        }

        fun onSmsResult(access: SmsAccess) {
            onboardingViewModel.onSmsPermissionRequested()
            when (access) {
                SmsAccess.Granted -> finishWithSmsGranted()
                SmsAccess.Denied -> navController.navigate(Routes.SMS_DENIED) { launchSingleTop = true }
                SmsAccess.Blocked -> navController.navigate(Routes.SMS_BLOCKED) {
                    popUpTo(Routes.SMS_DENIED) { inclusive = true }
                    launchSingleTop = true
                }
                // Can't happen right after a request.
                SmsAccess.NotAsked -> Unit
            }
        }

        val uiTriggers = triggers.map { entity ->
            Trigger(
                id = entity.trigger.id.toInt(),
                name = entity.trigger.name,
                sender = entity.conditions.firstOrNull { it.field == "sender" }?.value ?: "",
                contains = entity.conditions.firstOrNull { it.field == "message_body" }?.value ?: "",
                url = entity.actions.firstOrNull { it.type == "WEBHOOK" }
                    ?.let { WebhookConfig.fromArg(it.arg)?.url } ?: "",
                lastTriggered = null,
                isEnabled = entity.trigger.enabled
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onGetStarted = {
                        if (isSmsPermissionGranted()) {
                            goHome()
                        } else {
                            navController.navigate(Routes.PERMISSIONS)
                        }
                    }
                )
            }

            composable(Routes.PERMISSIONS) {
                PermissionsScreen(
                    onBack = { navController.navigateUp() },
                    onResult = ::onSmsResult,
                    onNotNow = ::goHome
                )
            }

            composable(Routes.SMS_DENIED) {
                SmsDeniedScreen(
                    onBack = { navController.navigateUp() },
                    onResult = ::onSmsResult,
                    onNotNow = ::goHome
                )
            }

            composable(Routes.SMS_BLOCKED) {
                SmsBlockedScreen(
                    onBack = { navController.navigateUp() },
                    onGranted = {
                        onboardingViewModel.onSmsPermissionRequested()
                        // The snackbar says it, so no toast on top of it.
                        finishWithSmsGranted(showToast = false)
                        scope.launch { snackbarHostState.showSnackbar(smsAllowedMessage) }
                    },
                    onNotNow = ::goHome
                )
            }

            composable(Routes.HOME) {
                TriggerListScreen(
                    // Never claims to listen without SMS access, whatever the saved setting.
                    isServiceActive = isListenerEnabled && smsGranted,
                    triggers = uiTriggers,
                    snackbarHostState = snackbarHostState,
                    onToggleService = { isEnabled ->
                        if (!isEnabled) {
                            stopListening()
                        } else {
                            when (smsAccess(askedBefore = hasRequestedSms)) {
                                SmsAccess.Granted -> startListening()
                                SmsAccess.NotAsked -> navController.navigate(Routes.PERMISSIONS)
                                SmsAccess.Denied -> navController.navigate(Routes.SMS_DENIED)
                                SmsAccess.Blocked -> navController.navigate(Routes.SMS_BLOCKED)
                            }
                        }
                    },
                    onAddTrigger = {
                        navController.navigate(Routes.ADD_TRIGGER)
                    },
                    onEditTrigger = { trigger ->
                        navController.navigate("edit_trigger/${trigger.id}")
                    },
                    onToggleTrigger = { trigger, isEnabled ->
                        viewModel.toggleTriggerStatus(trigger.id.toLong(), isEnabled)
                        Toast.makeText(this@MainActivity, "${trigger.name} toggled to $isEnabled", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            composable(Routes.ADD_TRIGGER) {
                AddEditTriggerScreen(
                    onBack = { navController.navigateUp() },
                    onSave = { newTrigger, newSenderCondition, newMessageCondition, newAction ->
                        viewModel.createTriggerWithRelations(newTrigger.name, newTrigger.enabled,
                            listOfNotNull(newSenderCondition, newMessageCondition), listOf(newAction))
                        Toast.makeText(this@MainActivity, "Trigger saved", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.EDIT_TRIGGER,
                arguments = listOf(navArgument("triggerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val triggerId = backStackEntry.arguments?.getInt("triggerId")?.toLong()
                val triggerToEdit = triggers.find { it.trigger.id == triggerId }

                // The editor reads its initial values once, so wait until the saved
                // trigger has loaded. Otherwise it would start from defaults and saving
                // would overwrite the stored conditions and webhook.
                if (triggerToEdit != null) {
                    AddEditTriggerScreen(
                        trigger = triggerToEdit.trigger,
                        senderCondition = triggerToEdit.conditions.firstOrNull { it.field == "sender" },
                        messageCondition = triggerToEdit.conditions.firstOrNull { it.field == "message_body" },
                        actionEntity = triggerToEdit.actions.firstOrNull { it.type == "WEBHOOK" },
                        onBack = { navController.navigateUp() },
                        onSave = { updatedTrigger, updatedSenderCondition, updatedMessageCondition, action ->
                            viewModel.updateTriggerWithRelations(updatedTrigger, listOfNotNull(updatedSenderCondition, updatedMessageCondition), listOf(action))
                            Toast.makeText(this@MainActivity, "Trigger updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }

    // Only called with SMS access granted, so the saved setting never says "on" while
    // nothing can run.
    private fun startListening(showToast: Boolean = true) {
        startSmsListenerService(showToast)
        viewModel.setSmsListenerEnabled(true)
    }

    private fun stopListening() {
        stopSmsListenerService()
        viewModel.setSmsListenerEnabled(false)
    }

    private fun startSmsListenerService(showToast: Boolean) {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        if (showToast) {
            Toast.makeText(this, "SMS Listener started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopSmsListenerService() {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "SMS Listener stopped", Toast.LENGTH_SHORT).show()
    }
}
