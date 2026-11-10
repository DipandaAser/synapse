package com.aserdipanda.synapse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import com.aserdipanda.synapse.feature.triggers.TriggersViewModel
import com.aserdipanda.synapse.feature.triggers.TriggersViewModelFactory
import com.aserdipanda.synapse.feature.triggers.ui.AddEditTriggerScreen
import com.aserdipanda.synapse.feature.triggers.ui.Trigger
import com.aserdipanda.synapse.feature.triggers.ui.TriggerListScreen
import com.aserdipanda.synapse.service.sms.SmsListenerService

class MainActivity : ComponentActivity() {

    private val viewModel: TriggersViewModel by viewModels {
        TriggersViewModelFactory((application as SynapseApp).triggersRepository)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startSmsListenerService()
            } else {
                Toast.makeText(this, "Permissions are required to run the service", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynapseAppTheme {
                val navController = rememberNavController()
                
                // Collect state from ViewModel
                val isServiceActive by viewModel.isSmsListenerEnabled.collectAsState()
                val triggers by viewModel.triggers.collectAsState()

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
                    startDestination = "trigger_list"
                ) {
                    composable("trigger_list") {
                        TriggerListScreen(
                            isServiceActive = isServiceActive,
                            triggers = uiTriggers,
                            onToggleService = { isEnabled ->
                                if (isEnabled) {
                                    checkAndStartService()
                                } else {
                                    stopSmsListenerService()
                                }
                                viewModel.setSmsListenerEnabled(isEnabled)
                            },
                            onAddTrigger = {
                                navController.navigate("add_trigger")
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
                    
                    composable("add_trigger") {
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
                        route = "edit_trigger/{triggerId}",
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
        }
    }

    private fun checkAndStartService() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startSmsListenerService()
        }
    }

    private fun startSmsListenerService() {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "SMS Listener started", Toast.LENGTH_SHORT).show()
    }

    private fun stopSmsListenerService() {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "SMS Listener stopped", Toast.LENGTH_SHORT).show()
    }
}
