package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetChatSessionsUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(): Flow<List<ChatSessionEntity>> = chatRepository.observeSessions()

    suspend fun sync() {
        chatRepository.syncSessions()
    }

    suspend fun getOrCreateSession(
        activeSessionId: String?,
        firstMessageText: String,
    ): String = chatRepository.getOrCreateSession(activeSessionId, firstMessageText)

    suspend fun deleteSession(sessionId: String): String? = chatRepository.deleteSession(sessionId)
}
