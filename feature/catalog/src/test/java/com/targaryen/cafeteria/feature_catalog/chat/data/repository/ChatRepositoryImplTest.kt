package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        repository = ChatRepositoryImpl()
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
}
