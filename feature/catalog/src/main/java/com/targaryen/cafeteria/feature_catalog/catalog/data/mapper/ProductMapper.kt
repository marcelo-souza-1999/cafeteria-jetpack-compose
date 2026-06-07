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
    val dbTags = (this.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    val tags =
        dbTags.map { tag -> tag.lowercase() } +
            name.lowercase().split(" ", ",", ".", ";", ":", "-").map { part -> part.trim() } +
            desc.lowercase().split(" ", ",", ".", ";", ":", "-").map { part -> part.trim() }

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
        else -> determineDynamicCategory(tags)
    }

private fun determineDynamicCategory(tags: List<String>): String {
    val iceKeywords =
        setOf(
            "frio",
            "gelado",
            "ice",
            "cold",
            "glacial",
            "suco",
            "smoothie",
            "limonada",
            "néctar",
            "nectar",
        )
    val fireKeywords =
        setOf(
            "quente",
            "hot",
            "fire",
            "fogo",
            "expresso",
            "espresso",
            "mocha",
            "capuccino",
            "macchiato",
            "latte",
            "chocolate",
            "fervente",
        )

    val isIce = tags.any { tag -> iceKeywords.contains(tag) }
    val isFire = tags.any { tag -> fireKeywords.contains(tag) }

    return when {
        isIce -> CatalogCategories.ICE_BREATH
        isFire -> CatalogCategories.DRAGON_FIRE
        else -> CatalogCategories.ALL
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
