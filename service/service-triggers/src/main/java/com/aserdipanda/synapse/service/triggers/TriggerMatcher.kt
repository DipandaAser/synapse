package com.aserdipanda.synapse.service.triggers

import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.Operator

/** Whether an SMS meets every condition of a trigger. Comparisons ignore case. */
internal object TriggerMatcher {

    /** A trigger without conditions never runs, rather than running for every text. */
    fun matches(conditions: List<ConditionEntity>, sms: Sms): Boolean =
        conditions.isNotEmpty() && conditions.all { it.isMetBy(sms) }

    private fun ConditionEntity.isMetBy(sms: Sms): Boolean {
        // Unknown fields read as empty.
        val text = when (field) {
            "sender" -> sms.sender
            "message_body" -> sms.message
            else -> ""
        }
        return when (operator) {
            Operator.EQUALS -> text.equals(value, ignoreCase = true)
            Operator.CONTAINS -> text.contains(value, ignoreCase = true)
            Operator.START_WITH -> text.startsWith(value, ignoreCase = true)
            Operator.END_WITH -> text.endsWith(value, ignoreCase = true)
            Operator.DOES_NOT_CONTAINS -> !text.contains(value, ignoreCase = true)
        }
    }
}
