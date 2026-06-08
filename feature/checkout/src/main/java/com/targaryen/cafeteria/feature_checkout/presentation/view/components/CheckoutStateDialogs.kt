package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState

@Composable
fun CheckoutStateDialogs(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.showCepModal) {
        CepSearchDialog(
            onDismiss = { onIntent(CheckoutIntent.OnDismissCepModal) },
            onSearch = { uf, city, street ->
                onIntent(CheckoutIntent.OnSearchReverseCep(uf, city, street))
            },
        )
    }

    state.errorResId?.let { errorId ->
        CheckoutErrorFancyDialog(
            title = stringResource(R.string.dialog_checkout_error_title),
            message = stringResource(errorId),
            isCancelable = true,
            onRetryClick = {
                onIntent(CheckoutIntent.OnDismissError)
                when (errorId) {
                    R.string.error_checkout_cep_failed -> {
                        if (state.cep.length == 8) {
                            onIntent(CheckoutIntent.OnCepChanged(state.cep))
                        }
                    }

                    R.string.error_checkout_payment_failed -> {
                        onIntent(CheckoutIntent.OnSubmitPayment)
                    }
                }
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissError)
            },
        )
    }

    if (state.isRedirecting) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = DragonScale,
            title = { Text(stringResource(R.string.dialog_redirect_title), color = ValyrianGold) },
            text = {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceNormal),
                ) {
                    Text(stringResource(R.string.dialog_redirect_message), color = Color.White)
                    CircularProgressIndicator(color = BloodRed)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onIntent(CheckoutIntent.OnCancelCheckout) }) {
                    Text(stringResource(R.string.dialog_redirect_button_cancel), color = Color.Gray)
                }
            },
        )
    }

    if (state.showCancelNotice) {
        CheckoutErrorFancyDialog(
            title = stringResource(R.string.dialog_checkout_error_title),
            message = stringResource(R.string.dialog_checkout_error_message),
            isCancelable = true,
            onRetryClick = {
                onIntent(CheckoutIntent.OnDismissCancelNotice)
                onIntent(CheckoutIntent.OnSubmitPayment)
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissCancelNotice)
            },
        )
    }

    if (state.showSuccessNotice) {
        CheckoutSuccessFancyDialog(
            title = stringResource(R.string.dialog_success_title),
            message = stringResource(R.string.dialog_success_message),
            isCancelable = false,
            onConfirmClick = {
                onIntent(CheckoutIntent.OnDismissSuccessNotice)
                onPaymentSuccess()
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissSuccessNotice)
                onPaymentSuccess()
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutStateDialogsPreview() {
    TargaryenTheme {
        CheckoutStateDialogs(
            state = CheckoutState(isRedirecting = true),
            onIntent = {},
            onPaymentSuccess = {},
        )
    }
}
