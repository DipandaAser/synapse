package com.aserdipanda.synapse.service.sms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.AppSettingEntity
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SmsListenerService : Service() {

    private val smsReceiver = SmsReceiver()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        var isRunning = false
        private const val TAG = "SmsListenerService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Note: You'll need to create a proper PendingIntent that points to MainActivity
        // This is a placeholder
        val notificationIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, Constants.SMS_LISTENER_CHANNEL_ID)
            .setContentTitle("Synapse Listener Active")
            .setContentText("Listening for incoming SMS messages.")
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(Constants.SMS_LISTENER_NOTIFICATION_ID, notification)

        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called")
        isRunning = true
        // Save state to database
        serviceScope.launch {
            try {
                Log.d(TAG, "Getting database instance...")
                val database = DatabaseProvider.getDatabase(applicationContext)
                Log.d(TAG, "Database instance obtained, saving setting...")
                val setting = AppSettingEntity(
                    key = Constants.SETTING_KEY_SMS_LISTENER_ENABLED,
                    value = true.toString()
                )
                database.appSettingDao().upsert(setting)
                Log.d(TAG, "Setting saved: SMS_LISTENER_ENABLED = true")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving setting to database", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy() called")
        isRunning = false
        unregisterReceiver(smsReceiver)

        // Save state to database
        serviceScope.launch {
            try {
                Log.d(TAG, "Getting database instance for cleanup...")
                val database = DatabaseProvider.getDatabase(applicationContext)
                Log.d(TAG, "Database instance obtained, saving setting...")
                val setting = AppSettingEntity(
                    key = Constants.SETTING_KEY_SMS_LISTENER_ENABLED,
                    value = false.toString()
                )
                database.appSettingDao().upsert(setting)
                Log.d(TAG, "Setting saved: SMS_LISTENER_ENABLED = false")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving setting to database", e)
            }
        }

        serviceScope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.SMS_LISTENER_CHANNEL_ID,
                "Synapse Listener Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
