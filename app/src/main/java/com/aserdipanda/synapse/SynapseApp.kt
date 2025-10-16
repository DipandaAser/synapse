package com.aserdipanda.synapse

import android.app.Application
import androidx.room.Room
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.TriggerDatabase

class SynapseApp : Application() {
    
    // Database instance
    val database: TriggerDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TriggerDatabase::class.java,
            "synapse_database"
        ).build()
    }
    
    // Repository instance
    val triggersRepository: TriggersRepository by lazy {
        TriggersRepository(database.triggerDao())
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-level components here
    }
}
