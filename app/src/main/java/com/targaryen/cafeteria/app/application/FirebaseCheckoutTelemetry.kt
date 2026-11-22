package com.targaryen.cafeteria.app.application

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.util.AppLogger
import com.targaryen.cafeteria.feature_checkout.telemetry.AppLoggerCheckoutTelemetry
import com.targaryen.cafeteria.feature_checkout.telemetry.CheckoutTelemetry
import org.koin.core.annotation.Single

/**
 * Implementação de produção de [CheckoutTelemetry] integrando logging local,
 * eventos de funil no [FirebaseAnalytics] e breadcrumbs / non-fatals no [FirebaseCrashlytics].
 */
@Single
class FirebaseCheckoutTelemetry(
    appLogger: AppLogger,
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : CheckoutTelemetry {
    private val loggerDelegate = AppLoggerCheckoutTelemetry(appLogger)

    override fun trackCepLookup(
        success: Boolean,
        isReverseSearch: Boolean,
    ) {
        loggerDelegate.trackCepLookup(success = success, isReverseSearch = isReverseSearch)
        analytics.logEvent(
            EVENT_CEP_LOOKUP,
            Bundle().apply {
                putBoolean(PARAM_IS_REVERSE_SEARCH, isReverseSearch)
                putBoolean(PARAM_SUCCESS, success)
            },
        )
        crashlytics.setCustomKey(KEY_LAST_CHECKOUT_STAGE, STAGE_CEP_LOOKUP)
        crashlytics.log("checkout_cep_lookup reverse=$isReverseSearch success=$success")
    }

    override fun trackPaymentPreferenceCreated(
        success: Boolean,
        itemCount: Int,
    ) {
        loggerDelegate.trackPaymentPreferenceCreated(success = success, itemCount = itemCount)
        analytics.logEvent(
            EVENT_PREFERENCE_CREATED,
            Bundle().apply {
                putInt(PARAM_ITEM_COUNT, itemCount)
                putBoolean(PARAM_SUCCESS, success)
            },
        )
        crashlytics.setCustomKey(KEY_LAST_CHECKOUT_STAGE, STAGE_PAYMENT_PREFERENCE)
        crashlytics.log("checkout_preference_created items=$itemCount success=$success")
    }

    override fun trackPaymentResult(status: PaymentStatus) {
        loggerDelegate.trackPaymentResult(status)
        analytics.logEvent(
            EVENT_PAYMENT_RESULT,
            Bundle().apply {
                putString(PARAM_PAYMENT_STATUS, status.name)
            },
        )
        crashlytics.setCustomKey(KEY_LAST_CHECKOUT_STAGE, STAGE_PAYMENT_RESULT)
        crashlytics.setCustomKey(KEY_LAST_PAYMENT_STATUS, status.name)
        crashlytics.log("checkout_payment_result status=${status.name}")
    }

    override fun trackProcessDeathRestored() {
        loggerDelegate.trackProcessDeathRestored()
        analytics.logEvent(EVENT_PROCESS_DEATH_RESTORED, null)
        crashlytics.setCustomKey(KEY_PROCESS_DEATH_RESTORED, true)
        crashlytics.log("checkout_process_death_restored")
    }

    override fun trackNonFatalError(
        stage: String,
        throwable: Throwable,
    ) {
        loggerDelegate.trackNonFatalError(stage = stage, throwable = throwable)
        analytics.logEvent(
            EVENT_NON_FATAL_ERROR,
            Bundle().apply {
                putString(PARAM_STAGE, stage)
            },
        )
        crashlytics.setCustomKey(KEY_LAST_CHECKOUT_STAGE, stage)
        crashlytics.log("checkout_non_fatal_error stage=$stage")
        crashlytics.recordException(throwable)
    }

    companion object {
        const val EVENT_CEP_LOOKUP = "checkout_cep_lookup"
        const val EVENT_PREFERENCE_CREATED = "checkout_preference_created"
        const val EVENT_PAYMENT_RESULT = "checkout_payment_result"
        const val EVENT_PROCESS_DEATH_RESTORED = "checkout_process_death_restored"
        const val EVENT_NON_FATAL_ERROR = "checkout_non_fatal_error"

        private const val PARAM_IS_REVERSE_SEARCH = "is_reverse_search"
        private const val PARAM_SUCCESS = "success"
        private const val PARAM_ITEM_COUNT = "item_count"
        private const val PARAM_PAYMENT_STATUS = "payment_status"
        private const val PARAM_STAGE = "stage"

        private const val KEY_LAST_CHECKOUT_STAGE = "last_checkout_stage"
        private const val KEY_LAST_PAYMENT_STATUS = "last_payment_status"
        private const val KEY_PROCESS_DEATH_RESTORED = "checkout_process_death_restored"

        private const val STAGE_CEP_LOOKUP = "cep_lookup"
        private const val STAGE_PAYMENT_PREFERENCE = "payment_preference"
        private const val STAGE_PAYMENT_RESULT = "payment_result"
    }
}
