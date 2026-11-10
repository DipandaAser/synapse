package com.aserdipanda.synapse.feature.triggers

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The examples Home offers when there are no triggers yet. Each opens the editor with its name and
 * message rule filled in; nothing is saved until the user saves.
 */
enum class TriggerTemplate(
    @get:StringRes val title: Int,
    val icon: ImageVector,
    val messageContains: String,
) {
    BankAlerts(R.string.template_bank_alerts, Icons.Outlined.CreditCard, "transaction"),
    DeliveryUpdates(R.string.template_delivery_updates, Icons.Outlined.Inventory2, "delivered"),
    OneTimeCodes(R.string.template_one_time_codes, Icons.Outlined.Key, "code"),
}
