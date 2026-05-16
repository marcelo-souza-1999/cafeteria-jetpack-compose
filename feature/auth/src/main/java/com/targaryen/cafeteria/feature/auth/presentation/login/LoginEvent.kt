package com.targaryen.cafeteria.feature.auth.presentation.login

import com.targaryen.cafeteria.feature.auth.domain.model.AuthError

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
    data object ResetPasswordEmailSent : LoginEvent
    data class ShowErrorDialog(val error: AuthError) : LoginEvent
}
