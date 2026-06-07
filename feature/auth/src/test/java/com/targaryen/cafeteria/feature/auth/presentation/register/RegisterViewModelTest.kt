package com.targaryen.cafeteria.feature.auth.presentation.register

import app.cash.turbine.test
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignUpWithEmailUseCase
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

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signUpWithEmailUseCase: SignUpWithEmailUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        viewModel =
            RegisterViewModel(
                signUpWithEmailUseCase = signUpWithEmailUseCase,
                signInWithGoogleUseCase = signInWithGoogleUseCase,
            )
    }

    @Test
    fun `when name is changed, uiState should reflect the new value and validity`() =
        runTest {
            viewModel.onNameChanged("Rhaenyra")
            assertEquals("Rhaenyra", viewModel.uiState.value.name)
            assertFalse(viewModel.uiState.value.nameError)

            viewModel.onNameChanged("  ")
            assertTrue(viewModel.uiState.value.nameError)
        }

    @Test
    fun `when email is changed, uiState should reflect the new value and validity`() =
        runTest {
            val validEmail = "queen@dragonstone.com"
            viewModel.onEmailChanged(validEmail)
            assertEquals(validEmail, viewModel.uiState.value.email)
            assertFalse(viewModel.uiState.value.emailError)

            viewModel.onEmailChanged("invalid-email")
            assertTrue(viewModel.uiState.value.emailError)
        }

    @Test
    fun `when password is changed, uiState should reflect the new value and validity`() =
        runTest {
            val validPassword = "password123"
            viewModel.onPasswordChanged(validPassword)
            assertEquals(validPassword, viewModel.uiState.value.password)
            assertFalse(viewModel.uiState.value.passwordError)

            viewModel.onPasswordChanged("123")
            assertTrue(viewModel.uiState.value.passwordError)
        }

    @Test
    fun `when confirm password is changed, uiState should reflect the new value and validity`() =
        runTest {
            viewModel.onPasswordChanged("password123")

            viewModel.onConfirmPasswordChanged("password123")
            assertEquals("password123", viewModel.uiState.value.confirmPassword)
            assertFalse(viewModel.uiState.value.confirmPasswordError)

            viewModel.onConfirmPasswordChanged("mismatch")
            assertTrue(viewModel.uiState.value.confirmPasswordError)
        }

    @Test
    fun `when all fields are valid, canRegister should be true`() =
        runTest {
            viewModel.onNameChanged("Aegon")
            viewModel.onEmailChanged("aegon@targaryen.com")
            viewModel.onPasswordChanged("dracarys")
            viewModel.onConfirmPasswordChanged("dracarys")

            assertTrue(viewModel.uiState.value.canRegister)
        }

    @Test
    fun `when email registration is successful, it should emit success event`() =
        runTest {
            val name = "Daemon"
            val email = "daemon@targaryen.com"
            val password = "caraxes_rider"

            every { signUpWithEmailUseCase(name, email, password) } returns flowOf(Resource.Success(Unit))

            viewModel.onNameChanged(name)
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)

            viewModel.events.test {
                viewModel.onRegisterClick()
                assertEquals(RegisterEvent.RegisterSuccess, awaitItem())
            }
        }

    @Test
    fun `when email registration fails, it should emit error dialog event`() =
        runTest {
            val name = "Criston Cole"
            val email = "kingmaker@hightower.com"
            val password = "traitor_ser"
            val error = AuthError.EmailAlreadyInUse

            every { signUpWithEmailUseCase(name, email, password) } returns flowOf(Resource.Error(error))

            viewModel.onNameChanged(name)
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)

            viewModel.events.test {
                viewModel.onRegisterClick()
                val event = awaitItem()
                assertTrue(event is RegisterEvent.ShowErrorDialog)
                assertEquals(error, (event as RegisterEvent.ShowErrorDialog).error)
            }
        }

    @Test
    fun `when google login is successful, it should emit success event`() =
        runTest {
            val idToken = "google_token"
            every { signInWithGoogleUseCase(idToken) } returns flowOf(Resource.Success(Unit))

            viewModel.events.test {
                viewModel.onGoogleSignIn(idToken)
                assertEquals(RegisterEvent.RegisterSuccess, awaitItem())
            }
        }

    @Test
    fun `when google sign-in returns error, it should emit error event`() =
        runTest {
            val message = "Google Play Services error"

            viewModel.events.test {
                viewModel.onGoogleSignInError(message)
                val event = awaitItem()
                assertTrue(event is RegisterEvent.ShowErrorDialog)
                val error = (event as RegisterEvent.ShowErrorDialog).error
                assertTrue(error is AuthError.Unknown && error.message == message)
            }
        }
}
