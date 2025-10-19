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
import com.aserdipanda.synapse.feature.triggers.ui.TriggerListScreen
import com.aserdipanda.synapse.service.sms.SmsListenerService

class MainActivity : ComponentActivity() {

    // A modern way to request permissions
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
                TriggerListScreen(
                    onStartListening = {},
                    onStopListening = {},
                    onAddTrigger = {}
                )
                /*SmsListenerScreen(
                    onStartService = {
                        checkAndStartService()
                    },
                    onStopService = {
                        stopSmsListenerService()
                    }
                )*/
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

@Composable
fun SmsListenerScreen(onStartService: () -> Unit, onStopService: () -> Unit) {
    // State variables that Compose will automatically observe for changes
    var serviceStatus by remember { mutableStateOf("Checking status...") }
    var lastMessage by remember { mutableStateOf("No message received yet.") }
    var isRunning by remember { mutableStateOf(SmsListenerService.isRunning) }
    val context = LocalContext.current

    // This effect runs when the composable enters the screen
    // and cleans up when it leaves.
    DisposableEffect(Unit) {
        val messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val sender = intent?.getStringExtra(Constants.EXTRA_SMS_SENDER) ?: "Unknown"
                val body = intent?.getStringExtra(Constants.EXTRA_SMS_BODY) ?: "No content"
                lastMessage = "From: $sender\n\n$body"
            }
        }
        // Register the receiver
        LocalBroadcastManager.getInstance(context).registerReceiver(
            messageReceiver, IntentFilter(Constants.ACTION_SMS_RECEIVED)
        )
        // This block is called when the composable is disposed (e.g., screen navigates away)
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(messageReceiver)
        }
    }

    // This will update the UI based on the service's static flag
    LaunchedEffect(SmsListenerService.isRunning) {
        serviceStatus = if (SmsListenerService.isRunning) {
            "Service is running"
        } else {
            "Service is stopped"
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = serviceStatus,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Last Received Message:")
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lastMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .background(Color(0xFFF1F1F1))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(enabled = !isRunning, onClick = onStartService) {

                    Text("Start Listening")
                }
                Button(enabled = isRunning, onClick = onStopService) {
                    Text("Stop Listening")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SynapseAppTheme {
        SmsListenerScreen(onStartService = {}, onStopService = {})
    }
}
