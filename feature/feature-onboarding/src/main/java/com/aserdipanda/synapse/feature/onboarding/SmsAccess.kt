package com.aserdipanda.synapse.feature.onboarding

/** Where Synapse stands with the RECEIVE_SMS permission, and so which screen helps. */
enum class SmsAccess {
    /** Allowed: Synapse can listen. */
    Granted,

    /** Never asked: explain first, then let Android ask. */
    NotAsked,

    /** Denied once: Android can still show its prompt, so ask again. */
    Denied,

    /** Denied for good: Android won't ask again, only the app's settings page can allow it. */
    Blocked,
}

/**
 * Android shows no rationale both before the first request and after a permanent denial,
 * so [askedBefore] tells the two apart.
 */
fun smsAccessOf(
    isGranted: Boolean,
    shouldShowRationale: Boolean,
    askedBefore: Boolean,
): SmsAccess = when {
    isGranted -> SmsAccess.Granted
    shouldShowRationale -> SmsAccess.Denied
    askedBefore -> SmsAccess.Blocked
    else -> SmsAccess.NotAsked
}
