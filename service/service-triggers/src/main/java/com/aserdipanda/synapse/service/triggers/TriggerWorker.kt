package com.aserdipanda.synapse.service.triggers

import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aserdipanda.synapse.core.network.NetworkModule
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.ActionEntity
import com.aserdipanda.synapse.data.triggers.local.DatabaseProvider
import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Runs one trigger's actions for one SMS, as soon as there's a network. When an SMS arrives,
 * Android lets the apps receiving it run right away, even on an idle phone. A webhook that
 * can't be reached, or answers with a timeout, rate limit or server error, gets another try.
 */
internal class TriggerWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private enum class Outcome { Done, Failed, Retry }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sender = inputData.getString(KEY_SENDER)
        val message = inputData.getString(KEY_MESSAGE)
        if (sender == null || message == null) return@withContext Result.failure()
        val sms = Sms(sender, message, receivedAt = inputData.getLong(KEY_RECEIVED_AT, 0L))

        val repository = TriggersRepository(DatabaseProvider.getDatabase(applicationContext))
        // Deleted since the SMS arrived: nothing left to run.
        val trigger = repository.getTriggerWithRelationsById(inputData.getLong(KEY_TRIGGER_ID, -1L))
            ?: return@withContext Result.success()

        // Another try runs every action again. A trigger has a single webhook for now.
        val outcomes = trigger.actions.map { run(it, sms) }
        when {
            Outcome.Retry in outcomes && runAttemptCount + 1 < MAX_ATTEMPTS -> Result.retry()
            outcomes.all { it == Outcome.Done } -> Result.success()
            else -> Result.failure()
        }
    }

    private fun run(action: ActionEntity, sms: Sms): Outcome = when (action.type.uppercase()) {
        "WEBHOOK" -> send(action.arg, sms)
        "LOG" -> {
            val template = runCatching { JSONObject(action.arg).optString("message", "SMS received") }
                .getOrDefault("SMS received")
            Log.i(TAG, "Log action: ${Webhook.fill(template, sms)}")
            Outcome.Done
        }
        else -> {
            Log.w(TAG, "Unknown action type: ${action.type}")
            Outcome.Failed
        }
    }

    private fun send(arg: String, sms: Sms): Outcome {
        val config = WebhookConfig.fromArg(arg)
        if (config == null) {
            Log.e(TAG, "Webhook action has no valid settings")
            return Outcome.Failed
        }
        val request = try {
            Webhook.request(config, sms)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Webhook URL or method isn't valid")
            return Outcome.Failed
        }
        return try {
            NetworkModule.provideOkHttpClient().newCall(request).execute().use { response ->
                Log.d(TAG, "Webhook answered ${response.code}")
                when {
                    response.isSuccessful -> Outcome.Done
                    Webhook.isRetryable(response.code) -> Outcome.Retry
                    else -> Outcome.Failed
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Webhook unreachable", e)
            Outcome.Retry
        }
    }

    // Android 11 and earlier run expedited work in a foreground service, which shows this
    // notification while the call runs. Later versions don't need it.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val context = applicationContext
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.webhook_channel_name))
                .build()
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_synapse)
            .setContentTitle(context.getString(R.string.webhook_sending))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "TriggerWorker"
        private const val KEY_TRIGGER_ID = "trigger_id"
        private const val KEY_SENDER = "sender"
        private const val KEY_MESSAGE = "message"
        private const val KEY_RECEIVED_AT = "received_at"
        private const val CHANNEL_ID = "webhook_calls"
        private const val NOTIFICATION_ID = 1

        // Eight tries: the first retry waits 30 seconds, and each next one twice as long.
        private const val MAX_ATTEMPTS = 8

        fun enqueue(context: Context, triggerId: Long, sms: Sms) {
            val request = OneTimeWorkRequestBuilder<TriggerWorker>()
                .setInputData(
                    workDataOf(
                        KEY_TRIGGER_ID to triggerId,
                        KEY_SENDER to sms.sender,
                        KEY_MESSAGE to sms.message,
                        KEY_RECEIVED_AT to sms.receivedAt,
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkRequest(anyNetwork(), NetworkType.CONNECTED)
                        .build()
                )
                // Runs right away when Android allows it, and as regular work otherwise.
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        // Any connected network, VPNs included, even one Android hasn't confirmed reaches the
        // internet: the webhook may be on the local network, and a failed call is tried again.
        // A plain network constraint would wait for that confirmation.
        private fun anyNetwork(): NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()
    }
}
