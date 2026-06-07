package com.targaryen.cafeteria.feature.auth.presentation.register

import com.targaryen.cafeteria.feature.auth.domain.model.AuthError

sealed interface RegisterEvent {
    data object RegisterSuccess : RegisterEvent

    data class ShowErrorDialog(
        val error: AuthError,
    ) : RegisterEvent
}
