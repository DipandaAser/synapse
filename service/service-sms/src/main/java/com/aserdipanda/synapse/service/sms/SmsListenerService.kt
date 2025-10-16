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
import androidx.core.app.NotificationCompat
import com.aserdipanda.synapse.core.common.Constants

class SmsListenerService : Service() {

    private val smsReceiver = SmsReceiver()

    companion object {
        var isRunning = false
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
            .setContentTitle("SMS Listener Active")
            .setContentText("Listening for incoming SMS messages.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(Constants.SMS_LISTENER_NOTIFICATION_ID, notification)

        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterReceiver(smsReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.SMS_LISTENER_CHANNEL_ID,
                "SMS Listener Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
