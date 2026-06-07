package com.targaryen.cafeteria.feature.auth.presentation.login

import app.cash.turbine.test
import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithEmailUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.targaryen.cafeteria.feature.auth.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signInWithEmailUseCase: SignInWithEmailUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase = mockk()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel =
            LoginViewModel(
                signInWithEmailUseCase = signInWithEmailUseCase,
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                sendPasswordResetEmailUseCase = sendPasswordResetEmailUseCase,
            )
    }

    @Test
    fun `when email is changed, uiState should reflect the new value`() =
        runTest {
            val email = "rhaenyra@targaryen.com"
            viewModel.onEmailChanged(email)
            assertEquals(email, viewModel.uiState.value.email)
        }

    @Test
    fun `when password is changed, uiState should reflect the new value`() =
        runTest {
            val password = "Dracarys123"
            viewModel.onPasswordChanged(password)
            assertEquals(password, viewModel.uiState.value.password)
        }

    @Test
    fun `when forgot password is clicked, it should open the sheet and fill the recovery email`() =
        runTest {
            val email = "viserys@targaryen.com"
            viewModel.onEmailChanged(email)

            viewModel.onForgotPasswordClick()

            val state = viewModel.uiState.value
            assertTrue(state.isForgotPasswordSheetOpen)
            assertEquals(email, state.forgotPasswordEmail)
            assertFalse(state.isForgotPasswordSuccess)
        }

    @Test
    fun `when forgot password sheet is dismissed, it should clear the recovery states`() =
        runTest {
            viewModel.onForgotPasswordClick()
            viewModel.onForgotPasswordDismiss()

            val state = viewModel.uiState.value
            assertFalse(state.isForgotPasswordSheetOpen)
            assertEquals("", state.forgotPasswordEmail)
            assertFalse(state.isForgotPasswordSuccess)
        }

    @Test
    fun `when recovery email is changed, uiState should be updated`() =
        runTest {
            val email = "aegon@targaryen.com"
            viewModel.onForgotPasswordEmailChanged(email)
            assertEquals(email, viewModel.uiState.value.forgotPasswordEmail)
        }

    @Test
    fun `when email login is successful, it should emit success event`() =
        runTest {
            val email = "queen@dragonstone.com"
            val password = "password"
            every { signInWithEmailUseCase(email, password) } returns flowOf(Resource.Success(Unit))

            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged(password)

            viewModel.events.test {
                viewModel.onLoginClick()
                assertEquals(LoginEvent.LoginSuccess, awaitItem())
            }
        }

    @Test
    fun `when email login fails, it should emit error dialog event`() =
        runTest {
            val email = "hightower@kinglanding.com"
            val password = "wrong"
            val error = AuthError.InvalidCredentials
            every { signInWithEmailUseCase(email, password) } returns flowOf(Resource.Error(error))

            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged(password)

            viewModel.events.test {
                viewModel.onLoginClick()
                val event = awaitItem()
                assertTrue(event is LoginEvent.ShowErrorDialog)
                assertEquals(error, (event as LoginEvent.ShowErrorDialog).error)
            }
        }

    @Test
    fun `when google login is successful, it should emit success event`() =
        runTest {
            val idToken = "google_token"
            every { signInWithGoogleUseCase(idToken) } returns flowOf(Resource.Success(Unit))

            viewModel.events.test {
                viewModel.onGoogleSignIn(idToken)
                assertEquals(LoginEvent.LoginSuccess, awaitItem())
            }
        }

    @Test
    fun `when google sign-in returns error, it should emit error event`() =
        runTest {
            val message = "Google Play Services error"

            viewModel.events.test {
                viewModel.onGoogleSignInError(message)
                val event = awaitItem()
                assertTrue(event is LoginEvent.ShowErrorDialog)
                val error = (event as LoginEvent.ShowErrorDialog).error
                assertTrue(error is AuthError.Unknown && error.message == message)
            }
        }

    @Test
    fun `when password recovery email is sent successfully, it should update state and emit event`() =
        runTest {
            val email = "alicent@targaryen.com"
            every { sendPasswordResetEmailUseCase(email) } returns flowOf(Resource.Success(Unit))

            viewModel.onForgotPasswordEmailChanged(email)

            viewModel.events.test {
                viewModel.onSendPasswordResetClick()
                assertEquals(LoginEvent.ResetPasswordEmailSent, awaitItem())
            }

            assertTrue(viewModel.uiState.value.isForgotPasswordSuccess)
        }

    @Test
    fun `when password recovery email fails, it should emit error event`() =
        runTest {
            val email = "otto@hightower.com"
            val error = AuthError.UserNotFound
            every { sendPasswordResetEmailUseCase(email) } returns flowOf(Resource.Error(error))

            viewModel.onForgotPasswordEmailChanged(email)

            viewModel.events.test {
                viewModel.onSendPasswordResetClick()
                val event = awaitItem()
                assertTrue(event is LoginEvent.ShowErrorDialog)
                assertEquals(error, (event as LoginEvent.ShowErrorDialog).error)
            }

            assertFalse(viewModel.uiState.value.isForgotPasswordSuccess)
        }
}
