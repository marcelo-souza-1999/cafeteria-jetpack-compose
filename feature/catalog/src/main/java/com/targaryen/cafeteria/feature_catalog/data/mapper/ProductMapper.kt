package com.targaryen.cafeteria.feature_catalog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.targaryen.cafeteria.core_designsystem.model.CatalogConstants
import com.targaryen.cafeteria.coredatabase.model.ProductEntity
import com.targaryen.cafeteria.feature_catalog.domain.model.Product

fun DocumentSnapshot.toProductEntity(
    currentIsFavorite: Boolean = false,
    currentQuantity: Int = 0
): ProductEntity {
    val rawCategory = this.getString("category") ?: ""
    val name = this.getString("name") ?: ""
    val desc = this.getString("description") ?: ""

    val mappedCategory = when (rawCategory.lowercase()) {
        "comidas" -> CatalogConstants.CATEGORY_ROYAL_FEAST
        "elixires" -> CatalogConstants.CATEGORY_CROWN_ELIXIRS
        "bebidas" -> {
            val terms = listOf("glacial", "cold", "gelado", "winterfell", "frost", "gelo")
            val isIce = terms.any {
                name.contains(it, ignoreCase = true) || desc.contains(
                    it,
                    ignoreCase = true
                )
            }
            if (isIce) CatalogConstants.CATEGORY_ICE_BREATH
            else CatalogConstants.CATEGORY_DRAGON_FIRE
        }

        else -> CatalogConstants.CATEGORY_ALL
    }

    return ProductEntity(
        id = this.getString("id") ?: this.id,
        name = name,
        description = desc,
        price = this.getDouble("price") ?: 0.0,
        imageUrl = this.getString("imageUrl") ?: "",
        category = mappedCategory,
        isFavorite = currentIsFavorite,
        quantityInCart = currentQuantity
    )
}

fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        imageUrl = this.imageUrl,
        category = this.category,
        isFavorite = this.isFavorite,
        quantityInCart = this.quantityInCart
    )
}
