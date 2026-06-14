package com.targaryen.cafeteria.feature.auth.presentation.register

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
import com.targaryen.cafeteria.feature.auth.presentation.register.state.RegisterEvent
import com.targaryen.cafeteria.feature.auth.presentation.register.state.RegisterUiState
import com.targaryen.cafeteria.feature.auth.presentation.register.view.RegisterScreen
import com.targaryen.cafeteria.feature.auth.presentation.register.viewmodel.RegisterViewModel
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
class RegisterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<RegisterViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow(RegisterUiState())
    private val eventFlow = MutableSharedFlow<RegisterEvent>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        every { mockViewModel.uiState } returns uiStateFlow
        every { mockViewModel.events } returns eventFlow

        startKoin {
            modules(
                module {
                    single(named("WebClientId")) { "dummy_client_id" }
                },
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun setContent(
        onNavigateBack: () -> Unit = {},
        onRegisterSuccess: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TargaryenTheme {
                RegisterScreen(
                    onNavigateBack = onNavigateBack,
                    onRegisterSuccess = onRegisterSuccess,
                    viewModel = mockViewModel,
                )
            }
        }
    }

    @Test
    fun registerScreen_initialState_shouldDisplayAllElements() {
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.title_register)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_name)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_email)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.label_confirm_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_do_register)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_register_google)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_already_have_account)).assertIsDisplayed()
    }

    @Test
    fun registerScreen_whenTypingName_shouldCallViewModel() {
        setContent()

        val name = "Aegon Targaryen"
        composeTestRule.onNodeWithText(context.getString(R.string.label_name)).performTextInput(name)

        verify { mockViewModel.onNameChanged(name) }
    }

    @Test
    fun registerScreen_whenTypingEmail_shouldCallViewModel() {
        setContent()

        val email = "aegon@dragonstone.com"
        composeTestRule.onNodeWithText(context.getString(R.string.label_email)).performTextInput(email)

        verify { mockViewModel.onEmailChanged(email) }
    }

    @Test
    fun registerScreen_whenTypingPassword_shouldCallViewModel() {
        setContent()

        val password = "password123"
        composeTestRule.onNodeWithText(context.getString(R.string.label_password)).performTextInput(password)

        verify { mockViewModel.onPasswordChanged(password) }
    }

    @Test
    fun registerScreen_whenTypingConfirmPassword_shouldCallViewModel() {
        setContent()

        val confirmPassword = "password123"
        composeTestRule
            .onNodeWithText(
                context.getString(R.string.label_confirm_password),
            ).performTextInput(confirmPassword)

        verify { mockViewModel.onConfirmPasswordChanged(confirmPassword) }
    }

    @Test
    fun registerScreen_registerButton_shouldBeEnabledOnlyWhenFormIsValid() {
        // Initial state: invalid
        setContent()
        composeTestRule.onNodeWithText(context.getString(R.string.action_do_register)).assertIsNotEnabled()

        // Valid state
        uiStateFlow.value =
            RegisterUiState(
                name = "Aegon",
                email = "valid@test.com",
                password = "password123",
                confirmPassword = "password123",
            )
        // Re-composition happens
        composeTestRule.onNodeWithText(context.getString(R.string.action_do_register)).assertIsEnabled()
    }

    @Test
    fun registerScreen_whenRegisterClicked_shouldCallViewModel() {
        uiStateFlow.value =
            RegisterUiState(
                name = "Aegon",
                email = "valid@test.com",
                password = "password123",
                confirmPassword = "password123",
            )
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.action_do_register)).performClick()

        verify { mockViewModel.onRegisterClick() }
    }

    @Test
    fun registerScreen_whenAlreadyHaveAccountClicked_shouldInvokeCallback() {
        var backClicked = false
        setContent(onNavigateBack = { backClicked = true })

        composeTestRule.onNodeWithText(context.getString(R.string.action_already_have_account)).performClick()

        assert(backClicked)
    }
}
