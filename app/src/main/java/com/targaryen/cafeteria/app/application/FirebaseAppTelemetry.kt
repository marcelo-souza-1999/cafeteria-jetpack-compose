package com.targaryen.cafeteria.app.application

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.targaryen.cafeteria.core_network.telemetry.AppTelemetry
import com.targaryen.cafeteria.core_network.util.AppLogger
import org.koin.core.annotation.Single

/**
 * Implementação de produção de [AppTelemetry] conectando [AppLogger],
 * [FirebaseAnalytics] e [FirebaseCrashlytics] via injeção pura Koin.
 */
@Single
class FirebaseAppTelemetry(
    private val appLogger: AppLogger,
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : AppTelemetry {
    override fun trackScreen(screenName: String) {
        appLogger.i(TAG_TELEMETRY, "screen_view=$screenName")
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            },
        )
        crashlytics.setCustomKey(KEY_CURRENT_ROUTE, screenName)
        crashlytics.log("screen_view=$screenName")
    }

    override fun trackEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        val summary =
            if (params.isEmpty()) {
                "event=$name"
            } else {
                val formatted = params.entries.joinToString(" ") { entry -> "${entry.key}=${entry.value}" }
                "event=$name $formatted"
            }
        appLogger.i(TAG_TELEMETRY, summary)
        analytics.logEvent(name, params.toAnalyticsBundle())
        crashlytics.log(summary)
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(
        key: String,
        value: Boolean,
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun trackNonFatalError(
        stage: String,
        throwable: Throwable,
    ) {
        appLogger.e(TAG_TELEMETRY, "non_fatal stage=$stage", throwable)
        analytics.logEvent(
            EVENT_NON_FATAL_ERROR,
            Bundle().apply {
                putString(PARAM_STAGE, stage)
            },
        )
        crashlytics.setCustomKey(KEY_LAST_ERROR_STAGE, stage)
        crashlytics.log("non_fatal stage=$stage")
        crashlytics.recordException(throwable)
    }

    companion object {
        private const val TAG_TELEMETRY = "AppTelemetry"
        private const val EVENT_NON_FATAL_ERROR = "app_non_fatal_error"
        private const val PARAM_STAGE = "stage"
        private const val KEY_CURRENT_ROUTE = "current_route"
        private const val KEY_LAST_ERROR_STAGE = "last_error_stage"
    }
}

private fun Map<String, Any>.toAnalyticsBundle(): Bundle? {
    if (isEmpty()) return null
    return Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
}
