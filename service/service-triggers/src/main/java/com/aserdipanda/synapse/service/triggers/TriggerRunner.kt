package com.aserdipanda.synapse.service.triggers

import android.content.Context
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider

/** Runs the triggers that incoming texts match. */
object TriggerRunner {

    /**
     * Queues a run of each active trigger that [sms] matches, and returns how many matched.
     * Each run starts in WorkManager once there's a network.
     */
    suspend fun runMatching(context: Context, sms: Sms): Int {
        val repository = TriggersRepository(DatabaseProvider.getDatabase(context))
        val matches = repository.getActiveTriggersWithRelations()
            .filter { TriggerMatcher.matches(it.conditions, sms) }
        matches.forEach { TriggerWorker.enqueue(context, it.trigger.id, sms) }
        return matches.size
    }
}
