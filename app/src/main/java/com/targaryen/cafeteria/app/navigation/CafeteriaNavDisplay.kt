package com.targaryen.cafeteria.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.targaryen.cafeteria.feature.auth.presentation.login.LoginScreen
import com.targaryen.cafeteria.feature.auth.presentation.register.RegisterScreen
import com.targaryen.cafeteria.feature.auth.presentation.splash.SplashScreen
import com.targaryen.cafeteria.feature_catalog.presentation.view.CatalogScreen

@Composable
fun CafeteriaNavDisplay() {
    val backStack = remember { mutableStateListOf<Any>(SplashRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { route ->
            when (route) {
                is SplashRoute -> NavEntry(route) {
                    SplashScreen(
                        onNavigateToMain = {
                            backStack.clear()
                            backStack.add(CatalogRoute)
                        },
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(LoginRoute)
                        }
                    )
                }

                is LoginRoute -> NavEntry(route) {
                    LoginScreen(
                        onLoginClick = {
                            backStack.clear()
                            backStack.add(CatalogRoute)
                        },
                        onRegisterClick = {
                            backStack.add(RegisterRoute)
                        }
                    )
                }

                is RegisterRoute -> NavEntry(route) {
                    RegisterScreen(
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        },
                        onRegisterSuccess = {
                            backStack.clear()
                            backStack.add(CatalogRoute)
                        }
                    )
                }

                is CatalogRoute -> NavEntry(route) {
                    CatalogScreen(
                        onTabSelected = { /* TODO Sprint 6 - Tabs */ },
                        onMenuClick = { /* TODO Sprint 6 - Drawer */ },
                        onLogoutClick = {
                            backStack.clear()
                            backStack.add(LoginRoute)
                        }
                    )
                }

                else -> {
                    error("Rota desconhecida: $route")
                }
            }
        }
    )
}
