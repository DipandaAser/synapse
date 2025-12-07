package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aserdipanda.synapse.core.common.Constants
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
                // Get repository to fetch active triggers
                val database = DatabaseProvider.getDatabase(context)
                val repository = TriggersRepository(database.triggerDao(), database.appSettingDao())
                
                // Fetch all active triggers
                val activeTriggers = repository.getActiveTriggers()
                Log.d("SmsReceiver", "Loaded ${activeTriggers.size} active triggers")
                
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (smsMessage in messages) {
                    val sender = smsMessage.displayOriginatingAddress ?: ""
                    val messageBody = smsMessage.messageBody

//                    val localIntent = Intent(Constants.ACTION_SMS_RECEIVED).apply {
//                        putExtra(Constants.EXTRA_SMS_SENDER, sender)
//                        putExtra(Constants.EXTRA_SMS_BODY, messageBody)
//                    }
//                    CoroutineScope(Dispatchers.Main).launch {
//                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
//                    }

                    // Check against all active triggers
                    val matchingTriggers = activeTriggers.filter { trigger ->
                        matchesTrigger(sender, messageBody, trigger)
                    }

                    if (matchingTriggers.isNotEmpty()) {
                        Log.d("SmsReceiver", "MATCH FOUND! SMS from $sender matches ${matchingTriggers.size} trigger(s)")
                        
                        matchingTriggers.forEach { trigger ->
                            Log.d("SmsReceiver", "Processing trigger: ${trigger.name}")
                            
                            // Call webhook URL for this trigger
                            callWebhook(trigger.webhookUrl, sender, messageBody)
                            
                            // Call each target phone number
                            trigger.targetPhoneNumbers.forEach { phoneNumber ->
                                callApi(phoneNumber, messageBody)
                            }
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
        // Check sender pattern
        val senderMatches = sender.contains(trigger.senderPattern, ignoreCase = true)
        
        // Check message pattern if provided
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

    private fun callApi(phoneNumber: String, message: String) {
        val url = "https://example.com"

        val formattedPhoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"

        val json = """
            {
                "phoneNumber":"$formattedPhoneNumber",
                "message": "$message"
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("SmsReceiver", "API Call to $phoneNumber Failed: ${response.code} ${response.message}")
                } else {
                    Log.d("SmsReceiver", "API Call to $phoneNumber Successful: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "API Call to $phoneNumber Exception: ${e.message}")
        }
    }
}
