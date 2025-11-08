package com.aserdipanda.synapse

import android.app.Application
import android.util.Log
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.data.triggers.local.TriggerDatabase

class SynapseApp : Application() {
    
    companion object {
        private const val TAG = "SynapseApp"
    }

    val database: TriggerDatabase by lazy {
        Log.d(TAG, "Initializing database...")
        DatabaseProvider.getDatabase(applicationContext)
    }
    
    // Repository instance
    val triggersRepository: TriggersRepository by lazy {
        Log.d(TAG, "Initializing repository...")
        TriggersRepository(database.triggerDao(), database.appSettingDao())
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SynapseApp onCreate() called")
        // Initialize database eagerly to ensure it's created on first launch
        database
        Log.d(TAG, "Database initialization triggered")
    }
}
