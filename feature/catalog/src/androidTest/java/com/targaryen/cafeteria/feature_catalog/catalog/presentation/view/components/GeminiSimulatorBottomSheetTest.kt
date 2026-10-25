package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.targaryen.cafeteria.feature_catalog.R as CatalogR

@RunWith(AndroidJUnit4::class)
class GeminiSimulatorBottomSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun voiceCommandSheetContent_rendersHeaderAndSuggestions() {
        val title = context.getString(CatalogR.string.voice_command_title)
        val subtitle = context.getString(CatalogR.string.voice_command_subtitle)

        composeTestRule.setContent {
            TargaryenTheme {
                VoiceCommandSheetContent(
                    state =
                        VoiceCommandUiState(
                            commandText = "",
                            isLoading = false,
                            executionResult = null,
                        ),
                    onCommandTextChange = {},
                    onShortcutClick = {},
                    onExecuteClick = {},
                    onDismissRequest = {},
                )
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun voiceCommandSheetContent_whenShortcutClicked_invokesCallback() {
        var clickedCommand = ""
        val firstShortcut = VOICE_SHORTCUTS.first()
        val shortcutLabel = context.getString(firstShortcut.labelResId)

        composeTestRule.setContent {
            TargaryenTheme {
                VoiceCommandSheetContent(
                    state =
                        VoiceCommandUiState(
                            commandText = "",
                            isLoading = false,
                            executionResult = null,
                        ),
                    onCommandTextChange = {},
                    onShortcutClick = { command -> clickedCommand = command },
                    onExecuteClick = {},
                    onDismissRequest = {},
                )
            }
        }

        composeTestRule.onNodeWithText(shortcutLabel).performClick()
        assertEquals(firstShortcut.command, clickedCommand)
    }

    @Test
    fun voiceCommandSheetContent_whenLoading_displaysProcessingState() {
        val processingText = context.getString(CatalogR.string.voice_command_status_processing)

        composeTestRule.setContent {
            TargaryenTheme {
                VoiceCommandSheetContent(
                    state =
                        VoiceCommandUiState(
                            commandText = "Adicionar café",
                            isLoading = true,
                            executionResult = null,
                        ),
                    onCommandTextChange = {},
                    onShortcutClick = {},
                    onExecuteClick = {},
                    onDismissRequest = {},
                )
            }
        }

        composeTestRule.onNodeWithText(processingText).assertIsDisplayed()
    }

    @Test
    fun voiceCommandSheetContent_whenExecutionResultPresent_displaysMessage() {
        val mockMessage = "Café Expresso Valíria adicionado ao carrinho com sucesso."
        val mockResult =
            VoiceExecutionResult(
                message = mockMessage,
                isError = false,
            )

        composeTestRule.setContent {
            TargaryenTheme {
                VoiceCommandSheetContent(
                    state =
                        VoiceCommandUiState(
                            commandText = "Quero um café",
                            isLoading = false,
                            executionResult = mockResult,
                        ),
                    onCommandTextChange = {},
                    onShortcutClick = {},
                    onExecuteClick = {},
                    onDismissRequest = {},
                )
            }
        }

        composeTestRule.onNodeWithText(mockMessage).assertIsDisplayed()
    }

    @Test
    fun voiceCommandSheetContent_whenDismissClicked_invokesOnDismiss() {
        var dismissed = false
        val closeDesc = context.getString(CatalogR.string.voice_command_close_desc)

        composeTestRule.setContent {
            TargaryenTheme {
                VoiceCommandSheetContent(
                    state =
                        VoiceCommandUiState(
                            commandText = "",
                            isLoading = false,
                            executionResult = null,
                        ),
                    onCommandTextChange = {},
                    onShortcutClick = {},
                    onExecuteClick = {},
                    onDismissRequest = { dismissed = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(closeDesc).performClick()
        assertTrue("Dismiss callback deve ser acionado", dismissed)
    }
}
