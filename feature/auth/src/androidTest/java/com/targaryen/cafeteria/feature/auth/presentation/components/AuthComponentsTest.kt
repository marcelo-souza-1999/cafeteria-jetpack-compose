package com.targaryen.cafeteria.feature.auth.presentation.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.targaryen.cafeteria.core_designsystem.components.TargaryenButton
import com.targaryen.cafeteria.core_designsystem.components.TargaryenPasswordField
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTextField
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun targaryenButton_shouldDisplayCorrectTextAndBeEnabled() {
        val text = "Click Me"
        val onClick: () -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenButton(text = text, onClick = onClick)
            }
        }

        composeTestRule.onNodeWithText(text).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText(text).performClick()
        
        verify { onClick() }
    }

    @Test
    fun targaryenButton_whenDisabled_shouldNotBeEnabled() {
        val text = "Disabled"
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenButton(text = text, onClick = {}, enabled = false)
            }
        }

        composeTestRule.onNodeWithText(text).assertIsNotEnabled()
    }

    @Test
    fun targaryenButton_whenLoading_shouldNotBeEnabled() {
        val text = "Loading"
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenButton(text = text, onClick = {}, isLoading = true)
            }
        }

        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun targaryenTextField_shouldDisplayLabelAndAcceptInput() {
        val label = "Email"
        val onValueChange: (String) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenTextField(
                    value = "",
                    onValueChange = onValueChange,
                    label = label
                )
            }
        }

        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        composeTestRule.onNodeWithText(label).performTextInput("test@test.com")
        
        verify { onValueChange("test@test.com") }
    }

    @Test
    fun targaryenTextField_whenError_shouldDisplaySupportingText() {
        val label = "Email"
        val errorText = "Invalid Email"
        
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenTextField(
                    value = "wrong",
                    onValueChange = {},
                    label = label,
                    isError = true,
                    supportingText = { Text(text = errorText) }
                )
            }
        }

        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    @Test
    fun targaryenPasswordField_shouldDisplayLabelAndAcceptInput() {
        val label = "Password"
        val onValueChange: (String) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            TargaryenTheme {
                TargaryenPasswordField(
                    value = "",
                    onValueChange = onValueChange,
                    label = label
                )
            }
        }

        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        
        composeTestRule.onNodeWithText(label).performTextInput("newpass")
        verify { onValueChange("newpass") }

        composeTestRule.onNodeWithTag("password_visibility_toggle").performClick()
    }
}
