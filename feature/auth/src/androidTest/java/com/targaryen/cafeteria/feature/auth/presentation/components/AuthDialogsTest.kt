package com.targaryen.cafeteria.feature.auth.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature.auth.R
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthDialogsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val onRetryClick: () -> Unit = mockk(relaxed = true)
    private val onConfirmClick: () -> Unit = mockk(relaxed = true)
    private val onDismissRequest: () -> Unit = mockk(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun errorDialog_shouldDisplayCorrectTitleAndMessage() {
        val title = "Error Title"
        val message = "Error Message"

        composeTestRule.setContent {
            TargaryenTheme {
                AuthErrorFancyDialog(
                    title = title,
                    message = message,
                    onRetryClick = onRetryClick,
                    onDismissRequest = onDismissRequest,
                )
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun errorDialog_whenRetryClicked_shouldInvokeCallback() {
        composeTestRule.setContent {
            TargaryenTheme {
                AuthErrorFancyDialog(
                    title = "Error",
                    message = "Message",
                    onRetryClick = onRetryClick,
                    onDismissRequest = onDismissRequest,
                )
            }
        }

        val retryText = context.getString(R.string.dialog_error_button_retry)
        composeTestRule.onNodeWithText(retryText, ignoreCase = true).performClick()

        verify { onRetryClick() }
    }

    @Test
    fun successDialog_shouldDisplayCorrectTitleAndMessage() {
        val title = "Success Title"
        val message = "Success Message"

        composeTestRule.setContent {
            TargaryenTheme {
                AuthSuccessFancyDialog(
                    title = title,
                    message = message,
                    onConfirmClick = onConfirmClick,
                    onDismissRequest = onDismissRequest,
                )
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun successDialog_whenConfirmClicked_shouldInvokeCallbacks() {
        composeTestRule.setContent {
            TargaryenTheme {
                AuthSuccessFancyDialog(
                    title = "Success",
                    message = "Message",
                    onConfirmClick = onConfirmClick,
                    onDismissRequest = onDismissRequest,
                )
            }
        }

        val okText = context.getString(R.string.dialog_success_button_ok)
        composeTestRule.onNodeWithText(okText, ignoreCase = true).performClick()

        verify { onConfirmClick() }
        verify { onDismissRequest() }
    }
}
