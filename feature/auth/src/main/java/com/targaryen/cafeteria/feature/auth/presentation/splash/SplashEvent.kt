package com.targaryen.cafeteria.feature.auth.presentation.splash

sealed interface SplashEvent {
    object NavigateToMain : SplashEvent
    object NavigateToLogin : SplashEvent
}
