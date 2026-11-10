package com.aserdipanda.synapse.service.triggers

import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.Operator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerMatcherTest {

    private val sms = Sms(
        sender = "+12465550123",
        message = "Card transaction of 42.00 at Coffee",
        receivedAt = 0L,
    )

    private fun sender(operator: Operator, value: String) =
        ConditionEntity(triggerId = 1, field = "sender", operator = operator, value = value)

    private fun message(operator: Operator, value: String) =
        ConditionEntity(triggerId = 1, field = "message_body", operator = operator, value = value)

    @Test
    fun `matches when every condition holds, whatever the case`() {
        val conditions = listOf(
            sender(Operator.EQUALS, "+12465550123"),
            message(Operator.CONTAINS, "TRANSACTION"),
        )
        assertTrue(TriggerMatcher.matches(conditions, sms))
    }

    @Test
    fun `one condition that fails is enough to skip the trigger`() {
        val conditions = listOf(
            sender(Operator.EQUALS, "+12465550123"),
            message(Operator.CONTAINS, "delivered"),
        )
        assertFalse(TriggerMatcher.matches(conditions, sms))
    }

    @Test
    fun `a trigger without conditions never runs`() {
        assertFalse(TriggerMatcher.matches(emptyList(), sms))
    }

    @Test
    fun `starts with, ends with and does not contain`() {
        assertTrue(TriggerMatcher.matches(listOf(sender(Operator.START_WITH, "+1246")), sms))
        assertTrue(TriggerMatcher.matches(listOf(message(Operator.END_WITH, "coffee")), sms))
        assertTrue(TriggerMatcher.matches(listOf(message(Operator.DOES_NOT_CONTAINS, "refund")), sms))
        assertFalse(TriggerMatcher.matches(listOf(message(Operator.DOES_NOT_CONTAINS, "card")), sms))
    }
}
