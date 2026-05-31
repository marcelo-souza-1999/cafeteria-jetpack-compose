package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.patrik.fancycomposedialogs.dialogs.WarningFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.R

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    WarningFancyDialog(
        title = stringResource(R.string.logout_confirm_title),
        showTitle = true,
        showMessage = true,
        message = stringResource(R.string.logout_confirm_msg),
        isCancelable = true,
        dialogActionType = DialogActionType.ACTIONABLE,
        dialogProperties = DialogButtonProperties(
            positiveButtonText = R.string.logout_btn_confirm,
            negativeButtonText = R.string.logout_btn_dismiss,
            buttonColor = MaterialTheme.colorScheme.error,
            buttonTextColor = MaterialTheme.colorScheme.onError
        ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        positiveButtonClick = onConfirm,
        negativeButtonClick = onDismiss,
        dismissTouchOutside = onDismiss
    )
}

@Preview(name = "Logout Confirmation Dialog", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LogoutConfirmationDialogPreview() {
    TargaryenTheme {
        LogoutConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
