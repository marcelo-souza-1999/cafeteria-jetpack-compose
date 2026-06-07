package com.targaryen.cafeteria.core_network.remote

import com.targaryen.cafeteria.core_network.BuildConfig
import com.targaryen.cafeteria.core_network.model.B2AuthorizeResponse
import com.targaryen.cafeteria.core_network.model.B2GetUploadUrlRequest
import com.targaryen.cafeteria.core_network.model.B2GetUploadUrlResponse
import com.targaryen.cafeteria.core_network.model.B2UploadFileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Single
import java.util.Base64

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
                    .get(BuildConfig.B2_AUTHORIZE_URL) {
                        header(HEADER_AUTHORIZATION, "Basic $authCredentials")
                    }.body<B2AuthorizeResponse>()

            val getUploadUrlResponse =
                httpClient
                    .post("${authorizeResponse.apiInfo.storageApi.apiUrl}/b2api/v3/b2_get_upload_url") {
                        header(HEADER_AUTHORIZATION, authorizeResponse.authorizationToken)
                        contentType(ContentType.Application.Json)
                        setBody(B2GetUploadUrlRequest(bucketId = BuildConfig.B2_BUCKET_ID))
                    }.body<B2GetUploadUrlResponse>()

            httpClient
                .post(getUploadUrlResponse.uploadUrl) {
                    header(HEADER_AUTHORIZATION, getUploadUrlResponse.authorizationToken)
                    header(HEADER_BZ_FILE_NAME, fileName)
                    header(HEADER_BZ_CONTENT_SHA1, VALUE_BZ_SHA1_DO_NOT_VERIFY)
                    contentType(ContentType.parse(contentType))
                    setBody(fileBytes)
                }.body<B2UploadFileResponse>()

            return "${BuildConfig.B2_DOWNLOAD_URL_BASE}$fileName"
        } catch (e: ResponseException) {
            throw java.io.IOException("B2 Upload failed: ${e.message}", e)
        }
    }
}

private const val HEADER_AUTHORIZATION = "Authorization"
private const val HEADER_BZ_FILE_NAME = "X-Bz-File-Name"
private const val HEADER_BZ_CONTENT_SHA1 = "X-Bz-Content-Sha1"
private const val VALUE_BZ_SHA1_DO_NOT_VERIFY = "do_not_verify"
