package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.patrik.fancycomposedialogs.dialogs.ErrorFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_checkout.R

@Composable
fun CheckoutErrorFancyDialog(
    title: String,
    message: String,
    isCancelable: Boolean = true,
    onRetryClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErrorFancyDialog(
        title = title,
        showTitle = true,
        showMessage = true,
        message = message,
        isCancelable = isCancelable,
        dialogActionType = DialogActionType.ACTIONABLE,
        dialogProperties = DialogButtonProperties(
            positiveButtonText = R.string.dialog_checkout_error_button_retry,
            negativeButtonText = R.string.dialog_checkout_error_button_cancel,
            buttonColor = BloodRed,
            buttonTextColor = Color.White
        ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        positiveButtonClick = onRetryClick,
        negativeButtonClick = onDismissRequest,
        dismissTouchOutside = onDismissRequest
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckoutErrorFancyDialogPreview() {
    TargaryenTheme {
        CheckoutErrorFancyDialog(
            title = "Tributo Negado",
            message = "Os corvos falharam ao voar na tempestade de rede.",
            onRetryClick = {},
            onDismissRequest = {}
        )
    }
}
