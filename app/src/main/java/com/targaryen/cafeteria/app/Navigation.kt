package com.targaryen.cafeteria.app

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

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(LoginDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<LoginDestination> {
                    LoginScreen(
                        onLoginClick = { backStack.add(MainDestination) },
                        onRegisterClick = { /* O futuro julgará esta rota */ },
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
