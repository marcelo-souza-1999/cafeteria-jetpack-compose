package com.targaryen.cafeteria.coredatabase.model

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId"])],
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val text: String,
    val sender: String, // "USER" ou "AI"
    val timestamp: Long,
)
