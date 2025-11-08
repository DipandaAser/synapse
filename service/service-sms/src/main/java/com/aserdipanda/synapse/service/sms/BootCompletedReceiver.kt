package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "Boot completed, checking SMS Listener state.")

            // Use goAsync to allow asynchronous work
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    val database = DatabaseProvider.getDatabase(context.applicationContext)
                    val setting = database.appSettingDao().getSetting(Constants.SETTING_KEY_SMS_LISTENER_ENABLED)
                    val isEnabled = setting?.value?.toBoolean() ?: false

                    if (isEnabled) {
                        Log.d("BootCompletedReceiver", "SMS Listener was enabled, starting service.")
                        val serviceIntent = Intent(context, SmsListenerService::class.java)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    } else {
                        Log.d("BootCompletedReceiver", "SMS Listener was disabled, not starting service.")
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
