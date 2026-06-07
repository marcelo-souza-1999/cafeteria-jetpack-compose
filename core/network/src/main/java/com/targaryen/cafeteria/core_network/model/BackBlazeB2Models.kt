package com.targaryen.cafeteria.core_network.model

import kotlinx.serialization.Serializable

@Serializable
data class B2StorageApi(
    val apiUrl: String,
    val downloadUrl: String,
)

@Serializable
data class B2ApiInfo(
    val storageApi: B2StorageApi,
)

@Serializable
data class B2AuthorizeResponse(
    val apiInfo: B2ApiInfo,
    val authorizationToken: String,
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
