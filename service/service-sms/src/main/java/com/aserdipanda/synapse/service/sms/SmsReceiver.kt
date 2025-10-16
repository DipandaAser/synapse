package com.aserdipanda.synapse.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.core.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SmsReceiver : BroadcastReceiver() {

    private val targetSenders = listOf("7515")
    private val broadcastList = listOf("")
    private val client by lazy { NetworkModule.provideOkHttpClient() }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (smsMessage in messages) {
                    val sender = smsMessage.displayOriginatingAddress ?: ""
                    val messageBody = smsMessage.messageBody

                    val localIntent = Intent(Constants.ACTION_SMS_RECEIVED).apply {
                        putExtra(Constants.EXTRA_SMS_SENDER, sender)
                        putExtra(Constants.EXTRA_SMS_BODY, messageBody)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
                    }

                    val isTargetSender = targetSenders.any { sender.contains(it) }

                    if (isTargetSender) {
                        Log.d("SmsReceiver", "MATCH FOUND! SMS from $sender. Calling API for broadcast list.")

                        broadcastList.forEach { phoneNumberToCall ->
                            callApi(phoneNumberToCall, messageBody)
                        }

                    } else {
                        Log.d("SmsReceiver", "Ignored SMS from $sender")
                    }
                }
            } finally {
                pendingResult.finish()
            }
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
