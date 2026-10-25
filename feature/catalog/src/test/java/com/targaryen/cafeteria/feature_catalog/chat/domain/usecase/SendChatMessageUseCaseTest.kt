package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.coredatabase.address.AddressProvider
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendChatMessageUseCaseTest {
    private val catalogRepository: CatalogRepository = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val addressProvider: AddressProvider = mockk(relaxed = true)

    private lateinit var useCase: SendChatMessageUseCase

    private val mockProducts =
        listOf(
            Product(
                id = "1",
                name = "Café Expresso Valíria",
                description = "Forte",
                price = 10.0,
                imageUrl = "",
                category = "bebidas",
                isFavorite = false,
                quantityInCart = 0,
            ),
        )

    @Before
    fun setUp() {
        every { catalogRepository.getProducts() } returns flowOf(mockProducts)
        useCase = SendChatMessageUseCase(catalogRepository, chatRepository, addressProvider)
    }

    @Test
    fun invoke_injectsCatalogContextAndFormatsHistoryCorrectly() =
        runTest {
            val systemInstructionSlot = slot<String>()
            val promptSlot = slot<String>()

            every {
                chatRepository.sendMessage(
                    prompt = capture(promptSlot),
                    systemInstruction = capture(systemInstructionSlot),
                    onFunctionCall = any(),
                )
            } returns flowOf("Resposta da IA")

            val history =
                listOf(
                    ChatMessage(
                        id = "msg1",
                        text = "Qual o melhor café?",
                        sender = ChatSender.USER,
                        timestamp = 1000L,
                    ),
                    ChatMessage(
                        id = "msg2",
                        text = "O Expresso Valíria!",
                        sender = ChatSender.AI,
                        timestamp = 2000L,
                    ),
                )

            val result = useCase("Me dê um!", history).first()

            assertEquals("Resposta da IA", result)
            assertTrue(
                "Instrução do sistema deve conter o catálogo real de produtos",
                systemInstructionSlot.captured.contains(
                    "Café Expresso Valíria: 10.0 ${ChatPromptConstants.PRICE_SUFFIX}",
                ),
            )
            assertTrue(
                "Prompt formatado deve conter o histórico e a nova mensagem do usuário",
                promptSlot.captured.contains("${ChatPromptConstants.SPEAKER_USER}: Qual o melhor café?"),
            )
            assertTrue(
                "Prompt formatado deve conter a resposta anterior da IA",
                promptSlot.captured.contains("${ChatPromptConstants.SPEAKER_AI}: O Expresso Valíria!"),
            )
            assertTrue(
                "Prompt formatado deve conter o novo comando",
                promptSlot.captured.contains("${ChatPromptConstants.SPEAKER_USER}: Me dê um!"),
            )
        }
}
