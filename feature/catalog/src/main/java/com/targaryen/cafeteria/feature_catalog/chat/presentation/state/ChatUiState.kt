package com.targaryen.cafeteria.feature_catalog.chat.presentation.state

import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSessionId: String? = null,
    val sessions: List<ChatSessionEntity> = emptyList(),
    val showHistory: Boolean = false,
)
