package com.targaryen.cafeteria.feature_checkout.data

import com.targaryen.cafeteria.core_network.model.MpItemRequest
import com.targaryen.cafeteria.core_network.model.MpPreferenceRequest
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.core_network.remote.MercadoPagoDataSource
import com.targaryen.cafeteria.core_network.remote.ViaCepDataSource
import com.targaryen.cafeteria.feature_catalog.domain.model.Product
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import org.koin.core.annotation.Single

@Single
class CheckoutRepositoryImpl(
    private val viaCepDataSource: ViaCepDataSource,
    private val mercadoPagoDataSource: MercadoPagoDataSource
) : CheckoutRepository {

    override suspend fun fetchAddressByCep(cep: String): Result<ViaCepResponse> {
        return viaCepDataSource.fetchAddressByCep(cep)
    }

    override suspend fun searchCepByAddress(uf: String, city: String, street: String): Result<List<ViaCepResponse>> {
        return viaCepDataSource.searchCepByAddress(uf, city, street)
    }

    override suspend fun createPreferenceForCart(cartItems: List<Product>): Result<MpPreferenceResponse> {
        if (cartItems.isEmpty()) {
            return Result.failure(Exception(ERROR_EMPTY_CART))
        }

        val items = cartItems.map { product ->
            MpItemRequest(
                id = product.id,
                title = product.name,
                quantity = product.quantityInCart,
                unitPrice = product.price,
                description = product.description,
                pictureUrl = product.imageUrl
            )
        }

        val backUrls = com.targaryen.cafeteria.core_network.model.MpBackUrls(
            success = "cafeteria://checkout/success",
            failure = "cafeteria://checkout/failure",
            pending = "cafeteria://checkout/pending"
        )
        val request = MpPreferenceRequest(
            items = items,
            backUrls = backUrls,
            autoReturn = "all"
        )
        return mercadoPagoDataSource.createPreference(request)
    }

    companion object {
        private const val ERROR_EMPTY_CART = "Carrinho vazio"
    }
}
