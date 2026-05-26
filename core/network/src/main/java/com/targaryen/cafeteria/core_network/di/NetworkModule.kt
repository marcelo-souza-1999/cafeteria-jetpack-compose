package com.targaryen.cafeteria.core_network.di

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Cache
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module
@ComponentScan("com.targaryen.cafeteria.core_network")
class NetworkModule {

    @Single
    fun provideKtorClient(context: Context): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                val cacheSize = HTTP_CACHE_SIZE
                val cacheDir = File(context.cacheDir, "http_cache")
                config {
                    cache(Cache(cacheDir, cacheSize))
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }

    companion object {
        private const val CACHE_SIZE_MB = 10L
        private const val KB_TO_BYTES = 1024L
        private const val MB_TO_KB = 1024L
        private const val HTTP_CACHE_SIZE = CACHE_SIZE_MB * KB_TO_BYTES * MB_TO_KB
    }
}
