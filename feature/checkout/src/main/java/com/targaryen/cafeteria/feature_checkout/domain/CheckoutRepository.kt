package com.targaryen.cafeteria.feature_checkout.domain

import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product

interface CheckoutRepository {
    suspend fun fetchAddressByCep(cep: String): Result<ViaCepResponse>
    suspend fun searchCepByAddress(uf: String, city: String, street: String): Result<List<ViaCepResponse>>
    suspend fun createPreferenceForCart(cartItems: List<Product>): Result<MpPreferenceResponse>
    
    suspend fun saveOrder(
        userId: String,
        itemsSummary: String,
        totalPrice: Double,
        status: String
    ): Result<Unit>
}
