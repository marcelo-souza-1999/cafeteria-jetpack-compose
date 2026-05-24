package com.targaryen.cafeteria.feature_catalog.data.repository

import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.feature_catalog.data.mapper.toDomain
import com.targaryen.cafeteria.feature_catalog.data.mapper.toProductEntity
import com.targaryen.cafeteria.feature_catalog.data.remote.FirestoreDataSource
import com.targaryen.cafeteria.feature_catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single
class CatalogRepositoryImpl(
    private val localDao: ProductDao,
    remoteDataSource: FirestoreDataSource
) : CatalogRepository {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        remoteDataSource.streamProducts()
            .onEach { snapshot ->
                val currentLocalProducts = localDao.getProducts().firstOrNull() ?: emptyList()
                val favoriteMap = currentLocalProducts.associate { it.id to it.isFavorite }
                val quantityMap = currentLocalProducts.associate { it.id to it.quantityInCart }

                val entitiesToInsert = snapshot.documents.map { doc ->
                    val isFavorite = favoriteMap[doc.id] ?: false
                    val quantity = quantityMap[doc.id] ?: 0
                    doc.toProductEntity(
                        currentIsFavorite = isFavorite,
                        currentQuantity = quantity
                    )
                }

                if (entitiesToInsert.isNotEmpty()) {
                    localDao.insertProducts(entitiesToInsert)
                }
            }
            .launchIn(coroutineScope)
    }

    override fun getProducts(): Flow<List<Product>> {
        return localDao.getProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(productId: String) {
        val product = localDao.getProduct(productId).firstOrNull()
        if (product != null) {
            val newStatus = !product.isFavorite
            localDao.updateFavoriteStatus(productId, newStatus)
        }
    }

    override suspend fun updateProductQuantity(productId: String, quantity: Int) {
        localDao.updateCartQuantity(productId, quantity)
    }
}
