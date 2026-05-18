package com.targaryen.cafeteria.app

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.targaryen.cafeteria.app.ui.main.MainScreen
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature.auth.presentation.login.LoginScreen
import com.targaryen.cafeteria.feature.auth.presentation.register.RegisterScreen
import com.targaryen.cafeteria.feature.auth.presentation.splash.SplashScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(SplashDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<SplashDestination> {
                    SplashScreen(
                        onNavigateToMain = {
                            backStack.clear()
                            backStack.add(MainDestination)
                        },
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(LoginDestination)
                        }
                    )
                }
                entry<LoginDestination> {
                    LoginScreen(
                        onLoginClick = {
                            backStack.clear()
                            backStack.add(MainDestination)
                        },
                        onRegisterClick = { 
                            backStack.add(RegisterDestination)
                        },
                        modifier = Modifier.safeDrawingPadding()
                    )
                }
                entry<RegisterDestination> {
                    RegisterScreen(
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        },
                        onRegisterSuccess = {
                            backStack.clear()
                            backStack.add(MainDestination)
                        },
                        modifier = Modifier.safeDrawingPadding()
                    )
                }
                entry<MainDestination> {
                    MainScreen(
                        modifier = Modifier
                          .safeDrawingPadding()
                          .padding(TargaryenTheme.dimens.spaceNormal)
                    )
                }
            },
    )
}

