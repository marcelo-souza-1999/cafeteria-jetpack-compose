package com.targaryen.cafeteria.core_database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
