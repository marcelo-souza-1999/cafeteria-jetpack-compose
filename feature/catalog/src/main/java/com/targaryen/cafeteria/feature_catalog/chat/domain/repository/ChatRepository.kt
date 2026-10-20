package com.targaryen.cafeteria.feature_catalog.chat.domain.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(
        prompt: String,
        systemInstruction: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): Flow<String>
}
