package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

@Factory
class SendChatMessageUseCase(
    private val catalogRepository: CatalogRepository,
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(
        prompt: String,
        history: List<ChatMessage>,
    ): Flow<String> =
        flow {
            val products = catalogRepository.getProducts().first()
            val catalogString =
                products.joinToString(separator = "\n") { product ->
                    "- ${product.name}: ${product.price} ${ChatPromptConstants.PRICE_SUFFIX}"
                }

            val systemInstruction =
                """
                ${ChatPromptConstants.SYSTEM_INSTRUCTION.trimIndent()}
                
                CONTEXTO REAL DO CARDÁPIO DE PRODUTOS DA CAFETERIA:
                $catalogString
                """.trimIndent()

            val formattedPrompt =
                buildString {
                    history.takeLast(ChatPromptConstants.MAX_HISTORY_LIMIT).forEach { message ->
                        val speaker =
                            if (message.sender == ChatSender.USER) {
                                ChatPromptConstants.SPEAKER_USER
                            } else {
                                ChatPromptConstants.SPEAKER_AI
                            }
                        append("$speaker: ${message.text}\n")
                    }
                    append("${ChatPromptConstants.SPEAKER_USER}: $prompt\n")
                    append("${ChatPromptConstants.SPEAKER_AI}: ")
                }

            chatRepository.sendMessage(formattedPrompt, systemInstruction).collect { reply ->
                emit(reply)
            }
        }
}
