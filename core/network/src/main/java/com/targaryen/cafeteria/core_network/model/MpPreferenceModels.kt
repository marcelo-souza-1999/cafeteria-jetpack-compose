package com.targaryen.cafeteria.core_network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MpPreferenceRequest(
    @SerialName("items") val items: List<MpItemRequest>,
    @SerialName("back_urls") val backUrls: MpBackUrls? = null,
    @SerialName("auto_return") val autoReturn: String? = null
)

@Serializable
data class MpBackUrls(
    @SerialName("success") val success: String? = null,
    @SerialName("failure") val failure: String? = null,
    @SerialName("pending") val pending: String? = null
)

@Serializable
data class MpItemRequest(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("currency_id") val currencyId: String = "BRL",
    @SerialName("description") val description: String? = null,
    @SerialName("picture_url") val pictureUrl: String? = null
)

@Serializable
data class MpPreferenceResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("init_point") val initPoint: String? = null,
    @SerialName("sandbox_init_point") val sandboxInitPoint: String? = null
)
