package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetChatMessagesUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(sessionId: String): Flow<List<ChatMessage>> = chatRepository.observeMessages(sessionId)

    suspend fun sync(sessionId: String) {
        chatRepository.syncMessages(sessionId)
    }

    suspend fun saveMessage(
        sessionId: String,
        text: String,
        sender: String,
    ): ChatMessage = chatRepository.saveMessage(sessionId, text, sender)
}
