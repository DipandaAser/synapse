package com.aserdipanda.synapse

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.data.triggers.local.TriggerDatabase

class SynapseApp : Application() {
    
    companion object {
        private const val TAG = "SynapseApp"
        private const val OLD_LISTENER_CHANNEL_ID = "SynapseSmsListenerServiceChannel"
    }

    val database: TriggerDatabase by lazy {
        DatabaseProvider.getDatabase(applicationContext)
    }
    
    val triggersRepository: TriggersRepository by lazy {
        TriggersRepository(database)
    }
    
    override fun onCreate() {
        super.onCreate()
        database
        // Synapse used to listen from a foreground service. Delete that service's notification
        // channel, which would otherwise stay listed in Synapse's notification settings.
        NotificationManagerCompat.from(this).deleteNotificationChannel(OLD_LISTENER_CHANNEL_ID)
    }
}
