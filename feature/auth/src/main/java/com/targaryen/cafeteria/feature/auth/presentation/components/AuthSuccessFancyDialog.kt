package com.targaryen.cafeteria.feature.auth.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.feature.auth.R
import com.patrik.fancycomposedialogs.dialogs.SuccessFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme

@Composable
fun AuthSuccessFancyDialog(
    title: String,
    message: String,
    buttonTextRes: Int = R.string.dialog_success_button_ok,
    isCancelable: Boolean = true,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    SuccessFancyDialog(
        title = title,
        showTitle = true,
        showMessage = true,
        message = message,
        isCancelable = isCancelable,
        dialogActionType = DialogActionType.INFORMATIVE,
        dialogProperties = DialogButtonProperties(
            neutralButtonText = buttonTextRes,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonTextColor = Color.White
        ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        neutralButtonClick = {
            onConfirmClick()
            onDismissRequest()
        },
        dismissTouchOutside = onDismissRequest
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthSuccessFancyDialogPreview() {
    TargaryenTheme {
        AuthSuccessFancyDialog(
            title = "Sucesso!",
            message = "Operação realizada com sucesso.",
            buttonTextRes = R.string.dialog_success_button_ok,
            isCancelable = true,
            onConfirmClick = {},
            onDismissRequest = {}
        )
    }
}

