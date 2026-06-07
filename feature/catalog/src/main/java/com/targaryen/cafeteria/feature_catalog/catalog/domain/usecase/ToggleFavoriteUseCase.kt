package com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase

import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import org.koin.core.annotation.Factory

@Factory
class ToggleFavoriteUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(productId: String) {
        repository.toggleFavorite(productId)
    }
}
