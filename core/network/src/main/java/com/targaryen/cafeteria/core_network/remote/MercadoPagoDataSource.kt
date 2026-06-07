package com.targaryen.cafeteria.core_network.remote

import com.targaryen.cafeteria.core_network.model.MpPreferenceRequest
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class MercadoPagoDataSource(
    private val httpClient: HttpClient,
    private val tokenProvider: MercadoPagoTokenProvider,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun createPreference(request: MpPreferenceRequest): Result<MpPreferenceResponse> =
        try {
            val token = tokenProvider.getAccessToken()
            val response: MpPreferenceResponse =
                httpClient
                    .post("https://api.mercadopago.com/checkout/preferences") {
                        header("Authorization", "Bearer $token")
                        setBody(request)
                    }.body()

            if (response.id.isNullOrBlank()) {
                Result.failure(Exception("Falha ao criar preferência no Mercado Pago"))
            } else {
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
