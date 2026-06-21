package com.targaryen.cafeteria.coredatabase.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val userId: String?,
    val lastUpdated: Long,
)
