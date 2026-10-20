package com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.chat.data.remote.ChatRemoteDataSource
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.chat.presentation.state.ChatUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import java.util.UUID

@KoinViewModel
class ChatViewModel(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val chatDao: ChatDao,
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val chatSessionProvider: ChatSessionProvider,
    private val getCatalogUseCase: GetCatalogUseCase,
) : ViewModel() {
    val uiState: StateFlow<ChatUiState>
        field: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState())

    sealed interface ChatNavigationEvent {
        object NavigateToCheckout : ChatNavigationEvent
    }

    private val _navigationEvents = Channel<ChatNavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    private var allProducts: List<ProductUiModel> = emptyList()

    private var messagesJob: Job? = null

    init {
        loadSessions()
        observeCatalog()
    }

    private fun loadSessions() {
        val userId = chatSessionProvider.getCurrentUserUid() ?: ""
        viewModelScope.launch {
            chatDao.getAllSessions(userId).collect { sessionsList ->
                uiState.update { state -> state.copy(sessions = sessionsList) }
                if (uiState.value.currentSessionId == null && sessionsList.isNotEmpty()) {
                    selectSession(sessionsList.first().id)
                }
            }
        }

        if (userId.isNotBlank()) {
            viewModelScope.launch {
                val remoteSessions = chatRemoteDataSource.fetchSessions(userId)
                remoteSessions.forEach { session ->
                    chatDao.insertSession(session)
                }
            }
        }
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> sendMessage(intent.text)
            is ChatIntent.UpdateInputText -> updateInputText(intent.text)
            is ChatIntent.ClearChat -> {
                val sessionId = uiState.value.currentSessionId
                if (sessionId != null) {
                    deleteSession(sessionId)
                }
            }

            is ChatIntent.NewSession -> {
                messagesJob?.cancel()
                uiState.update { state ->
                    state.copy(
                        messages = emptyList(),
                        currentSessionId = null,
                    )
                }
            }

            is ChatIntent.SelectSession -> selectSession(intent.sessionId)
            is ChatIntent.ToggleHistory -> {
                uiState.update { state -> state.copy(showHistory = intent.show) }
            }

            is ChatIntent.DeleteSession -> deleteSession(intent.sessionId)
            is ChatIntent.SelectProductSuggestion -> selectProductSuggestion(intent.product)
        }
    }

    private fun selectSession(sessionId: String) {
        android.util.Log.d("ChatViewModel", "selectSession invocada para ID: $sessionId")
        uiState.update { state -> state.copy(currentSessionId = sessionId) }
        messagesJob?.cancel()
        messagesJob =
            viewModelScope.launch {
                chatDao.getMessagesForSession(sessionId).collect { entities ->
                    android.util.Log.d(
                        "ChatViewModel",
                        "Room coletou ${entities.size} mensagens para a sessão: $sessionId",
                    )
                    val domainMessages =
                        entities.map { entity ->
                            ChatMessage(
                                id = entity.id,
                                text = entity.text,
                                sender = if (entity.sender == "USER") ChatSender.USER else ChatSender.AI,
                                timestamp = entity.timestamp,
                            )
                        }
                    uiState.update { state ->
                        state.copy(messages = domainMessages, currentSessionId = sessionId)
                    }
                }
            }

        val userId = chatSessionProvider.getCurrentUserUid()
        if (userId != null) {
            viewModelScope.launch {
                val remoteMessages = chatRemoteDataSource.fetchMessagesForSession(userId, sessionId)
                remoteMessages.forEach { msg ->
                    chatDao.insertMessage(msg)
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return
        android.util.Log.d("ChatViewModel", "sendMessage disparada. Texto: '$text'")
        viewModelScope.launch {
            val userId = chatSessionProvider.getCurrentUserUid()
            val sessionId = getOrCreateSessionId(text, userId)
            android.util.Log.d("ChatViewModel", "Sessão ativa para envio: $sessionId (Usuário: $userId)")

            persistAndSyncMessage(sessionId, text, "USER", userId)

            uiState.update { state ->
                state.copy(
                    inputText = "",
                    isLoading = true,
                    error = null,
                )
            }

            val history = uiState.value.messages
            sendChatMessageUseCase(text, history)
                .catch { exception ->
                    android.util.Log.e("ChatViewModel", "Erro no fluxo do chat", exception)
                    val errorReply =
                        if (exception.message?.contains("Quota") == true ||
                            exception.message?.contains("quota") == true ||
                            exception.toString().contains("Quota")
                        ) {
                            "Nobre cliente, as chamas da API excederam os limites do Trono neste momento. " +
                                "Por favor, aguarde alguns instantes (cerca de 1 minuto) antes de enviar uma nova mensagem."
                        } else {
                            "As chamas da Cidadela oscilaram e não consegui responder. " +
                                "Por favor, tente enviar novamente."
                        }
                    persistAndSyncMessage(sessionId, errorReply, "AI", userId)
                    uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = exception.message ?: "Erro desconhecido",
                        )
                    }
                }.collect { reply ->
                    if (reply == "[NAVIGATE_TO_CHECKOUT]") {
                        val friendlyReply =
                            "Com certeza, nobre cliente imperial. " +
                                "Estou direcionando você para a página de checkout."
                        persistAndSyncMessage(sessionId, friendlyReply, "AI", userId)
                        _navigationEvents.send(ChatNavigationEvent.NavigateToCheckout)
                    } else {
                        persistAndSyncMessage(sessionId, reply, "AI", userId)
                    }
                    uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }

    private suspend fun getOrCreateSessionId(
        firstMessageText: String,
        userId: String?,
    ): String {
        val activeSessionId = uiState.value.currentSessionId
        android.util.Log.d("ChatViewModel", "getOrCreateSessionId: activeSessionId do estado = $activeSessionId")
        if (activeSessionId != null) {
            val session = uiState.value.sessions.find { it.id == activeSessionId }
            android.util.Log.d(
                "ChatViewModel",
                "getOrCreateSessionId: sessão encontrada na lista do estado = ${session != null}",
            )
            if (session != null) {
                val updatedTimestamp = System.currentTimeMillis()
                chatDao.updateSessionTimestamp(activeSessionId, updatedTimestamp)
                if (userId != null) {
                    val updatedSession = session.copy(lastUpdated = updatedTimestamp)
                    chatRemoteDataSource.saveSession(userId, updatedSession)
                }
            }
            return activeSessionId
        }

        val newSessionId =
            UUID
                .randomUUID()
                .toString()
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
        uiState.update { state -> state.copy(currentSessionId = newSessionId) }
        selectSession(newSessionId)

        chatDao.insertSession(session)
        if (userId != null) {
            chatRemoteDataSource.saveSession(userId, session)
        }
        return newSessionId
    }

    private suspend fun persistAndSyncMessage(
        sessionId: String,
        text: String,
        sender: String,
        userId: String?,
    ): ChatMessage {
        val message =
            ChatMessage(
                text = text,
                sender = if (sender == "USER") ChatSender.USER else ChatSender.AI,
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

    private fun deleteSession(sessionId: String) {
        val userId = chatSessionProvider.getCurrentUserUid()
        viewModelScope.launch {
            chatDao.deleteSession(sessionId)
            chatDao.deleteMessagesForSession(sessionId)
            if (userId != null) {
                chatRemoteDataSource.deleteSession(userId, sessionId)
            }

            if (uiState.value.currentSessionId == sessionId) {
                messagesJob?.cancel()
                uiState.update { state ->
                    state.copy(
                        messages = emptyList(),
                        currentSessionId = null,
                    )
                }
                val remainingSessions = chatDao.getAllSessions(userId ?: "").first()
                if (remainingSessions.isNotEmpty()) {
                    selectSession(remainingSessions.first().id)
                }
            }
        }
    }

    private fun observeCatalog() {
        viewModelScope.launch {
            getCatalogUseCase().collect { products ->
                allProducts =
                    products.map { domainProduct ->
                        ProductUiModel(
                            id = domainProduct.id,
                            name = domainProduct.name,
                            description = domainProduct.description,
                            price = domainProduct.price,
                            category = domainProduct.category,
                            imageUrl = domainProduct.imageUrl,
                            isFavorite = domainProduct.isFavorite,
                            quantityInCart = domainProduct.quantityInCart,
                        )
                    }
            }
        }
    }

    private fun updateInputText(text: String) {
        val lastAtIndex = text.lastIndexOf('@')
        val suggestions =
            if (lastAtIndex >= 0) {
                val query = text.substring(lastAtIndex + 1)
                allProducts.filter { product ->
                    product.name.contains(query, ignoreCase = true)
                }
            } else {
                emptyList()
            }
        uiState.update { state ->
            state.copy(
                inputText = text,
                productSuggestions = suggestions,
            )
        }
    }

    private fun selectProductSuggestion(product: ProductUiModel) {
        val text = uiState.value.inputText
        val lastAtIndex = text.lastIndexOf('@')
        val newText =
            if (lastAtIndex >= 0) {
                text.substring(0, lastAtIndex) + product.name + " "
            } else {
                text + product.name + " "
            }
        uiState.update { state ->
            state.copy(
                inputText = newText,
                productSuggestions = emptyList(),
            )
        }
    }

    companion object {
        private const val SESSION_TITLE_MAX_LENGTH = 20
    }
}
