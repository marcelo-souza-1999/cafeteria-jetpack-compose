package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    val isEmailValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val isPasswordValid: Boolean
        get() = password.length >= MIN_PASSWORD_LENGTH

    val canLogin: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading

    val emailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    val passwordError: Boolean
        get() = password.isNotEmpty() && !isPasswordValid

    fun onLoginClick(onSuccess: () -> Unit) {
        if (!canLogin) return

        viewModelScope.launch {
            isLoading = true
            // Simulando o tempo de uma conquista
            delay(SIMULATED_LOGIN_DELAY)
            isLoading = false
            onSuccess()
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val SIMULATED_LOGIN_DELAY = 2000L
    }
}
