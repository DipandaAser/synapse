package com.aserdipanda.synapse

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aserdipanda.core.ui.theme.SynapseAppTheme
import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import com.aserdipanda.synapse.feature.triggers.TriggersViewModel
import com.aserdipanda.synapse.feature.triggers.TriggersViewModelFactory
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
        setContent {
            SynapseAppTheme {
                // Collect state from ViewModel
                val isServiceActive by viewModel.isSmsListenerEnabled.collectAsState()
                val triggers by viewModel.triggers.collectAsState()
                
                // Navigation state
                var showAddTriggerScreen by remember { mutableStateOf(false) }
                var triggerToEdit by remember { mutableStateOf<TriggerEntity?>(null) }

                // Convert TriggerEntity to UI Trigger model
                val uiTriggers = triggers.map { entity ->
                    Trigger(
                        id = entity.id.toInt(),
                        name = entity.name,
                        sender = entity.senderPattern,
                        contains = entity.messagePattern ?: "",
                        url = entity.webhookUrl,
                        lastTriggered = null, // TODO: Add lastTriggered field to TriggerEntity
                        isEnabled = entity.isActive
                    )
                }

                if (showAddTriggerScreen) {
                    com.aserdipanda.synapse.feature.triggers.ui.AddEditTriggerScreen(
                        trigger = triggerToEdit,
                        onSave = { newTrigger ->
                            if (triggerToEdit == null) {
                                viewModel.addTrigger(newTrigger)
                            } else {
                                viewModel.updateTrigger(newTrigger)
                            }
                            showAddTriggerScreen = false
                            triggerToEdit = null
                            Toast.makeText(this, "Trigger saved", Toast.LENGTH_SHORT).show()
                        },
                        onCancel = {
                            showAddTriggerScreen = false
                            triggerToEdit = null
                        }
                    )
                } else {
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
                            triggerToEdit = null
                            showAddTriggerScreen = true
                        },
                        onEditTrigger = { trigger ->
                            triggerToEdit = triggers.find { it.id == trigger.id.toLong() }
                            showAddTriggerScreen = true
                        },
                        onToggleTrigger = { trigger, isEnabled ->
                            viewModel.toggleTriggerStatus(trigger.id.toLong(), isEnabled)
                            Toast.makeText(this, "${trigger.name} toggled to $isEnabled", Toast.LENGTH_SHORT).show()
                        }
                    )
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
