package com.targaryen.cafeteria.feature.auth.presentation.splash.state

sealed interface SplashEvent {
    object NavigateToMain : SplashEvent

    object NavigateToLogin : SplashEvent
}
