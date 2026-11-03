package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.data.remote.ChatRemoteDataSource
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Single
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private const val CHAT_TEMPERATURE = 0.2f

@Single
class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val chatSessionProvider: ChatSessionProvider,
) : ChatRepository {
    private val addProductToCartTool =
        FunctionDeclaration(
            name = FN_ADD_PRODUCT_TO_CART,
            description = FN_ADD_PRODUCT_TO_CART_DESC,
            parameters =
                mapOf(
                    PARAM_PRODUCT_NAME to Schema.string(PARAM_PRODUCT_NAME_DESC),
                    PARAM_QUANTITY to Schema.integer(PARAM_QUANTITY_DESC),
                ),
            optionalParameters = emptyList(),
        )

    private val saveDeliveryAddressTool =
        FunctionDeclaration(
            name = FN_SAVE_DELIVERY_ADDRESS,
            description = FN_SAVE_DELIVERY_ADDRESS_DESC,
            parameters =
                mapOf(
                    PARAM_CEP to Schema.string(PARAM_CEP_DESC),
                    PARAM_STREET to Schema.string(PARAM_STREET_DESC),
                    PARAM_CITY to Schema.string(PARAM_CITY_DESC),
                    PARAM_STATE to Schema.string(PARAM_STATE_DESC),
                    PARAM_NUMBER to Schema.string(PARAM_NUMBER_DESC),
                    PARAM_REFERENCE_POINT to Schema.string(PARAM_REFERENCE_POINT_DESC),
                    PARAM_RECIPIENT_NAME to Schema.string(PARAM_RECIPIENT_NAME_DESC),
                ),
            optionalParameters = listOf(PARAM_STREET, PARAM_CITY, PARAM_STATE),
        )

    private val navigateToCheckoutTool =
        FunctionDeclaration(
            name = FN_NAVIGATE_TO_CHECKOUT,
            description = FN_NAVIGATE_TO_CHECKOUT_DESC,
            parameters = emptyMap(),
            optionalParameters = emptyList(),
        )

    private val modelCache = ConcurrentHashMap<String, GenerativeModel>()

    override fun sendMessage(
        prompt: String,
        systemInstruction: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): Flow<String> =
        flow {
            if (prompt.isBlank()) {
                emit("")
                return@flow
            }
            val primaryModel = getOrCreateGenerativeModel(PRIMARY_MODEL_NAME, systemInstruction)
            try {
                emit(generateContentWithModel(primaryModel, prompt, onFunctionCall))
            } catch (primaryException: FirebaseAIException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            } catch (primaryException: SerializationException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            } catch (primaryException: IOException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            }
        }

    override fun observeSessions(): Flow<List<ChatSessionEntity>> {
        val userId = chatSessionProvider.getCurrentUserUid().orEmpty()
        return chatDao.getAllSessions(userId)
    }

    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    text = entity.text,
                    sender =
                        if (entity.sender == ChatPromptConstants.SENDER_USER) {
                            ChatSender.USER
                        } else {
                            ChatSender.AI
                        },
                    timestamp = entity.timestamp,
                )
            }
        }

    override suspend fun syncSessions() {
        val userId = chatSessionProvider.getCurrentUserUid().orEmpty()
        if (userId.isNotBlank()) {
            val remoteSessions = chatRemoteDataSource.fetchSessions(userId)
            remoteSessions.forEach { session ->
                chatDao.insertSession(session)
            }
        }
    }

    override suspend fun syncMessages(sessionId: String) {
        val userId = chatSessionProvider.getCurrentUserUid()
        if (userId != null) {
            val remoteMessages = chatRemoteDataSource.fetchMessagesForSession(userId, sessionId)
            remoteMessages.forEach { msg ->
                chatDao.insertMessage(msg)
            }
        }
    }

    override suspend fun getOrCreateSession(
        activeSessionId: String?,
        firstMessageText: String,
    ): String {
        val userId = chatSessionProvider.getCurrentUserUid()
        if (activeSessionId != null) {
            val updatedTimestamp = System.currentTimeMillis()
            chatDao.updateSessionTimestamp(activeSessionId, updatedTimestamp)
            if (userId != null) {
                val existingSession =
                    chatDao.getAllSessions(userId).first().find { item -> item.id == activeSessionId }
                if (existingSession != null) {
                    chatRemoteDataSource.saveSession(userId, existingSession.copy(lastUpdated = updatedTimestamp))
                }
            }
            return activeSessionId
        }

        val newSessionId = UUID.randomUUID().toString()
        val title =
            if (firstMessageText.length > SESSION_TITLE_MAX_LENGTH) {
                firstMessageText.take(SESSION_TITLE_MAX_LENGTH) + "..."
            } else {
                firstMessageText
            }
        val session =
            ChatSessionEntity(
                id = newSessionId,
                title = title,
                userId = userId,
                lastUpdated = System.currentTimeMillis(),
            )
        chatDao.insertSession(session)
        if (userId != null) {
            chatRemoteDataSource.saveSession(userId, session)
        }
        return newSessionId
    }

    override suspend fun saveMessage(
        sessionId: String,
        text: String,
        sender: String,
    ): ChatMessage {
        val userId = chatSessionProvider.getCurrentUserUid()
        val message =
            ChatMessage(
                text = text,
                sender = if (sender == ChatPromptConstants.SENDER_USER) ChatSender.USER else ChatSender.AI,
            )
        val entity =
            ChatMessageEntity(
                id = message.id,
                sessionId = sessionId,
                text = message.text,
                sender = sender,
                timestamp = message.timestamp,
            )
        chatDao.insertMessage(entity)
        if (userId != null) {
            chatRemoteDataSource.saveMessage(userId, sessionId, entity)
        }
        return message
    }

    override suspend fun deleteSession(sessionId: String): String? {
        val userId = chatSessionProvider.getCurrentUserUid()
        chatDao.deleteSession(sessionId)
        if (userId != null) {
            chatRemoteDataSource.deleteSession(userId, sessionId)
        }
        val remainingSessions = chatDao.getAllSessions(userId.orEmpty()).first()
        return remainingSessions.firstOrNull()?.id
    }

    private suspend fun tryFallbackOrThrow(
        primaryException: Exception,
        systemInstruction: String,
        prompt: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): String {
        if (!shouldFallback(primaryException)) {
            handleModelException(primaryException)
        }
        val fallbackModel = getOrCreateGenerativeModel(FALLBACK_MODEL_NAME, systemInstruction)
        return try {
            generateContentWithModel(fallbackModel, prompt, onFunctionCall)
        } catch (fallbackException: FirebaseAIException) {
            handleModelException(fallbackException)
        } catch (fallbackException: SerializationException) {
            handleModelException(fallbackException)
        } catch (fallbackException: IOException) {
            handleModelException(fallbackException)
        }
    }

    private fun getOrCreateGenerativeModel(
        modelName: String,
        systemInstruction: String,
    ): GenerativeModel {
        val cacheKey = "$modelName:${systemInstruction.hashCode()}"
        return modelCache.computeIfAbsent(cacheKey) {
            Firebase.ai.generativeModel(
                modelName = modelName,
                generationConfig =
                    generationConfig {
                        temperature = CHAT_TEMPERATURE
                        thinkingConfig =
                            thinkingConfig {
                                thinkingLevel = ThinkingLevel.MINIMAL
                            }
                    },
                systemInstruction =
                    content {
                        text(systemInstruction)
                    },
                tools =
                    listOf(
                        Tool.functionDeclarations(
                            listOf(addProductToCartTool, saveDeliveryAddressTool, navigateToCheckoutTool),
                        ),
                    ),
            )
        }
    }
}
