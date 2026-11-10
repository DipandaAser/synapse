package com.aserdipanda.synapse.service.triggers

import com.aserdipanda.synapse.data.triggers.local.WebhookConfig
import okhttp3.Request
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebhookTest {

    private val sms = Sms(
        sender = "+12465550123",
        message = "Your code is 4321",
        receivedAt = 1_700_000_000_000L,
    )

    @Test
    fun `fills the placeholders in the URL and the body`() {
        val config = WebhookConfig(
            url = "https://hooks.example.com/sms/{{sender}}",
            method = "post",
            body = """{"text":"{{message}}","at":{{timestamp}}}""",
        )
        val request = Webhook.request(config, sms)

        assertEquals("POST", request.method)
        assertEquals("https://hooks.example.com/sms/+12465550123", request.url.toString())
        assertEquals("""{"text":"Your code is 4321","at":1700000000000}""", request.bodyText())
    }

    @Test
    fun `GET and HEAD send no body`() {
        for (method in listOf("GET", "HEAD")) {
            val config = WebhookConfig(url = "https://hooks.example.com/sms", method = method, body = "{}")
            assertNull(method, Webhook.request(config, sms).body)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a URL without http or https is refused`() {
        Webhook.request(WebhookConfig(url = "hooks.example.com/sms"), sms)
    }

    @Test
    fun `timeouts, rate limits and server errors are worth another try`() {
        for (code in listOf(408, 429, 500, 503)) assertTrue("$code", Webhook.isRetryable(code))
        for (code in listOf(400, 401, 404, 422)) assertFalse("$code", Webhook.isRetryable(code))
    }

    private fun Request.bodyText(): String = Buffer().also { body!!.writeTo(it) }.readUtf8()
}
