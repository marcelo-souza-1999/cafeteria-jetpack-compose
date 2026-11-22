package com.targaryen.cafeteria.feature_checkout.telemetry

import com.targaryen.cafeteria.core_network.auth.UserSessionProvider
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.util.AppLogger
import org.koin.core.annotation.Single

/**
 * Contrato estruturado de telemetria do funil de checkout, garantindo observabilidade
 * sem exposição de dados sensíveis (PII).
 */
interface CheckoutTelemetry {
    fun trackCepLookup(
        success: Boolean,
        isReverseSearch: Boolean,
    )

    fun trackPaymentPreferenceCreated(
        success: Boolean,
        itemCount: Int,
    )

    fun trackPaymentResult(status: PaymentStatus)

    fun trackProcessDeathRestored()

    fun trackNonFatalError(
        stage: String,
        throwable: Throwable,
    )
}

/**
 * Implementação local de [CheckoutTelemetry] respaldada pelo [AppLogger].
 */
class AppLoggerCheckoutTelemetry(
    private val appLogger: AppLogger,
) : CheckoutTelemetry {
    override fun trackCepLookup(
        success: Boolean,
        isReverseSearch: Boolean,
    ) {
        appLogger.i(
            TAG_TELEMETRY,
            "event=checkout_cep_lookup reverse=$isReverseSearch success=$success",
        )
    }

    override fun trackPaymentPreferenceCreated(
        success: Boolean,
        itemCount: Int,
    ) {
        appLogger.i(
            TAG_TELEMETRY,
            "event=checkout_preference_created items=$itemCount success=$success",
        )
    }

    override fun trackPaymentResult(status: PaymentStatus) {
        appLogger.i(
            TAG_TELEMETRY,
            "event=checkout_payment_result status=${status.name}",
        )
    }

    override fun trackProcessDeathRestored() {
        appLogger.i(
            TAG_TELEMETRY,
            "event=checkout_process_death_restored",
        )
    }

    override fun trackNonFatalError(
        stage: String,
        throwable: Throwable,
    ) {
        appLogger.e(
            TAG_TELEMETRY,
            "event=checkout_non_fatal_error stage=$stage",
            throwable,
        )
    }

    companion object {
        private const val TAG_TELEMETRY = "CheckoutTelemetry"
    }
}

/**
 * Agrupador coeso de sessão e telemetria para manter o construtor do ViewModel enxuto e testável.
 */
@Single
class CheckoutRuntimeDependencies(
    val userSessionProvider: UserSessionProvider,
    val telemetry: CheckoutTelemetry,
)
