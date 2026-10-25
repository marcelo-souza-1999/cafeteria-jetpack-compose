package com.targaryen.cafeteria.coredatabase

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.database.TargaryenDatabase
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDaoTest {
    private lateinit var database: TargaryenDatabase
    private lateinit var chatDao: ChatDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    TargaryenDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        chatDao = database.chatDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertSessionAndGetAllSessions_returnsOrderedByLastUpdated() =
        runTest {
            val session1 =
                ChatSessionEntity(
                    id = "session_old",
                    title = "Primeiro Banquete",
                    userId = "user_valyria",
                    lastUpdated = 1000L,
                )
            val session2 =
                ChatSessionEntity(
                    id = "session_new",
                    title = "Segundo Banquete",
                    userId = "user_valyria",
                    lastUpdated = 5000L,
                )

            chatDao.insertSession(session1)
            chatDao.insertSession(session2)

            val sessions = chatDao.getAllSessions("user_valyria").first()
            assertEquals(2, sessions.size)
            assertEquals("session_new", sessions[0].id)
            assertEquals("session_old", sessions[1].id)
        }

    @Test
    fun insertMessagesAndGetForSession_returnsMessagesOrdered() =
        runTest {
            val session =
                ChatSessionEntity(
                    id = "session_chat",
                    title = "Conversa Real",
                    userId = "user_valyria",
                    lastUpdated = 2000L,
                )
            chatDao.insertSession(session)

            val msg1 =
                ChatMessageEntity(
                    id = "msg_1",
                    sessionId = "session_chat",
                    text = "Desejo café",
                    sender = "USER",
                    timestamp = 2100L,
                )
            val msg2 =
                ChatMessageEntity(
                    id = "msg_2",
                    sessionId = "session_chat",
                    text = "Seu café será servido!",
                    sender = "AI",
                    timestamp = 2200L,
                )

            chatDao.insertMessage(msg1)
            chatDao.insertMessage(msg2)

            val messages = chatDao.getMessagesForSession("session_chat").first()
            assertEquals(2, messages.size)
            assertEquals("Desejo café", messages[0].text)
            assertEquals("Seu café será servido!", messages[1].text)
        }

    @Test
    fun deleteSession_cascadesAndRemovesAssociatedMessages() =
        runTest {
            val session =
                ChatSessionEntity(
                    id = "session_to_delete",
                    title = "Sessão Condenada",
                    userId = "user_valyria",
                    lastUpdated = 3000L,
                )
            chatDao.insertSession(session)

            val msg =
                ChatMessageEntity(
                    id = "msg_cascade",
                    sessionId = "session_to_delete",
                    text = "Mensagem que deve perecer",
                    sender = "USER",
                    timestamp = 3100L,
                )
            chatDao.insertMessage(msg)

            // Deleta a sessão pai
            chatDao.deleteSession("session_to_delete")

            val sessions = chatDao.getAllSessions("user_valyria").first()
            assertTrue(sessions.isEmpty())

            val messages = chatDao.getMessagesForSession("session_to_delete").first()
            assertTrue("Mensagens associadas à sessão devem ser removidas em cascata", messages.isEmpty())
        }
}
