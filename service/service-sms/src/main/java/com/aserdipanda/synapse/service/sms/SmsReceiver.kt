package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.aserdipanda.synapse.core.network.NetworkModule
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.data.triggers.local.Operator
import com.aserdipanda.synapse.data.triggers.local.TriggerWithRelations
import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SmsReceiver : BroadcastReceiver() {

    private val client by lazy { NetworkModule.provideOkHttpClient() }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TriggersRepository(DatabaseProvider.getDatabase(context))
                
                val activeTriggersWithRelations = repository.getActiveTriggersWithRelations()
                Log.d("SmsReceiver", "Loaded ${activeTriggersWithRelations.size} active triggers")
                
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (smsMessage in messages) {
                    val sender = smsMessage.displayOriginatingAddress ?: ""
                    val messageBody = smsMessage.messageBody
                    smsMessage.timestampMillis

                    val matchingTriggers = activeTriggersWithRelations.filter { triggerWithRelations ->
                        evaluateConditions(triggerWithRelations.conditions, sender, messageBody)
                    }

                    if (matchingTriggers.isNotEmpty()) {
                        Log.d("SmsReceiver", "MATCH FOUND! SMS from $sender matches ${matchingTriggers.size} trigger(s)")
                        
                        matchingTriggers.forEach { triggerWithRelations ->
                            Log.d("SmsReceiver", "Processing trigger: ${triggerWithRelations.trigger.name}")
                            
                            executeActions(triggerWithRelations, sender, messageBody)
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

    private fun evaluateConditions(
        conditions: List<ConditionEntity>,
        sender: String,
        messageBody: String
    ): Boolean {
        if (conditions.isEmpty()) {
            Log.d("SmsReceiver", "No conditions defined, skipping trigger")
            return false
        }
        
        return conditions.all { condition ->
            val fieldValue = when (condition.field) {
                "sender" -> sender
                "message_body" -> messageBody
                else -> {
                    Log.w("SmsReceiver", "Unknown field: ${condition.field}")
                    ""
                }
            }
            
            val matches = evaluateCondition(fieldValue, condition.operator, condition.value)
            Log.d("SmsReceiver", "Condition check: ${condition.field} ${condition.operator} '${condition.value}' = $matches")
            matches
        }
    }

    private fun evaluateCondition(
        fieldValue: String,
        operator: Operator,
        expectedValue: String
    ): Boolean {
        return when (operator) {
            Operator.EQUALS -> fieldValue.equals(expectedValue, ignoreCase = true)
            Operator.CONTAINS -> fieldValue.contains(expectedValue, ignoreCase = true)
            Operator.START_WITH -> fieldValue.startsWith(expectedValue, ignoreCase = true)
            Operator.END_WITH -> fieldValue.endsWith(expectedValue, ignoreCase = true)
            Operator.DOES_NOT_CONTAINS -> !fieldValue.contains(expectedValue, ignoreCase = true)
        }
    }

    private fun executeActions(
        triggerWithRelations: TriggerWithRelations,
        sender: String,
        messageBody: String
    ) {
        triggerWithRelations.actions.forEach { action ->
            try {
                Log.d("SmsReceiver", "Executing action: ${action.type}")
                when (action.type.uppercase()) {
                    "WEBHOOK" -> executeWebhookAction(action.arg, sender, messageBody)
                    "LOG" -> executeLogAction(action.arg, sender, messageBody)
                    else -> Log.w("SmsReceiver", "Unknown action type: ${action.type}")
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error executing action ${action.type}", e)
            }
        }
    }

    private fun executeWebhookAction(arg: String, sender: String, messageBody: String) {
        val config = WebhookConfig.fromArg(arg)
        if (config == null) {
            Log.e("SmsReceiver", "Webhook action has no valid settings, skipping: $arg")
            return
        }
        try {
            val finalUrl = replaceTemplateVariables(config.url, sender, messageBody)
            val finalBody = config.body?.let {
                replaceTemplateVariables(it, sender, messageBody)
            }

            // OkHttp rejects a body on GET and HEAD, and requires one on POST, PUT and PATCH.
            val method = config.method.trim().uppercase()
            val requestBody = if (method in METHODS_WITHOUT_BODY) {
                null
            } else {
                (finalBody ?: "").toRequestBody("application/json".toMediaType())
            }

            val request = Request.Builder()
                .url(finalUrl)
                .method(method, requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("SmsReceiver", "Webhook call to $finalUrl Failed: ${response.code} ${response.message}")
                } else {
                    Log.d("SmsReceiver", "Webhook call to $finalUrl Successful: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Webhook action execution error", e)
        }
    }

    private fun executeLogAction(arg: String, sender: String, messageBody: String) {
        try {
            val json = JSONObject(arg)
            val message = json.optString("message", "SMS received")
            val finalMessage = replaceTemplateVariables(message, sender, messageBody)
            Log.i("SmsReceiver", "LOG ACTION: $finalMessage")
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Log action execution error", e)
        }
    }

    private fun replaceTemplateVariables(template: String, sender: String, messageBody: String): String {
        return template
            .replace("{{sender}}", sender)
            .replace("{{message}}", messageBody)
            .replace("{{message_body}}", messageBody)
            .replace("{{timestamp}}", System.currentTimeMillis().toString())
    }

    private companion object {
        val METHODS_WITHOUT_BODY = setOf("GET", "HEAD")
    }
}
