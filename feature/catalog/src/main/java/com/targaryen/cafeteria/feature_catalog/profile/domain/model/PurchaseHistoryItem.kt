package com.targaryen.cafeteria.feature_catalog.profile.domain.model

data class PurchaseHistoryItem(
    val id: String,
    val dateMillis: Long,
    val totalPrice: Double,
    val itemsSummary: String,
    val status: String
)
