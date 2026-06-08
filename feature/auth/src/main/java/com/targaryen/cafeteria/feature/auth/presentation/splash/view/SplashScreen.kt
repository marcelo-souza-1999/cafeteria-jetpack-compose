package com.targaryen.cafeteria.feature.auth.presentation.splash.view

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature.auth.R
import com.targaryen.cafeteria.feature.auth.presentation.splash.state.SplashEvent
import com.targaryen.cafeteria.feature.auth.presentation.splash.viewmodel.SplashViewModel
import org.koin.androidx.compose.koinViewModel
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SplashEvent.NavigateToMain -> onNavigateToMain()
                is SplashEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SplashContent(modifier = modifier)
}

@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = DesignSystemR.drawable.ic_logo_login_screen),
                contentDescription = null,
                modifier = Modifier.size(TargaryenTheme.dimens.logoSplash),
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
            Text(
                text = stringResource(id = R.string.splash_motto),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SplashScreenPreview() {
    TargaryenTheme {
        SplashContent()
    }
}
