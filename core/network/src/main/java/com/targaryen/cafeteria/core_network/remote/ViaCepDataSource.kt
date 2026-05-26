package com.targaryen.cafeteria.core_network.remote

import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Single

@Single
class ViaCepDataSource(
    private val httpClient: HttpClient
) {
    suspend fun fetchAddressByCep(cep: String): Result<ViaCepResponse> {
        return try {
            val sanitizedCep = cep.replace("-", "").replace(".", "")
            val response: ViaCepResponse =
                httpClient.get("https://viacep.com.br/ws/$sanitizedCep/json/").body()
            if (response.erro == true) {
                Result.failure(Exception("CEP não encontrado"))
            } else {
                Result.success(response)
            }
        } catch (e: kotlinx.io.IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        } catch (e: ResponseException) {
            Result.failure(e)
        }
    }

    suspend fun searchCepByAddress(
        uf: String,
        city: String,
        street: String
    ): Result<List<ViaCepResponse>> {
        return try {
            val sanitizedUf = uf.trim()
            val sanitizedCity = city.trim()
            val sanitizedStreet = street.trim()
            val response: List<ViaCepResponse> = httpClient.get("https://viacep.com.br/ws") {
                url {
                    appendPathSegments(sanitizedUf, sanitizedCity, sanitizedStreet, "json")
                }
            }.body()
            Result.success(response)
        } catch (e: kotlinx.io.IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        } catch (e: ResponseException) {
            Result.failure(e)
        }
    }
}
