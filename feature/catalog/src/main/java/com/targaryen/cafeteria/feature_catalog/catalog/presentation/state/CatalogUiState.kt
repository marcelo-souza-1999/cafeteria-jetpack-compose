package com.targaryen.cafeteria.feature_catalog.catalog.presentation.state

import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel

data class CatalogUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Todos",
    val products: List<ProductUiModel> = emptyList(),
    val favoriteProducts: List<ProductUiModel> = emptyList(),
    val selectedProduct: ProductUiModel? = null,
    val badgeCount: Int = 0,
)
