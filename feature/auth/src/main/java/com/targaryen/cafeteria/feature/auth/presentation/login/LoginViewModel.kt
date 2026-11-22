package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_network.telemetry.AppTelemetry
import com.targaryen.cafeteria.core_network.util.Resource
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

@KoinViewModel
class LoginViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val telemetry: AppTelemetry,
) : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onEmailChanged(email: String) {
        uiState.update { state -> state.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        uiState.update { state -> state.copy(password = password) }
    }

    fun onForgotPasswordClick() {
        uiState.update { state ->
            state.copy(
                isForgotPasswordSheetOpen = true,
                forgotPasswordEmail = state.email,
                isForgotPasswordSuccess = false,
            )
        }
    }

    fun onForgotPasswordDismiss() {
        uiState.update { state ->
            state.copy(
                isForgotPasswordSheetOpen = false,
                forgotPasswordEmail = "",
                isForgotPasswordSuccess = false,
            )
        }
    }

    fun onForgotPasswordEmailChanged(email: String) {
        uiState.update { state -> state.copy(forgotPasswordEmail = email) }
    }

    fun onSendPasswordResetClick() {
        val email = uiState.value.forgotPasswordEmail
        if (email.isBlank() || !uiState.value.canSendPasswordReset) return

        viewModelScope.launch {
            uiState.update { state -> state.copy(isForgotPasswordLoading = true, isForgotPasswordSuccess = false) }

            sendPasswordResetEmailUseCase(email).collect { resource ->
                uiState.update { state -> state.copy(isForgotPasswordLoading = false) }
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("password_reset_request", mapOf("success" to true))
                        uiState.update { state -> state.copy(isForgotPasswordSuccess = true) }
                        eventChannel.send(LoginEvent.ResetPasswordEmailSent)
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("password_reset_request", mapOf("success" to false))
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
            uiState.update { state -> state.copy(isEmailLoading = true) }

            signInWithEmailUseCase(currentState.email, currentState.password).collect { resource ->
                uiState.update { state -> state.copy(isEmailLoading = false) }
                when (resource) {
                    is Resource.Success -> {
                        telemetry.setCustomKey("is_authenticated", true)
                        telemetry.trackEvent("login", mapOf("method" to "email", "success" to true))
                        eventChannel.send(LoginEvent.LoginSuccess)
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("login", mapOf("method" to "email", "success" to false))
                        if (resource.error is AuthError.NetworkError ||
                            resource.error is AuthError.SecurityVerificationFailed ||
                            resource.error is AuthError.Unknown
                        ) {
                            telemetry.trackNonFatalError(
                                "auth_email_login",
                                IllegalStateException(resource.error.toString()),
                            )
                        }
                        eventChannel.send(LoginEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            uiState.update { state -> state.copy(isGoogleLoading = true) }

            signInWithGoogleUseCase(idToken).collect { resource ->
                uiState.update { state -> state.copy(isGoogleLoading = false) }
                when (resource) {
                    is Resource.Success -> {
                        telemetry.setCustomKey("is_authenticated", true)
                        telemetry.trackEvent("login", mapOf("method" to "google", "success" to true))
                        eventChannel.send(LoginEvent.LoginSuccess)
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("login", mapOf("method" to "google", "success" to false))
                        telemetry.trackNonFatalError(
                            "auth_google_login",
                            IllegalStateException(resource.error.toString()),
                        )
                        eventChannel.send(LoginEvent.ShowErrorDialog(resource.error))
                    }
                }
            }
        }
    }

    fun onGoogleSignInError(message: String?) {
        telemetry.trackEvent("login", mapOf("method" to "google", "success" to false))
        telemetry.trackNonFatalError(
            "auth_google_signin_ui",
            IllegalStateException(message ?: "google_signin_failed"),
        )
        viewModelScope.launch {
            eventChannel.send(LoginEvent.ShowErrorDialog(AuthError.Unknown(message)))
        }
    }
}
