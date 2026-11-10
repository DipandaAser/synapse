package com.aserdipanda.synapse.service.triggers

import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** A trigger's webhook request for one SMS, and which failed calls are worth another try. */
internal object Webhook {

    private val JSON = "application/json".toMediaType()

    // OkHttp rejects a body on GET and HEAD, and requires one on POST, PUT and PATCH.
    private val METHODS_WITHOUT_BODY = setOf("GET", "HEAD")

    /** @throws IllegalArgumentException when the URL or the method isn't valid. */
    fun request(config: WebhookConfig, sms: Sms): Request {
        val method = config.method.trim().uppercase()
        val body = if (method in METHODS_WITHOUT_BODY) {
            null
        } else {
            fill(config.body.orEmpty(), sms).toRequestBody(JSON)
        }
        return Request.Builder()
            .url(fill(config.url, sms))
            .method(method, body)
            .build()
    }

    /** Timeouts, rate limits and server errors can pass. Other client errors won't. */
    fun isRetryable(statusCode: Int): Boolean =
        statusCode == 408 || statusCode == 429 || statusCode in 500..599

    /** Replaces the placeholders that a trigger's URL and body can use. */
    fun fill(template: String, sms: Sms): String = template
        .replace("{{sender}}", sms.sender)
        .replace("{{message}}", sms.message)
        .replace("{{message_body}}", sms.message)
        .replace("{{timestamp}}", sms.receivedAt.toString())
}
