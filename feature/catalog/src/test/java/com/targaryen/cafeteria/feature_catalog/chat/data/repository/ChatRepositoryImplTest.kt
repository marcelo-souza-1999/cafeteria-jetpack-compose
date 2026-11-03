package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.feature_catalog.chat.data.remote.ChatRemoteDataSource
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {
    private val chatDao: ChatDao = mockk()
    private val chatRemoteDataSource: ChatRemoteDataSource = mockk()
    private val chatSessionProvider: ChatSessionProvider = mockk()

    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        every { chatSessionProvider.getCurrentUserUid() } returns "user_imperial_1"
        every { chatDao.getAllSessions(any()) } returns flowOf(emptyList())
        every { chatDao.getMessagesForSession(any()) } returns flowOf(emptyList())
        coJustRun { chatDao.insertSession(any()) }
        coJustRun { chatDao.insertMessage(any()) }
        coJustRun { chatDao.updateSessionTimestamp(any(), any()) }
        coJustRun { chatDao.deleteSession(any()) }
        coJustRun { chatRemoteDataSource.saveSession(any(), any()) }
        coJustRun { chatRemoteDataSource.saveMessage(any(), any(), any()) }
        coJustRun { chatRemoteDataSource.deleteSession(any(), any()) }
        coEvery { chatRemoteDataSource.fetchSessions(any()) } returns emptyList()
        coEvery { chatRemoteDataSource.fetchMessagesForSession(any(), any()) } returns emptyList()

        repository =
            ChatRepositoryImpl(
                chatDao = chatDao,
                chatRemoteDataSource = chatRemoteDataSource,
                chatSessionProvider = chatSessionProvider,
            )
    }

    @Test
    fun sendMessage_withBlankPrompt_emitsEmptyString() =
        runTest {
            val result =
                repository
                    .sendMessage(
                        prompt = "",
                        systemInstruction = "Instrução",
                        onFunctionCall = { _, _ -> "" },
                    ).first()

            assertEquals("", result)
        }

    @Test
    fun sendMessage_withWhitespacePrompt_emitsEmptyString() =
        runTest {
            val result =
                repository
                    .sendMessage(
                        prompt = "    ",
                        systemInstruction = "Instrução",
                        onFunctionCall = { _, _ -> "" },
                    ).first()

            assertEquals("", result)
        }

    @Test
    fun observeMessages_mapsEntitiesToDomainMessagesCorrectly() =
        runTest {
            val mockEntities =
                listOf(
                    ChatMessageEntity(
                        id = "msg_1",
                        sessionId = "sess_1",
                        sender = ChatPromptConstants.SENDER_USER,
                        text = "Dracarys",
                        timestamp = 1000L,
                    ),
                    ChatMessageEntity(
                        id = "msg_2",
                        sessionId = "sess_1",
                        sender = ChatPromptConstants.SENDER_AI,
                        text = "Fogo e Sangue",
                        timestamp = 2000L,
                    ),
                )
            every { chatDao.getMessagesForSession("sess_1") } returns flowOf(mockEntities)

            val messages = repository.observeMessages("sess_1").first()

            assertEquals(2, messages.size)
            assertEquals(ChatSender.USER, messages[0].sender)
            assertEquals(ChatSender.AI, messages[1].sender)
        }

    @Test
    fun getOrCreateSession_whenActiveSessionNull_createsNewSessionAndSyncs() =
        runTest {
            val newSessionId = repository.getOrCreateSession(null, "Quero um café forte valiriano agora")

            assertTrue(newSessionId.isNotBlank())
            coVerify {
                chatDao.insertSession(
                    match { session ->
                        session.id == newSessionId && session.title.endsWith("...")
                    },
                )
            }
            coVerify { chatRemoteDataSource.saveSession("user_imperial_1", any()) }
        }

    @Test
    fun saveMessage_persistsLocallyAndRemotely() =
        runTest {
            val saved = repository.saveMessage("sess_1", "Olá Rhaenyra", ChatPromptConstants.SENDER_USER)

            assertEquals("Olá Rhaenyra", saved.text)
            assertEquals(ChatSender.USER, saved.sender)
            coVerify { chatDao.insertMessage(match { entity -> entity.text == "Olá Rhaenyra" }) }
            coVerify { chatRemoteDataSource.saveMessage("user_imperial_1", "sess_1", any()) }
        }

    @Test
    fun deleteSession_deletesAndReturnsNextAvailableSessionId() =
        runTest {
            val remaining =
                listOf(
                    ChatSessionEntity(
                        id = "sess_remaining",
                        title = "Anterior",
                        userId = "user_imperial_1",
                        lastUpdated = 10L,
                    ),
                )
            every { chatDao.getAllSessions("user_imperial_1") } returns flowOf(remaining)

            val nextId = repository.deleteSession("sess_deleted")

            assertEquals("sess_remaining", nextId)
            coVerify { chatDao.deleteSession("sess_deleted") }
            coVerify { chatRemoteDataSource.deleteSession("user_imperial_1", "sess_deleted") }
        }
}
