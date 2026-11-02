package com.targaryen.cafeteria.feature.auth.presentation.splash

import app.cash.turbine.test
import com.targaryen.cafeteria.feature.auth.domain.usecase.CheckAuthSessionUseCase
import com.targaryen.cafeteria.feature.auth.presentation.splash.state.SplashEvent
import com.targaryen.cafeteria.feature.auth.presentation.splash.viewmodel.SplashViewModel
import com.targaryen.cafeteria.feature.auth.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkAuthSessionUseCase: CheckAuthSessionUseCase = mockk()

    @Test
    fun init_whenUserIsLoggedIn_emitsNavigateToMainEventAfterDelay() =
        runTest {
            coEvery { checkAuthSessionUseCase() } returns true

            val viewModel = SplashViewModel(checkAuthSessionUseCase)

            viewModel.events.test {
                advanceTimeBy(1501) // SPLASH_DURATION_MS + 1
                assertEquals(SplashEvent.NavigateToMain, awaitItem())
            }
        }

    @Test
    fun init_whenUserIsNotLoggedIn_emitsNavigateToLoginEventAfterDelay() =
        runTest {
            coEvery { checkAuthSessionUseCase() } returns false

            val viewModel = SplashViewModel(checkAuthSessionUseCase)

            viewModel.events.test {
                advanceTimeBy(1501)
                assertEquals(SplashEvent.NavigateToLogin, awaitItem())
            }
        }
}
