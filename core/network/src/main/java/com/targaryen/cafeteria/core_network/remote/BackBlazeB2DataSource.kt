package com.targaryen.cafeteria.core_network.remote

import com.targaryen.cafeteria.core_network.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import java.util.Base64

@Serializable
data class B2AuthorizeResponse(
    val apiUrl: String,
    val authorizationToken: String,
    val downloadUrl: String,
)

@Serializable
data class B2GetUploadUrlRequest(
    val bucketId: String,
)

@Serializable
data class B2GetUploadUrlResponse(
    val bucketId: String,
    val uploadUrl: String,
    val authorizationToken: String,
)

@Serializable
data class B2UploadFileResponse(
    val fileId: String,
    val fileName: String,
    val contentLength: Long,
)

@Single
class BackBlazeB2DataSource(
    private val httpClient: HttpClient,
) {
    suspend fun uploadFile(
        fileName: String,
        fileBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): String {
        try {
            val authString = "${BuildConfig.B2_KEY_ID}:${BuildConfig.B2_APPLICATION_KEY}"
            val authCredentials = Base64.getEncoder().encodeToString(authString.toByteArray())

            val authorizeResponse =
                httpClient
                    .get("https://api.backblazeb2.com/b2api/v3/b2_authorize_account") {
                        header("Authorization", "Basic $authCredentials")
                    }.body<B2AuthorizeResponse>()

            val getUploadUrlResponse =
                httpClient
                    .post("${authorizeResponse.apiUrl}/b2api/v3/b2_get_upload_url") {
                        header("Authorization", authorizeResponse.authorizationToken)
                        contentType(ContentType.Application.Json)
                        setBody(B2GetUploadUrlRequest(bucketId = BuildConfig.B2_BUCKET_ID))
                    }.body<B2GetUploadUrlResponse>()

            httpClient
                .post(getUploadUrlResponse.uploadUrl) {
                    header("Authorization", getUploadUrlResponse.authorizationToken)
                    header("X-Bz-File-Name", fileName)
                    header("X-Bz-Content-Sha1", "do_not_verify")
                    contentType(ContentType.parse(contentType))
                    setBody(fileBytes)
                }.body<B2UploadFileResponse>()

            return "${BuildConfig.B2_DOWNLOAD_URL_BASE}$fileName"
        } catch (e: ResponseException) {
            throw java.io.IOException("B2 Upload failed: ${e.message}", e)
        }
    }
}
