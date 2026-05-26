package com.targaryen.cafeteria.core_network.remote

interface MercadoPagoTokenProvider {
    fun getAccessToken(): String
    fun getPublicKey(): String
}
