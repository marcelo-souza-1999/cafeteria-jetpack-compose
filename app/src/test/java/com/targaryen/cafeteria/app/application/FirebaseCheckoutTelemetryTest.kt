package com.targaryen.cafeteria.app.application

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.util.AppLogger
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseCheckoutTelemetryTest {
    private val appLogger: AppLogger = mockk(relaxed = true)
    private val analytics: FirebaseAnalytics = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    @Test
    fun trackEvents_logsAnalyticsAndCrashlyticsBreadcrumbs() {
        val telemetry = FirebaseCheckoutTelemetry(appLogger, analytics, crashlytics)
        val error = IllegalStateException("timeout")

        telemetry.trackCepLookup(success = true, isReverseSearch = false)
        telemetry.trackPaymentPreferenceCreated(success = false, itemCount = 2)
        telemetry.trackPaymentResult(PaymentStatus.SUCCESS)
        telemetry.trackProcessDeathRestored()
        telemetry.trackNonFatalError("payment_preference", error)

        verify { analytics.logEvent(FirebaseCheckoutTelemetry.EVENT_CEP_LOOKUP, any()) }
        verify { analytics.logEvent(FirebaseCheckoutTelemetry.EVENT_PREFERENCE_CREATED, any()) }
        verify { analytics.logEvent(FirebaseCheckoutTelemetry.EVENT_PAYMENT_RESULT, any()) }
        verify { analytics.logEvent(FirebaseCheckoutTelemetry.EVENT_PROCESS_DEATH_RESTORED, null) }
        verify { analytics.logEvent(FirebaseCheckoutTelemetry.EVENT_NON_FATAL_ERROR, any()) }
        verify { crashlytics.recordException(error) }
    }

    @Test
    fun appTelemetry_logsScreensEventsKeysAndNonFatals() {
        val appTelemetry = FirebaseAppTelemetry(appLogger, analytics, crashlytics)
        val error = IllegalStateException("stream_failed")

        appTelemetry.trackScreen("CheckoutRoute")
        appTelemetry.trackEvent("add_to_cart", mapOf("item_id" to "1", "quantity" to 2))
        appTelemetry.setCustomKey("active_tab", "CART")
        appTelemetry.setCustomKey("is_authenticated", true)
        appTelemetry.trackNonFatalError("chat_ai_stream", error)

        verify { analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, any()) }
        verify { analytics.logEvent("add_to_cart", any()) }
        verify { analytics.logEvent("app_non_fatal_error", any()) }
        verify { crashlytics.setCustomKey("current_route", "CheckoutRoute") }
        verify { crashlytics.setCustomKey("active_tab", "CART") }
        verify { crashlytics.setCustomKey("is_authenticated", true) }
        verify { crashlytics.recordException(error) }
    }
}
