package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithEmailUseCase
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalStdlibApi::class)
@KoinViewModel
class LoginViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
) : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onEmailChanged(email: String) {
        uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        uiState.update { it.copy(password = password) }
    }

    fun onForgotPasswordClick() {
        uiState.update {
            it.copy(
                isForgotPasswordSheetOpen = true,
                forgotPasswordEmail = it.email,
                isForgotPasswordSuccess = false,
            )
        }
    }

    fun onForgotPasswordDismiss() {
        uiState.update {
            it.copy(
                isForgotPasswordSheetOpen = false,
                forgotPasswordEmail = "",
                isForgotPasswordSuccess = false,
            )
        }
    }

    fun onForgotPasswordEmailChanged(email: String) {
        uiState.update { it.copy(forgotPasswordEmail = email) }
    }

    fun onSendPasswordResetClick() {
        val email = uiState.value.forgotPasswordEmail
        if (email.isBlank() || !uiState.value.canSendPasswordReset) return

        viewModelScope.launch {
            uiState.update { it.copy(isForgotPasswordLoading = true, isForgotPasswordSuccess = false) }

            sendPasswordResetEmailUseCase(email).collect { resource ->
                uiState.update { it.copy(isForgotPasswordLoading = false) }
                when (resource) {
                    is Resource.Success -> {
                        uiState.update { it.copy(isForgotPasswordSuccess = true) }
                        eventChannel.send(LoginEvent.ResetPasswordEmailSent)
                    }
                    is Resource.Error -> {
                        eventChannel.send(LoginEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onLoginClick() {
        val currentState = uiState.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) return

        viewModelScope.launch {
            uiState.update { it.copy(isEmailLoading = true) }

            signInWithEmailUseCase(currentState.email, currentState.password).collect { resource ->
                uiState.update { it.copy(isEmailLoading = false) }
                when (resource) {
                    is Resource.Success -> {
                        eventChannel.send(LoginEvent.LoginSuccess)
                    }
                    is Resource.Error -> {
                        eventChannel.send(LoginEvent.ShowErrorDialog(resource.error))
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
                    is Resource.Success -> {
                        eventChannel.send(LoginEvent.LoginSuccess)
                    }
                    is Resource.Error -> {
                        eventChannel.send(LoginEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onGoogleSignInError(message: String?) {
        viewModelScope.launch {
            eventChannel.send(LoginEvent.ShowErrorDialog(AuthError.Unknown(message)))
        }
    }
}
