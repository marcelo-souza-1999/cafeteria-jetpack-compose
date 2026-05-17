package com.targaryen.cafeteria.feature.auth.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.feature.auth.R
import com.patrik.fancycomposedialogs.dialogs.ErrorFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme

@Composable
fun AuthErrorFancyDialog(
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
            positiveButtonText = R.string.dialog_error_button_retry,
            negativeButtonText = R.string.dialog_error_button_cancel,
            buttonColor = MaterialTheme.colorScheme.primary, 
            buttonTextColor = MaterialTheme.colorScheme.onPrimary 
        ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        positiveButtonClick = onRetryClick,
        negativeButtonClick = onDismissRequest,
        dismissTouchOutside = onDismissRequest
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthErrorFancyDialogPreview() {
    TargaryenTheme {
        AuthErrorFancyDialog(
            title = "Acesso Negado",
            message = "Credenciais inválidas. Verifique e-mail e senha.",
            isCancelable = false,
            onRetryClick = {},
            onDismissRequest = {}
        )
    }
}


