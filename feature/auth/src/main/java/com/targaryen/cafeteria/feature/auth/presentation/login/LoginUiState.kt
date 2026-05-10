package com.targaryen.cafeteria.feature.auth.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
) {
    private val isEmailFormatValid: Boolean
        get() = email.contains("@") && email.contains(".")

    private val isPasswordFormatValid: Boolean
        get() = password.length >= 6

    val emailError: Boolean
        get() = email.isNotBlank() && !isEmailFormatValid

    val passwordError: Boolean
        get() = password.isNotBlank() && !isPasswordFormatValid

    val canLogin: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && isEmailFormatValid && isPasswordFormatValid && !isLoading
}
