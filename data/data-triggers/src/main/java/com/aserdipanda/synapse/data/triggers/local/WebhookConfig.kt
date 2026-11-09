package com.aserdipanda.synapse.data.triggers.local

import org.json.JSONObject

/**
 * Settings of a WEBHOOK action, stored as JSON in [ActionEntity.arg]:
 * `{"url": "...", "method": "POST", "body": "..."}`. The body is optional.
 */
data class WebhookConfig(
    val url: String,
    val method: String = "POST",
    val body: String? = null
) {
    fun toArg(): String = JSONObject()
        .put("url", url)
        .put("method", method)
        .apply { if (body != null) put("body", body) }
        .toString()

    companion object {
        /** Returns null when [arg] is not valid webhook JSON, such as an empty string. */
        fun fromArg(arg: String): WebhookConfig? = runCatching {
            val json = JSONObject(arg)
            WebhookConfig(
                url = json.getString("url"),
                method = json.optString("method", "POST"),
                body = if (json.has("body")) json.getString("body") else null
            )
        }.getOrNull()
    }
}
