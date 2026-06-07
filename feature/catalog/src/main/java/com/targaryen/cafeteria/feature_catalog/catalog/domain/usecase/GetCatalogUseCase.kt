package com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase

import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetCatalogUseCase(
    private val repository: CatalogRepository,
) {
    operator fun invoke(): Flow<List<Product>> = repository.getProducts()
}
