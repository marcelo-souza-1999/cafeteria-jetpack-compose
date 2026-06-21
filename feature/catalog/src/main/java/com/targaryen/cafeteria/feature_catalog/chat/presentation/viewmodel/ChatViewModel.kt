package com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.data.remote.ChatRemoteDataSource
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.chat.presentation.state.ChatUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val chatDao: ChatDao,
    private val chatRemoteDataSource: ChatRemoteDataSource,
) : ViewModel() {
    val uiState: StateFlow<ChatUiState>
        field: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState())

    private var messagesJob: Job? = null

    init {
        loadSessions()
    }

    private fun loadSessions() {
        // Observar sessões locais
        viewModelScope.launch {
            chatDao.getAllSessions().collect { sessionsList ->
                uiState.update { state -> state.copy(sessions = sessionsList) }
                // Se não há uma sessão ativa selecionada, tenta carregar a mais recente
                if (uiState.value.currentSessionId == null && sessionsList.isNotEmpty()) {
                    selectSession(sessionsList.first().id)
                }
            }
        }

        // Sincronizar remotamente se estiver logado
        val userId = getUserId()
        if (userId != null) {
            viewModelScope.launch {
                val remoteSessions = chatRemoteDataSource.fetchSessions(userId)
                remoteSessions.forEach { session ->
                    chatDao.insertSession(session)
                    val remoteMessages = chatRemoteDataSource.fetchMessagesForSession(userId, session.id)
                    remoteMessages.forEach { msg ->
                        chatDao.insertMessage(msg)
                    }
                }
            }
        }
    }

    private fun observeMessages(sessionId: String) {
        messagesJob?.cancel()
        messagesJob =
            viewModelScope.launch {
                chatDao.getMessagesForSession(sessionId).collect { entities ->
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
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> sendMessage(intent.text)
            is ChatIntent.UpdateInputText -> {
                uiState.update { state -> state.copy(inputText = intent.text) }
            }
            is ChatIntent.ClearChat -> {
                val sessionId = uiState.value.currentSessionId
                if (sessionId != null) {
                    deleteSession(sessionId)
                }
            }
            is ChatIntent.NewSession -> startNewSession()
            is ChatIntent.SelectSession -> selectSession(intent.sessionId)
            is ChatIntent.ToggleHistory -> {
                uiState.update { state -> state.copy(showHistory = intent.show) }
            }
            is ChatIntent.DeleteSession -> deleteSession(intent.sessionId)
        }
    }

    private fun startNewSession() {
        messagesJob?.cancel()
        uiState.update { state ->
            state.copy(
                messages = emptyList(),
                currentSessionId = null,
            )
        }
    }

    private fun selectSession(sessionId: String) {
        uiState.update { state -> state.copy(currentSessionId = sessionId) }
        observeMessages(sessionId)
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userId = getUserId()
            val sessionId = getOrCreateSessionId(text, userId)

            persistAndSyncMessage(sessionId, text, "USER", userId)

            uiState.update { state ->
                state.copy(
                    inputText = "",
                    isLoading = true,
                    error = null,
                )
            }

            val history = uiState.value.messages
            sendChatMessageUseCase(text, history).collect { reply ->
                persistAndSyncMessage(sessionId, reply, "AI", userId)
                uiState.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    private suspend fun getOrCreateSessionId(
        firstMessageText: String,
        userId: String?,
    ): String {
        val activeSessionId = uiState.value.currentSessionId
        if (activeSessionId != null) {
            val session = uiState.value.sessions.find { it.id == activeSessionId }
            if (session != null) {
                val updatedSession = session.copy(lastUpdated = System.currentTimeMillis())
                chatDao.insertSession(updatedSession)
                if (userId != null) {
                    chatRemoteDataSource.saveSession(userId, updatedSession)
                }
            }
            return activeSessionId
        }

        val newSessionId =
            java.util.UUID
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
        chatDao.insertSession(session)
        if (userId != null) {
            chatRemoteDataSource.saveSession(userId, session)
        }
        uiState.update { state -> state.copy(currentSessionId = newSessionId) }
        observeMessages(newSessionId)
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
        val userId = getUserId()
        viewModelScope.launch {
            chatDao.deleteSession(sessionId)
            chatDao.deleteMessagesForSession(sessionId)
            if (userId != null) {
                chatRemoteDataSource.deleteSession(userId, sessionId)
            }

            if (uiState.value.currentSessionId == sessionId) {
                startNewSession()
                val remainingSessions = chatDao.getAllSessions().first()
                if (remainingSessions.isNotEmpty()) {
                    selectSession(remainingSessions.first().id)
                }
            }
        }
    }

    private fun getUserId(): String? =
        com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid

    companion object {
        private const val SESSION_TITLE_MAX_LENGTH = 20
    }
}
