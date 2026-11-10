package com.aserdipanda.synapse.feature.onboarding

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Context.isSmsPermissionGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) ==
        PackageManager.PERMISSION_GRANTED

fun Activity.shouldShowSmsRationale(): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)

/** [askedBefore] is whether Synapse has asked for SMS access before, saved across launches. */
fun Activity.smsAccess(askedBefore: Boolean): SmsAccess = smsAccessOf(
    isGranted = isSmsPermissionGranted(),
    shouldShowRationale = shouldShowSmsRationale(),
    askedBefore = askedBefore,
)

/** Opens Synapse's page in Android Settings, where blocked permissions can still be allowed. */
fun Context.openAppSettings() {
    val appDetails = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
    try {
        startActivity(appDetails)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
