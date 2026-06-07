package com.targaryen.cafeteria.feature_checkout.data

import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.core_network.model.MpItemRequest
import com.targaryen.cafeteria.core_network.model.MpPreferenceRequest
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.core_network.remote.MercadoPagoDataSource
import com.targaryen.cafeteria.core_network.remote.ViaCepDataSource
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single
class CheckoutRepositoryImpl(
    private val viaCepDataSource: ViaCepDataSource,
    private val mercadoPagoDataSource: MercadoPagoDataSource,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CheckoutRepository {
    override suspend fun fetchAddressByCep(cep: String): Result<ViaCepResponse> =
        viaCepDataSource.fetchAddressByCep(cep)

    override suspend fun searchCepByAddress(
        uf: String,
        city: String,
        street: String,
    ): Result<List<ViaCepResponse>> = viaCepDataSource.searchCepByAddress(uf, city, street)

    override suspend fun createPreferenceForCart(cartItems: List<Product>): Result<MpPreferenceResponse> {
        if (cartItems.isEmpty()) {
            return Result.failure(Exception(ERROR_EMPTY_CART))
        }

        val items =
            cartItems.map { product ->
                MpItemRequest(
                    id = product.id,
                    title = product.name,
                    quantity = product.quantityInCart,
                    unitPrice = product.price,
                    description = product.description,
                    pictureUrl = product.imageUrl,
                )
            }

        val backUrls =
            com.targaryen.cafeteria.core_network.model.MpBackUrls(
                success = "cafeteria://checkout/success",
                failure = "cafeteria://checkout/failure",
                pending = "cafeteria://checkout/pending",
            )
        val request =
            MpPreferenceRequest(
                items = items,
                backUrls = backUrls,
                autoReturn = "all",
            )
        return mercadoPagoDataSource.createPreference(request)
    }

    override suspend fun saveOrder(
        userId: String,
        itemsSummary: String,
        totalPrice: Double,
        status: String,
    ): Result<Unit> =
        try {
            val orderData =
                hashMapOf(
                    "userId" to userId,
                    "itemsSummary" to itemsSummary,
                    "totalPrice" to totalPrice,
                    "status" to status,
                    "dateMillis" to System.currentTimeMillis(),
                )
            firestore.collection("orders").add(orderData).await()
            Result.success(Unit)
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Result.failure(e)
        } catch (e: com.google.firebase.FirebaseException) {
            Result.failure(e)
        }

    companion object {
        private const val ERROR_EMPTY_CART = "Carrinho vazio"
    }
}
