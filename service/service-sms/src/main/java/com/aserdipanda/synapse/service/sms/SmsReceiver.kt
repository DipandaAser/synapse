package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.aserdipanda.synapse.core.network.NetworkModule
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SmsReceiver : BroadcastReceiver() {

    private val client by lazy { NetworkModule.provideOkHttpClient() }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                 val database = DatabaseProvider.getDatabase(context)
                val repository = TriggersRepository(database.triggerDao(), database.appSettingDao())
                
                val activeTriggers = repository.getActiveTriggers()
                Log.d("SmsReceiver", "Loaded ${activeTriggers.size} active triggers")
                
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (smsMessage in messages) {
                    val sender = smsMessage.displayOriginatingAddress ?: ""
                    val messageBody = smsMessage.messageBody

                    val matchingTriggers = activeTriggers.filter { trigger ->
                        matchesTrigger(sender, messageBody, trigger)
                    }

                    if (matchingTriggers.isNotEmpty()) {
                        Log.d("SmsReceiver", "MATCH FOUND! SMS from $sender matches ${matchingTriggers.size} trigger(s)")
                        
                        matchingTriggers.forEach { trigger ->
                            Log.d("SmsReceiver", "Processing trigger: ${trigger.name}")
                            
                            callWebhook(trigger.webhookUrl, sender, messageBody)
                        }
                    } else {
                        Log.d("SmsReceiver", "No matching triggers for SMS from $sender")
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun matchesTrigger(sender: String, message: String, trigger: TriggerEntity): Boolean {
        val senderMatches = sender.contains(trigger.senderPattern, ignoreCase = true)
        
        val messageMatches = trigger.messagePattern?.let { pattern ->
            message.contains(pattern, ignoreCase = true)
        } ?: true // If no message pattern, consider it a match
        
        return senderMatches && messageMatches
    }

    private fun callWebhook(webhookUrl: String, sender: String, message: String) {
        val json = """
            {
                "sender": "$sender",
                "message": "$message",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(webhookUrl)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("SmsReceiver", "Webhook call to $webhookUrl Failed: ${response.code} ${response.message}")
                } else {
                    Log.d("SmsReceiver", "Webhook call to $webhookUrl Successful: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Webhook call to $webhookUrl Exception: ${e.message}")
        }
    }
}
