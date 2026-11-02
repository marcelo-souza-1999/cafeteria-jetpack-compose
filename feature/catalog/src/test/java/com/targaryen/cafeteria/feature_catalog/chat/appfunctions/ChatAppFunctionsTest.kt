package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatAppFunctionsTest {
    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk(relaxed = true)
    private val appFunctionContext: AppFunctionContext = mockk(relaxed = true)
    private lateinit var chatAppFunctions: ChatAppFunctions

    @Before
    fun setUp() {
        chatAppFunctions = ChatAppFunctions(sendChatMessageUseCase)
    }

    @Test
    fun askRhaenyra_delegatesQueryToSendChatMessageUseCase_andReturnsResponse() =
        runTest {
            val query = "Qual a história da casa Targaryen?"
            val expectedReply = "Somos os senhores dos dragões de Valíria."

            every {
                sendChatMessageUseCase(query, emptyList())
            } returns flowOf(expectedReply)

            val request = ChatQueryRequest(query = query)
            val response = chatAppFunctions.askRhaenyra(appFunctionContext, request)

            assertEquals(expectedReply, response.reply)
            verify(exactly = 1) {
                sendChatMessageUseCase(query, emptyList())
            }
        }
}
