package com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel

import com.targaryen.cafeteria.core_network.util.AppLogger
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.GetChatMessagesUseCase
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.GetChatSessionsUseCase
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk()
    private val getChatSessionsUseCase: GetChatSessionsUseCase = mockk()
    private val getChatMessagesUseCase: GetChatMessagesUseCase = mockk()
    private val getCatalogUseCase: GetCatalogUseCase = mockk()
    private val appLogger: AppLogger = mockk()

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        every { getChatSessionsUseCase() } returns flowOf(emptyList())
        coJustRun { getChatSessionsUseCase.sync() }
        coEvery { getChatSessionsUseCase.getOrCreateSession(any(), any()) } returns "session_1"
        coEvery { getChatSessionsUseCase.deleteSession(any()) } returns null
        every { getChatMessagesUseCase(any()) } returns flowOf(emptyList())
        coJustRun { getChatMessagesUseCase.sync(any()) }
        coEvery { getChatMessagesUseCase.saveMessage(any(), any(), any()) } answers {
            ChatMessage(
                text = secondArg(),
                sender = if (thirdArg<String>() == ChatPromptConstants.SENDER_USER) ChatSender.USER else ChatSender.AI,
            )
        }
        every { getCatalogUseCase() } returns flowOf(emptyList())
        justRun { appLogger.e(any(), any(), any()) }

        viewModel =
            ChatViewModel(
                sendChatMessageUseCase = sendChatMessageUseCase,
                getChatSessionsUseCase = getChatSessionsUseCase,
                getChatMessagesUseCase = getChatMessagesUseCase,
                getCatalogUseCase = getCatalogUseCase,
                appLogger = appLogger,
            )
    }

    @Test
    fun onIntent_sendMessage_whenSuccessful_updatesUiStateAndPersists() =
        runTest {
            coEvery { sendChatMessageUseCase(any(), any()) } returns flowOf("Olá nobre cliente!")

            viewModel.onIntent(ChatIntent.SendMessage("Quais os cafés?"))

            coVerify {
                getChatMessagesUseCase.saveMessage(
                    sessionId = "session_1",
                    text = "Quais os cafés?",
                    sender = ChatPromptConstants.SENDER_USER,
                )
            }
            coVerify {
                getChatMessagesUseCase.saveMessage(
                    sessionId = "session_1",
                    text = "Olá nobre cliente!",
                    sender = ChatPromptConstants.SENDER_AI,
                )
            }
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun onIntent_sendMessage_whenQuotaExceeded_persistsFriendlyQuotaMessage() =
        runTest {
            coEvery { sendChatMessageUseCase(any(), any()) } returns
                flow {
                    throw IllegalStateException("Resource has been exhausted (e.g. check quota)")
                }

            viewModel.onIntent(ChatIntent.SendMessage("Mensagem pesada"))

            coVerify {
                getChatMessagesUseCase.saveMessage(
                    sessionId = "session_1",
                    text = ChatPromptConstants.REPLY_QUOTA_EXCEEDED,
                    sender = ChatPromptConstants.SENDER_AI,
                )
            }
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun onIntent_sendMessage_whenCheckoutActionReceived_triggersNavigationEvent() =
        runTest {
            coEvery { sendChatMessageUseCase(any(), any()) } returns
                flowOf(ChatPromptConstants.ACTION_NAVIGATE_TO_CHECKOUT)

            viewModel.onIntent(ChatIntent.SendMessage("Finalizar compra"))

            coVerify {
                getChatMessagesUseCase.saveMessage(
                    sessionId = "session_1",
                    text = ChatPromptConstants.REPLY_CHECKOUT_REDIRECT,
                    sender = ChatPromptConstants.SENDER_AI,
                )
            }
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun onIntent_selectSession_loadsMessagesAndUpdatesState() =
        runTest {
            val mockMessages =
                listOf(
                    ChatMessage(
                        id = "msg_1",
                        sender = ChatSender.USER,
                        text = "Mensagem do usuário",
                        timestamp = 1000L,
                    ),
                    ChatMessage(
                        id = "msg_2",
                        sender = ChatSender.AI,
                        text = "Resposta de Rhaenyra",
                        timestamp = 2000L,
                    ),
                )
            every { getChatMessagesUseCase("sess_1") } returns flowOf(mockMessages)

            viewModel.onIntent(ChatIntent.SelectSession("sess_1"))

            assertEquals("sess_1", viewModel.uiState.value.currentSessionId)
            val messages = viewModel.uiState.value.messages
            assertEquals(2, messages.size)
            assertEquals(ChatSender.USER, messages[0].sender)
            assertEquals(ChatSender.AI, messages[1].sender)
            coVerify { getChatMessagesUseCase.sync("sess_1") }
        }
}
