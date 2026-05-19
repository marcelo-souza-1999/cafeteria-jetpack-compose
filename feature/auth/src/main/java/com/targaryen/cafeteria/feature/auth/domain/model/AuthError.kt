package com.targaryen.cafeteria.feature.auth.domain.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object UserNotFound : AuthError
    data object EmailAlreadyInUse : AuthError
    data object TooManyRequests : AuthError
    data object NetworkError : AuthError
    data class Unknown(val message: String?) : AuthError
}
