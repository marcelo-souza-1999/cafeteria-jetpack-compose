package com.targaryen.cafeteria.feature_catalog.chat.domain.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(
        prompt: String,
        systemInstruction: String,
    ): Flow<String>
}
