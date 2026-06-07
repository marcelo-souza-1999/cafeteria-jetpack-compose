package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

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
fun ProfileDeleteWarningFancyDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    WarningFancyDialog(
        title = stringResource(R.string.profile_dialog_delete_title),
        showTitle = true,
        showMessage = true,
        message = stringResource(R.string.profile_dialog_delete_msg),
        isCancelable = true,
        dialogActionType = DialogActionType.ACTIONABLE,
        dialogProperties =
            DialogButtonProperties(
                positiveButtonText = R.string.profile_dialog_error_retry,
                negativeButtonText = R.string.profile_dialog_error_cancel,
                buttonColor = MaterialTheme.colorScheme.error,
                buttonTextColor = MaterialTheme.colorScheme.onError,
            ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        positiveButtonClick = onConfirm,
        negativeButtonClick = onDismiss,
        dismissTouchOutside = onDismiss,
    )
}

@Preview(name = "Delete Warning Dialog", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProfileDeleteWarningFancyDialogPreview() {
    TargaryenTheme {
        ProfileDeleteWarningFancyDialog(onConfirm = {}, onDismiss = {})
    }
}
