package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature.auth.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<LoginViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow(LoginUiState())
    private val eventFlow = MutableSharedFlow<LoginEvent>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        every { mockViewModel.uiState } returns uiStateFlow
        every { mockViewModel.events } returns eventFlow

        startKoin {
            modules(module {
                single(named("WebClientId")) { "dummy_client_id" }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun setContent(
        onLoginClick: () -> Unit = {},
        onRegisterClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TargaryenTheme {
                LoginScreen(
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick,
                    viewModel = mockViewModel
                )
            }
        }
    }

    @Test
    fun loginScreen_initialState_shouldDisplayAllElements() {
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.title_login)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_email)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_login)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_login_google)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_register)).assertIsDisplayed()
    }

    @Test
    fun loginScreen_whenTypingEmail_shouldCallViewModel() {
        setContent()

        val email = "queen@targaryen.com"
        composeTestRule.onNodeWithText(context.getString(R.string.label_email)).performTextInput(email)

        verify { mockViewModel.onEmailChanged(email) }
    }

    @Test
    fun loginScreen_whenTypingPassword_shouldCallViewModel() {
        setContent()

        val password = "password123"
        composeTestRule.onNodeWithText(context.getString(R.string.label_password)).performTextInput(password)

        verify { mockViewModel.onPasswordChanged(password) }
    }

    @Test
    fun loginScreen_loginButton_shouldBeEnabledOnlyWhenFormIsValid() {
        // Initial state: invalid
        setContent()
        composeTestRule.onNodeWithText(context.getString(R.string.action_login)).assertIsNotEnabled()

        // Valid state
        uiStateFlow.value = LoginUiState(
            email = "valid@test.com",
            password = "password123"
        )
        // Re-composition happens
        composeTestRule.onNodeWithText(context.getString(R.string.action_login)).assertIsEnabled()
    }

    @Test
    fun loginScreen_whenLoginClicked_shouldCallViewModel() {
        uiStateFlow.value = LoginUiState(
            email = "valid@test.com",
            password = "password123"
        )
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.action_login)).performClick()

        verify { mockViewModel.onLoginClick() }
    }

    @Test
    fun loginScreen_whenRegisterClicked_shouldInvokeCallback() {
        var registerClicked = false
        setContent(onRegisterClick = { registerClicked = true })

        composeTestRule.onNodeWithText(context.getString(R.string.action_register)).performClick()

        assert(registerClicked)
    }

    @Test
    fun loginScreen_whenForgotPasswordClicked_shouldCallViewModel() {
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.action_forgot_password)).performClick()

        verify { mockViewModel.onForgotPasswordClick() }
    }

    @Test
    fun loginScreen_whenForgotPasswordSheetIsOpen_shouldDisplayContent() {
        uiStateFlow.value = LoginUiState(isForgotPasswordSheetOpen = true, forgotPasswordEmail = "test@test.com")
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.title_forgot_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_send_reset_email)).assertIsDisplayed()
    }
}
