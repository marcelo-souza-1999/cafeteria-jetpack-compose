package com.targaryen.cafeteria.feature.auth.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.feature.auth.domain.usecase.CheckAuthSessionUseCase
import com.targaryen.cafeteria.feature.auth.presentation.splash.state.SplashEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SplashViewModel(
    private val checkAuthSessionUseCase: CheckAuthSessionUseCase,
) : ViewModel() {
    private val eventChannel = Channel<SplashEvent>()
    val events: Flow<SplashEvent> = eventChannel.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)

            val isUserLoggedIn = checkAuthSessionUseCase()
            if (isUserLoggedIn) {
                eventChannel.send(SplashEvent.NavigateToMain)
            } else {
                eventChannel.send(SplashEvent.NavigateToLogin)
            }
        }
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1500L
    }
}
