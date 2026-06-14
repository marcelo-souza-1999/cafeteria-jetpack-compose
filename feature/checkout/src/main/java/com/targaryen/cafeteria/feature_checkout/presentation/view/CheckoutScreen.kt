package com.targaryen.cafeteria.feature_checkout.presentation.view

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutAddressSection
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutDeliverySection
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutStateDialogs
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutSubmitButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onIntent(CheckoutIntent.OnResetState)
    }

    LaunchedEffect(state.preferenceId, state.sandboxInitPoint, state.initPoint) {
        val targetUrl = state.sandboxInitPoint ?: state.initPoint
        if (state.preferenceId != null && targetUrl != null) {
            launchMercadoPagoCheckout(context, targetUrl)
            onIntent(CheckoutIntent.OnPaymentInitiated)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnIntent by rememberUpdatedState(onIntent)
    val currentIsRedirecting by rememberUpdatedState(state.isRedirecting)

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (currentIsRedirecting) {
                        currentOnIntent(CheckoutIntent.OnCancelCheckout)
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_title), color = ValyrianGold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = ValyrianGold,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Obsidian,
                    ),
            )
        },
        containerColor = Obsidian,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(TargaryenTheme.dimens.spaceNormal)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceNormal),
        ) {
            CheckoutAddressSection(state, onIntent)
            CheckoutDeliverySection(state, onIntent)
            CheckoutSubmitButton(state, onIntent)
        }
    }

    CheckoutStateDialogs(state, onIntent, onPaymentSuccess)
}

private fun launchMercadoPagoCheckout(
    context: Context,
    initPoint: String,
) {
    val uri = initPoint.toUri()
    val customTabsIntent =
        CustomTabsIntent
            .Builder()
            .setShowTitle(true)
            .build()

    try {
        customTabsIntent.launchUrl(context, uri)
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutScreenPreview() {
    TargaryenTheme {
        CheckoutScreen(
            state =
                CheckoutState(
                    cep = "01001-000",
                    street = "Praça da Sé",
                    city = "São Paulo",
                    state = "SP",
                    number = "100",
                    referencePoint = "Catedral da Sé",
                    recipientName = "Aegon Targaryen",
                ),
            onIntent = {},
            onNavigateBack = {},
            onPaymentSuccess = {},
        )
    }
}
