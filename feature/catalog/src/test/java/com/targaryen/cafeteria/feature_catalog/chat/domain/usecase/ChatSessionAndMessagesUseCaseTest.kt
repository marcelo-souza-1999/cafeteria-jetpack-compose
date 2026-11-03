package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatSessionAndMessagesUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private lateinit var getChatSessionsUseCase: GetChatSessionsUseCase
    private lateinit var getChatMessagesUseCase: GetChatMessagesUseCase

    @Before
    fun setUp() {
        getChatSessionsUseCase = GetChatSessionsUseCase(chatRepository)
        getChatMessagesUseCase = GetChatMessagesUseCase(chatRepository)
    }

    @Test
    fun getChatSessionsUseCase_invoke_delegatesToRepository() =
        runTest {
            val sessions = listOf(ChatSessionEntity(id = "s1", title = "T1", userId = "u1", lastUpdated = 1L))
            every { chatRepository.observeSessions() } returns flowOf(sessions)

            val result = getChatSessionsUseCase().first()

            assertEquals(sessions, result)
        }

    @Test
    fun getChatSessionsUseCase_sync_delegatesToRepository() =
        runTest {
            coJustRun { chatRepository.syncSessions() }

            getChatSessionsUseCase.sync()

            coVerify { chatRepository.syncSessions() }
        }

    @Test
    fun getChatSessionsUseCase_getOrCreateSession_delegatesToRepository() =
        runTest {
            coEvery { chatRepository.getOrCreateSession("s1", "Msg") } returns "s1"

            val result = getChatSessionsUseCase.getOrCreateSession("s1", "Msg")

            assertEquals("s1", result)
        }

    @Test
    fun getChatSessionsUseCase_deleteSession_delegatesToRepository() =
        runTest {
            coEvery { chatRepository.deleteSession("s1") } returns "s2"

            val result = getChatSessionsUseCase.deleteSession("s1")

            assertEquals("s2", result)
        }

    @Test
    fun getChatMessagesUseCase_invoke_delegatesToRepository() =
        runTest {
            val messages = listOf(ChatMessage(id = "m1", text = "Oi", sender = ChatSender.USER))
            every { chatRepository.observeMessages("s1") } returns flowOf(messages)

            val result = getChatMessagesUseCase("s1").first()

            assertEquals(messages, result)
        }

    @Test
    fun getChatMessagesUseCase_sync_delegatesToRepository() =
        runTest {
            coJustRun { chatRepository.syncMessages("s1") }

            getChatMessagesUseCase.sync("s1")

            coVerify { chatRepository.syncMessages("s1") }
        }

    @Test
    fun getChatMessagesUseCase_saveMessage_delegatesToRepository() =
        runTest {
            val expected = ChatMessage(id = "m1", text = "Oi", sender = ChatSender.USER)
            coEvery {
                chatRepository.saveMessage("s1", "Oi", ChatPromptConstants.SENDER_USER)
            } returns expected

            val result = getChatMessagesUseCase.saveMessage("s1", "Oi", ChatPromptConstants.SENDER_USER)

            assertEquals(expected, result)
        }
}
