package com.targaryen.cafeteria.feature_catalog.domain.usecase

import com.targaryen.cafeteria.feature_catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetCatalogUseCase(
    private val repository: CatalogRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getProducts()
    }
}
