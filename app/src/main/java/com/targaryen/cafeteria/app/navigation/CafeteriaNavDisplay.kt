package com.targaryen.cafeteria.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.targaryen.cafeteria.core_network.telemetry.AppTelemetry
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import com.targaryen.cafeteria.feature.auth.presentation.login.LoginScreen
import com.targaryen.cafeteria.feature.auth.presentation.register.view.RegisterScreen
import com.targaryen.cafeteria.feature.auth.presentation.splash.view.SplashScreen
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.CatalogScreen
import com.targaryen.cafeteria.feature_checkout.presentation.viewmodel.CheckoutViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CafeteriaNavDisplay() {
    val backStack = rememberNavBackStack(SplashRoute)
    val scope = rememberCoroutineScope()
    val authRepository = koinInject<AuthRepository>()
    val telemetry = koinInject<AppTelemetry>()
    val currentRoute = backStack.lastOrNull()

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            val routeName = route::class.simpleName ?: "UnknownRoute"
            telemetry.trackScreen(routeName)
            if (route is CheckoutRoute) {
                telemetry.trackEvent("begin_checkout")
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { route ->
            when (route) {
                is SplashRoute ->
                    NavEntry(route) {
                        SplashScreen(
                            onNavigateToMain = {
                                backStack.clear()
                                backStack.add(CatalogRoute)
                            },
                            onNavigateToLogin = {
                                backStack.clear()
                                backStack.add(LoginRoute)
                            },
                        )
                    }

                is LoginRoute ->
                    NavEntry(route) {
                        LoginScreen(
                            onLoginClick = {
                                backStack.clear()
                                backStack.add(CatalogRoute)
                            },
                            onRegisterClick = {
                                backStack.add(RegisterRoute)
                            },
                        )
                    }

                is RegisterRoute ->
                    NavEntry(route) {
                        RegisterScreen(
                            onNavigateBack = {
                                backStack.removeLastOrNull()
                            },
                            onRegisterSuccess = {
                                backStack.clear()
                                backStack.add(CatalogRoute)
                            },
                        )
                    }

                is CatalogRoute ->
                    NavEntry(route) {
                        CatalogScreen(
                            onTabSelected = {},
                            onMenuClick = {},
                            onLogoutClick = {
                                scope.launch {
                                    authRepository.logout()
                                    backStack.clear()
                                    backStack.add(LoginRoute)
                                }
                            },
                            onCheckoutClick = {
                                backStack.add(CheckoutRoute)
                            },
                        )
                    }

                is CheckoutRoute ->
                    NavEntry(route) {
                        val viewModel = koinViewModel<CheckoutViewModel>()
                        val state by viewModel.uiState.collectAsState()
                        com.targaryen.cafeteria.feature_checkout.presentation.view.CheckoutScreen(
                            state = state,
                            onIntent = viewModel::onIntent,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onPaymentSuccess = {
                                backStack.clear()
                                backStack.add(CatalogRoute)
                            },
                        )
                    }

                else -> {
                    error("Rota desconhecida: $route")
                }
            }
        },
    )
}
