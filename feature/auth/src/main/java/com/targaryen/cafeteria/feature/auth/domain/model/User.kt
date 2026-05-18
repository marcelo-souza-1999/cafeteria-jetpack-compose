package com.targaryen.cafeteria.feature.auth.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null
)
