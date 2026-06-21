package com.targaryen.cafeteria.feature_catalog.chat.domain.model

enum class ChatSender { USER, AI }

data class ChatMessage(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val text: String,
    val sender: ChatSender,
    val timestamp: Long = System.currentTimeMillis(),
)
