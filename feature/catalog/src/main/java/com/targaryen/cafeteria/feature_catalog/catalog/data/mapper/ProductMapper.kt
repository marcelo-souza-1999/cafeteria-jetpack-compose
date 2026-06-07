package com.targaryen.cafeteria.feature_catalog.catalog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import com.targaryen.cafeteria.coredatabase.model.ProductEntity
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product

fun DocumentSnapshot.toProductEntity(
    currentIsFavorite: Boolean = false,
    currentQuantity: Int = 0,
): ProductEntity {
    val rawCategory = this.getString("category") ?: ""
    val name = this.getString("name") ?: ""
    val desc = this.getString("description") ?: ""
    val tags = this.get("tags") as? List<String> ?: emptyList()

    val mappedCategory = mapCategory(rawCategory, tags)

    return ProductEntity(
        id = this.getString("id") ?: this.id,
        name = name,
        description = desc,
        price = this.getDouble("price") ?: 0.0,
        imageUrl = this.getString("imageUrl") ?: "",
        category = mappedCategory,
        isFavorite = currentIsFavorite,
        quantityInCart = currentQuantity,
    )
}

private fun mapCategory(
    rawCategory: String,
    tags: List<String>,
): String =
    when (rawCategory.lowercase()) {
        "comidas" -> CatalogCategories.ROYAL_FEAST
        "elixires" -> CatalogCategories.CROWN_ELIXIRS
        "frio" -> CatalogCategories.ICE_BREATH
        "quente" -> CatalogCategories.DRAGON_FIRE
        else -> {
            val isIce = tags.contains("frio") || tags.contains("gelado") || tags.contains("ice")
            val isFire = tags.contains("quente") || tags.contains("hot") || tags.contains("fire")

            if (isIce) {
                CatalogCategories.ICE_BREATH
            } else if (isFire) {
                CatalogCategories.DRAGON_FIRE
            } else {
                CatalogCategories.ALL
            }
        }
    }

fun ProductEntity.toDomain(): Product =
    Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        imageUrl = this.imageUrl,
        category = this.category,
        isFavorite = this.isFavorite,
        quantityInCart = this.quantityInCart,
    )
