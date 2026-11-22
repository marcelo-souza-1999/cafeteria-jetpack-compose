package com.targaryen.cafeteria.core_network.telemetry

/**
 * Barramento unificado de telemetria (Analytics + Crashlytics + AppLogger),
 * mantendo módulos de feature desacoplados de SDKs do Firebase.
 *
 * Política de privacidade: nunca envie PII (e-mail, senha, CEP, endereço, texto de chat ou UID bruto).
 */
interface AppTelemetry {
    fun trackScreen(screenName: String)

    fun trackEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun setCustomKey(
        key: String,
        value: String,
    )

    fun setCustomKey(
        key: String,
        value: Boolean,
    )

    fun trackNonFatalError(
        stage: String,
        throwable: Throwable,
    )
}
