package com.targaryen.cafeteria.feature.auth.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isForgotPasswordSheetOpen: Boolean = false,
    val forgotPasswordEmail: String = "",
    val isForgotPasswordLoading: Boolean = false,
    val isForgotPasswordSuccess: Boolean = false
) {
    private val isEmailFormatValid: Boolean
        get() = email.contains("@") && email.contains(".")

    private val isForgotPasswordEmailFormatValid: Boolean
        get() = forgotPasswordEmail.contains("@") && forgotPasswordEmail.contains(".")

    private val isPasswordFormatValid: Boolean
        get() = password.length >= 6

    val emailError: Boolean
        get() = email.isNotBlank() && !isEmailFormatValid

    val forgotPasswordEmailError: Boolean
        get() = forgotPasswordEmail.isNotBlank() && !isForgotPasswordEmailFormatValid

    val passwordError: Boolean
        get() = password.isNotBlank() && !isPasswordFormatValid

    val canLogin: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && 
                isEmailFormatValid && isPasswordFormatValid && 
                !isEmailLoading && !isGoogleLoading

    val canSendPasswordReset: Boolean
        get() = forgotPasswordEmail.isNotBlank() && 
                isForgotPasswordEmailFormatValid && !isForgotPasswordLoading
}
