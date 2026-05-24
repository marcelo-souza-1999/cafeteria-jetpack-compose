package com.targaryen.cafeteria.feature_catalog.presentation.model

data class ProductUiModel(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val quantityInCart: Int = 0,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
)
