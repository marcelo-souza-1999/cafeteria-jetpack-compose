package com.targaryen.cafeteria.app.appfunctions

import com.targaryen.cafeteria.core_network.telemetry.AppTelemetry
import kotlinx.coroutines.CancellationException

internal suspend inline fun <T> AppTelemetry.traceAppFunction(
    functionName: String,
    block: () -> T,
): T =
    try {
        val result = block()
        trackEvent(
            name = "app_function_invoked",
            params = mapOf("function_name" to functionName, "success" to true),
        )
        result
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: IllegalStateException) {
        reportAppFunctionFailure(functionName, error)
        throw error
    } catch (error: IllegalArgumentException) {
        reportAppFunctionFailure(functionName, error)
        throw error
    }

private fun AppTelemetry.reportAppFunctionFailure(
    functionName: String,
    error: Throwable,
) {
    trackEvent(
        name = "app_function_invoked",
        params = mapOf("function_name" to functionName, "success" to false),
    )
    trackNonFatalError("app_function_$functionName", error)
}
