package com.targaryen.cafeteria.feature_catalog.chat.domain.repository

import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(
        prompt: String,
        systemInstruction: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): Flow<String>

    fun observeSessions(): Flow<List<ChatSessionEntity>>

    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>

    suspend fun syncSessions()

    suspend fun syncMessages(sessionId: String)

    suspend fun getOrCreateSession(
        activeSessionId: String?,
        firstMessageText: String,
    ): String

    suspend fun saveMessage(
        sessionId: String,
        text: String,
        sender: String,
    ): ChatMessage

    suspend fun deleteSession(sessionId: String): String?
}
