package com.targaryen.cafeteria.feature_catalog.domain.repository

import com.targaryen.cafeteria.feature_catalog.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun toggleFavorite(productId: String)
    suspend fun updateProductQuantity(productId: String, quantity: Int)
    suspend fun clearCart()
}
