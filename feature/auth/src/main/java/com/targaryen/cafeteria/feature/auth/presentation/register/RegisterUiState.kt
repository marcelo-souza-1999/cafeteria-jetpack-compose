package com.targaryen.cafeteria.feature.auth.presentation.register

data class RegisterUiState(
    val name: String = "",
    val nameError: Boolean = false,
    val email: String = "",
    val emailError: Boolean = false,
    val password: String = "",
    val passwordError: Boolean = false,
    val confirmPassword: String = "",
    val confirmPasswordError: Boolean = false,
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false
) {
    val canRegister: Boolean
        get() = name.isNotBlank() && !nameError &&
                email.isNotBlank() && !emailError &&
                password.isNotBlank() && !passwordError &&
                confirmPassword.isNotBlank() && !confirmPasswordError &&
                !isEmailLoading && !isGoogleLoading
}
