package com.aserdipanda.synapse.service.triggers

/** One incoming text. A long text arrives in parts, and [message] has them joined. */
data class Sms(
    val sender: String,
    val message: String,
    /** When Synapse received it, in milliseconds since the epoch. */
    val receivedAt: Long,
)
