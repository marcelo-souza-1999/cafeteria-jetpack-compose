package com.targaryen.cafeteria.feature.auth.presentation.login

import com.targaryen.cafeteria.feature.auth.domain.model.AuthError

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
    data class ShowErrorToast(val error: AuthError) : LoginEvent
}
