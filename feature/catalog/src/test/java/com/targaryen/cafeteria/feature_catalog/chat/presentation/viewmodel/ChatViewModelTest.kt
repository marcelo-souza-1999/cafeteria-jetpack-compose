package com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel

import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.chat.data.remote.ChatRemoteDataSource
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk(relaxed = true)
    private val getCatalogUseCase: GetCatalogUseCase = mockk(relaxed = true)
    private val chatDao: ChatDao = mockk(relaxed = true)
    private val chatRemoteDataSource: ChatRemoteDataSource = mockk(relaxed = true)
    private val chatSessionProvider: ChatSessionProvider = mockk(relaxed = true)

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0

        every { chatSessionProvider.getCurrentUserUid() } returns "user_imperial_1"
        every { chatDao.getAllSessions(any()) } returns flowOf(emptyList())
        every { chatDao.getMessagesForSession(any()) } returns flowOf(emptyList())
        every { getCatalogUseCase() } returns flowOf(emptyList())

        viewModel =
            ChatViewModel(
                sendChatMessageUseCase = sendChatMessageUseCase,
                getCatalogUseCase = getCatalogUseCase,
                chatDao = chatDao,
                chatRemoteDataSource = chatRemoteDataSource,
                chatSessionProvider = chatSessionProvider,
            )
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun onIntent_sendMessage_whenSuccessful_updatesUiStateAndPersists() =
        runTest {
            coEvery { sendChatMessageUseCase(any(), any()) } returns flowOf("Olá nobre cliente!")

            viewModel.onIntent(ChatIntent.SendMessage("Quais os cafés?"))

            coVerify {
                chatDao.insertMessage(
                    match { entity ->
                        entity.sender == ChatPromptConstants.SENDER_USER && entity.text == "Quais os cafés?"
                    },
                )
            }
            coVerify {
                chatDao.insertMessage(
                    match { entity ->
                        entity.sender == ChatPromptConstants.SENDER_AI && entity.text == "Olá nobre cliente!"
                    },
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
                chatDao.insertMessage(
                    match { entity ->
                        entity.sender == ChatPromptConstants.SENDER_AI &&
                            entity.text == ChatPromptConstants.REPLY_QUOTA_EXCEEDED
                    },
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
                chatDao.insertMessage(
                    match { entity ->
                        entity.sender == ChatPromptConstants.SENDER_AI &&
                            entity.text == ChatPromptConstants.REPLY_CHECKOUT_REDIRECT
                    },
                )
            }
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun onIntent_selectSession_loadsMessagesAndMapsSendersCorrectly() =
        runTest {
            val mockEntities =
                listOf(
                    ChatMessageEntity(
                        id = "msg_1",
                        sessionId = "sess_1",
                        sender = ChatPromptConstants.SENDER_USER,
                        text = "Mensagem do usuário",
                        timestamp = 1000L,
                    ),
                    ChatMessageEntity(
                        id = "msg_2",
                        sessionId = "sess_1",
                        sender = ChatPromptConstants.SENDER_AI,
                        text = "Resposta de Rhaenyra",
                        timestamp = 2000L,
                    ),
                )
            every { chatDao.getMessagesForSession("sess_1") } returns flowOf(mockEntities)

            viewModel.onIntent(ChatIntent.SelectSession("sess_1"))

            assertEquals("sess_1", viewModel.uiState.value.currentSessionId)
            val messages = viewModel.uiState.value.messages
            assertEquals(2, messages.size)
            assertEquals(ChatSender.USER, messages[0].sender)
            assertEquals(ChatSender.AI, messages[1].sender)
        }
}
