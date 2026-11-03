package com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_network.util.AppLogger
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.GetChatMessagesUseCase
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.GetChatSessionsUseCase
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.chat.presentation.state.ChatUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val getChatSessionsUseCase: GetChatSessionsUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val getCatalogUseCase: GetCatalogUseCase,
    private val appLogger: AppLogger,
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
        viewModelScope.launch {
            getChatSessionsUseCase().collect { sessionsList ->
                uiState.update { state -> state.copy(sessions = sessionsList) }
                if (uiState.value.currentSessionId == null && sessionsList.isNotEmpty()) {
                    selectSession(sessionsList.first().id)
                }
            }
        }

        viewModelScope.launch {
            getChatSessionsUseCase.sync()
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
        uiState.update { state -> state.copy(currentSessionId = sessionId) }
        messagesJob?.cancel()
        messagesJob =
            viewModelScope.launch {
                getChatMessagesUseCase(sessionId).collect { domainMessages ->
                    uiState.update { state ->
                        state.copy(messages = domainMessages, currentSessionId = sessionId)
                    }
                }
            }

        viewModelScope.launch {
            getChatMessagesUseCase.sync(sessionId)
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val previousSessionId = uiState.value.currentSessionId
            val sessionId = getChatSessionsUseCase.getOrCreateSession(previousSessionId, text)
            if (previousSessionId == null) {
                selectSession(sessionId)
            }

            getChatMessagesUseCase.saveMessage(sessionId, text, ChatPromptConstants.SENDER_USER)

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
                    appLogger.e(TAG, ERROR_STREAM_FAILED, exception)
                    val errorReply =
                        if (exception.message?.contains("Quota") == true ||
                            exception.message?.contains("quota") == true ||
                            exception.toString().contains("Quota")
                        ) {
                            ChatPromptConstants.REPLY_QUOTA_EXCEEDED
                        } else {
                            ChatPromptConstants.REPLY_UNAVAILABLE_FALLBACK
                        }
                    getChatMessagesUseCase.saveMessage(sessionId, errorReply, ChatPromptConstants.SENDER_AI)
                    uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = exception.message ?: ChatPromptConstants.ERROR_UNKNOWN,
                        )
                    }
                }.collect { reply ->
                    if (reply == ChatPromptConstants.ACTION_NAVIGATE_TO_CHECKOUT) {
                        val friendlyReply = ChatPromptConstants.REPLY_CHECKOUT_REDIRECT
                        getChatMessagesUseCase.saveMessage(sessionId, friendlyReply, ChatPromptConstants.SENDER_AI)
                        _navigationEvents.send(ChatNavigationEvent.NavigateToCheckout)
                    } else {
                        getChatMessagesUseCase.saveMessage(sessionId, reply, ChatPromptConstants.SENDER_AI)
                    }
                    uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }

    private fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val nextSessionId = getChatSessionsUseCase.deleteSession(sessionId)

            if (uiState.value.currentSessionId == sessionId) {
                messagesJob?.cancel()
                uiState.update { state ->
                    state.copy(
                        messages = emptyList(),
                        currentSessionId = null,
                    )
                }
                if (nextSessionId != null) {
                    selectSession(nextSessionId)
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
        private const val TAG = "ChatViewModel"
        private const val ERROR_STREAM_FAILED = "Erro no fluxo do chat"
    }
}
