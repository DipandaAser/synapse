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
        DatabaseProvider.getDatabase(applicationContext)
    }
    
    val triggersRepository: TriggersRepository by lazy {
        TriggersRepository(
            database.triggerDao(),
            database.conditionDao(),
            database.actionDao(),
            database.appSettingDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        database
    }
}
