package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.patrik.fancycomposedialogs.dialogs.SuccessFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R

@Composable
fun CheckoutSuccessFancyDialog(
    title: String,
    message: String,
    isCancelable: Boolean = false,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SuccessFancyDialog(
        title = title,
        showTitle = true,
        showMessage = true,
        message = message,
        isCancelable = isCancelable,
        dialogActionType = DialogActionType.INFORMATIVE,
        dialogProperties =
            DialogButtonProperties(
                neutralButtonText = R.string.dialog_success_button_ok,
                buttonColor = ValyrianGold,
                buttonTextColor = Color.Black,
            ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        neutralButtonClick = {
            onConfirmClick()
            onDismissRequest()
        },
        dismissTouchOutside = onDismissRequest,
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckoutSuccessFancyDialogPreview() {
    TargaryenTheme {
        CheckoutSuccessFancyDialog(
            title = "Tributo Aceito",
            message = "O trono de ferro reconhece seu pagamento. Que o fogo e o sangue guiem seus passos.",
            onConfirmClick = {},
            onDismissRequest = {},
        )
    }
}
