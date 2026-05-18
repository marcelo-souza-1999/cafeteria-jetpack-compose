package com.targaryen.cafeteria.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignUpWithEmailUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalStdlibApi::class)
@KoinViewModel
class RegisterViewModel(
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    val uiState: StateFlow<RegisterUiState>
        field: MutableStateFlow<RegisterUiState> = MutableStateFlow(RegisterUiState())

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    fun onNameChanged(name: String) {
        uiState.update {
            it.copy(
                name = name,
                nameError = name.isBlank()
            )
        }
    }

    fun onEmailChanged(email: String) {
        uiState.update {
            it.copy(
                email = email,
                emailError = !EMAIL_REGEX.matches(email)
            )
        }
    }

    fun onPasswordChanged(password: String) {
        uiState.update {
            it.copy(
                password = password,
                passwordError = password.length < 6,
                confirmPasswordError = it.confirmPassword.isNotEmpty() && password != it.confirmPassword
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = confirmPassword != it.password
            )
        }
    }

    fun onRegisterClick() {
        val currentState = uiState.value
        if (!currentState.canRegister) return

        viewModelScope.launch {
            uiState.update { it.copy(isEmailLoading = true) }
            
            signUpWithEmailUseCase(
                name = currentState.name,
                email = currentState.email,
                pass = currentState.password
            ).collect { resource ->
                uiState.update { it.copy(isEmailLoading = false) }
                when (resource) {
                    is com.targaryen.cafeteria.core_network.Resource.Success -> {
                        eventChannel.send(RegisterEvent.RegisterSuccess)
                    }
                    is com.targaryen.cafeteria.core_network.Resource.Error -> {
                        eventChannel.send(RegisterEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            uiState.update { it.copy(isGoogleLoading = true) }
            
            signInWithGoogleUseCase(idToken).collect { resource ->
                uiState.update { it.copy(isGoogleLoading = false) }
                when (resource) {
                    is com.targaryen.cafeteria.core_network.Resource.Success -> {
                        eventChannel.send(RegisterEvent.RegisterSuccess)
                    }
                    is com.targaryen.cafeteria.core_network.Resource.Error -> {
                        eventChannel.send(RegisterEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onGoogleSignInError(message: String?) {
        viewModelScope.launch {
            eventChannel.send(RegisterEvent.ShowErrorDialog(com.targaryen.cafeteria.feature.auth.domain.model.AuthError.Unknown(message)))
        }
    }
}
