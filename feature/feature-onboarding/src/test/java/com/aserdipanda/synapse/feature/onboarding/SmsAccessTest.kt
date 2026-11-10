package com.aserdipanda.synapse.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsAccessTest {

    @Test
    fun `granted wins whatever else Android reports`() {
        assertEquals(SmsAccess.Granted, smsAccessOf(isGranted = true, shouldShowRationale = true, askedBefore = true))
        assertEquals(SmsAccess.Granted, smsAccessOf(isGranted = true, shouldShowRationale = false, askedBefore = false))
    }

    @Test
    fun `no rationale and never asked is the first request`() {
        assertEquals(SmsAccess.NotAsked, smsAccessOf(isGranted = false, shouldShowRationale = false, askedBefore = false))
    }

    @Test
    fun `rationale means Android can still ask`() {
        assertEquals(SmsAccess.Denied, smsAccessOf(isGranted = false, shouldShowRationale = true, askedBefore = true))
    }

    @Test
    fun `no rationale after asking means only Settings can allow it`() {
        assertEquals(SmsAccess.Blocked, smsAccessOf(isGranted = false, shouldShowRationale = false, askedBefore = true))
    }
}
