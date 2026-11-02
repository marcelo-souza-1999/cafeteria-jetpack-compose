package com.targaryen.cafeteria.feature_catalog.chat.presentation.view

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.chat.presentation.state.ChatUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun chatScreen_displaysWelcomeMessage_whenMessagesEmpty() {
        val welcomeText = context.getString(R.string.chat_welcome_message)

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = emptyList()),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText(welcomeText).assertIsDisplayed()
    }

    @Test
    fun chatScreen_displaysChatMessages() {
        val userMsg = "Qual o café mais forte?"
        val aiMsg = "O Café Expresso Valíria possui o fogo mais intenso de Westeros."
        val messages =
            listOf(
                ChatMessage(id = "1", text = userMsg, sender = ChatSender.USER),
                ChatMessage(id = "2", text = aiMsg, sender = ChatSender.AI),
            )

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = messages),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText(userMsg).assertIsDisplayed()
        composeTestRule.onNodeWithText(aiMsg).assertIsDisplayed()
    }

    @Test
    fun chatScreen_displaysThinkingIndicator_whenLoading() {
        val thinkingText = context.getString(R.string.chat_thinking_indicator)

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(isLoading = true),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText(thinkingText).assertIsDisplayed()
    }

    @Test
    fun chatScreen_displaysProductSuggestions_andHandlesClick() {
        var selectedSuggestion: ChatIntent? = null
        val testProduct =
            ProductUiModel(
                id = "prod-1",
                name = "Expresso Valíria",
                description = "Forte",
                price = 10.0,
                imageUrl = "",
                category = "bebidas",
                isFavorite = false,
                quantityInCart = 0,
            )

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(productSuggestions = listOf(testProduct)),
                    onIntent = { intent -> selectedSuggestion = intent },
                )
            }
        }

        composeTestRule.onNodeWithText("Expresso Valíria").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expresso Valíria").performClick()

        assertTrue(selectedSuggestion is ChatIntent.SelectProductSuggestion)
        assertEquals(
            testProduct.id,
            (selectedSuggestion as ChatIntent.SelectProductSuggestion).product.id,
        )
    }

    @Test
    fun chatScreen_clickSend_triggersSendMessageIntent() {
        var sentIntent: ChatIntent? = null
        val sendDescription = context.getString(R.string.chat_send_desc)

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(inputText = "Quero um bolo de limão"),
                    onIntent = { intent -> sentIntent = intent },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(sendDescription).performClick()

        assertTrue(sentIntent is ChatIntent.SendMessage)
        assertEquals(
            "Quero um bolo de limão",
            (sentIntent as ChatIntent.SendMessage).text,
        )
    }

    @Test
    fun chatScreen_rendersVisualSnapshot_withExpectedDimensionsAndObsidianTheme() {
        val messages =
            listOf(
                ChatMessage(id = "1", text = "Dracarys!", sender = ChatSender.USER),
                ChatMessage(id = "2", text = "O fogo real ilumina seu pedido.", sender = ChatSender.AI),
            )

        composeTestRule.setContent {
            TargaryenTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = messages),
                    onIntent = {},
                )
            }
        }

        val pixelMap =
            composeTestRule
                .onRoot()
                .captureToImage()
                .toPixelMap()

        assertTrue(pixelMap.width > 0)
        assertTrue(pixelMap.height > 0)
    }
}
