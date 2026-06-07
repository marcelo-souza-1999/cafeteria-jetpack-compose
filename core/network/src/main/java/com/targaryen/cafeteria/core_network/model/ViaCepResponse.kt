package com.targaryen.cafeteria.core_network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViaCepResponse(
    @SerialName("cep") val cep: String? = null,
    @SerialName("logradouro") val logradouro: String? = null,
    @SerialName("complemento") val complemento: String? = null,
    @SerialName("bairro") val bairro: String? = null,
    @SerialName("localidade") val localidade: String? = null,
    @SerialName("uf") val uf: String? = null,
    @SerialName("erro") val erro: Boolean? = null,
)
