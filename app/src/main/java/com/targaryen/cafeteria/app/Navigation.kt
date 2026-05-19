package com.targaryen.cafeteria.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.targaryen.cafeteria.app.ui.main.MainScreen
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature.auth.presentation.login.LoginScreen
import com.targaryen.cafeteria.feature.auth.presentation.register.RegisterScreen
import com.targaryen.cafeteria.feature.auth.presentation.splash.SplashScreen

private const val ANIM_LONG = 700
private const val ANIM_SHORT = 500

private val registerAnim = metadata {
    put(NavDisplay.TransitionKey) {
        slideInVertically(
            initialOffsetY = { it }, animationSpec = tween(ANIM_LONG)
        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith slideOutVertically(
            targetOffsetY = { it }, animationSpec = tween(ANIM_LONG)
        )
    }
}

private val mainAnim = metadata {
    put(NavDisplay.TransitionKey) {
        slideInHorizontally(
            initialOffsetX = { it }, animationSpec = tween(ANIM_LONG)
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { -it }, animationSpec = tween(ANIM_LONG)
        )
    }
}

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(SplashDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            fadeIn(animationSpec = tween(ANIM_SHORT)) togetherWith fadeOut(animationSpec = tween(ANIM_SHORT))
        },
        popTransitionSpec = {
            fadeIn(animationSpec = tween(ANIM_SHORT)) togetherWith fadeOut(animationSpec = tween(ANIM_SHORT))
        },
        entryProvider = entryProvider {
            entry<SplashDestination> {
                SplashScreen(
                    onNavigateToMain = { backStack.clear(); backStack.add(MainDestination) },
                    onNavigateToLogin = { backStack.clear(); backStack.add(LoginDestination) }
                )
            }
            entry<LoginDestination> {
                LoginScreen(
                    onLoginClick = { backStack.clear(); backStack.add(MainDestination) },
                    onRegisterClick = { backStack.add(RegisterDestination) },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<RegisterDestination>(metadata = registerAnim) {
                RegisterScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onRegisterSuccess = { backStack.clear(); backStack.add(MainDestination) },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<MainDestination>(metadata = mainAnim) {
                MainScreen(modifier = Modifier.safeDrawingPadding().padding(TargaryenTheme.dimens.spaceNormal))
            }
        }
    )
}
