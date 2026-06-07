package com.targaryen.cafeteria.coredatabase.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isFavorite: Boolean = false,
    val quantityInCart: Int = 0,
)
