package com.targaryen.cafeteria.feature_catalog.catalog.presentation.intent

import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel

sealed interface CatalogIntent {
    data class Search(
        val query: String,
    ) : CatalogIntent

    data class SelectCategory(
        val category: String,
    ) : CatalogIntent

    data class SelectProduct(
        val product: ProductUiModel?,
    ) : CatalogIntent

    data class UpdateProductQuantity(
        val productId: String,
        val quantity: Int,
    ) : CatalogIntent

    data class AddToCart(
        val product: ProductUiModel,
    ) : CatalogIntent

    data class RemoveFromCart(
        val product: ProductUiModel,
    ) : CatalogIntent

    data class ToggleFavorite(
        val product: ProductUiModel,
    ) : CatalogIntent

    object ClearSearch : CatalogIntent
}
