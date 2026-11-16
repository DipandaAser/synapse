package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.service.triggers.Sms
import com.aserdipanda.synapse.service.triggers.TriggerRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Declared in the manifest, so Android starts Synapse for each incoming SMS, even when the app
 * isn't running: nothing has to stay in the background. Hands each text to [TriggerRunner],
 * which runs the triggers it matches. [SmsListener] turns this receiver off while paused.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // A long text arrives in parts, all in this one broadcast: check it as one message.
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)?.filterNotNull().orEmpty()
        if (parts.isEmpty()) return
        val sms = Sms(
            sender = parts.first().displayOriginatingAddress.orEmpty(),
            message = parts.joinToString(separator = "") { it.messageBody.orEmpty() },
            receivedAt = System.currentTimeMillis(),
        )

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TriggersRepository(DatabaseProvider.getDatabase(appContext))
                // Pausing turns this receiver off. The saved setting still has the last word.
                if (!repository.isSmsListenerEnabled()) return@launch

                val matched = TriggerRunner.runMatching(appContext, sms)
                Log.d(TAG, "SMS matched $matched trigger(s)")
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't check an SMS against the triggers", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsReceiver"
    }
}
