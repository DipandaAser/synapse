package com.aserdipanda.synapse.service.sms

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Turns listening on or off. Synapse listens through [SmsReceiver], which Android starts for
 * each incoming SMS. Off, the receiver is disabled, so Android doesn't start Synapse for texts.
 */
object SmsListener {

    fun setEnabled(context: Context, enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SmsReceiver::class.java),
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    }
}
