package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.usecase.SignInWithEmailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(kotlin.ExperimentalStdlibApi::class)
@KoinViewModel
class LoginViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase
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

    fun onLoginClick() {
        val currentState = uiState.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) return

        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true) }
            
            signInWithEmailUseCase(currentState.email, currentState.password).collect { resource ->
                uiState.update { it.copy(isLoading = false) }
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
}
