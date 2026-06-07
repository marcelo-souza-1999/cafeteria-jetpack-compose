package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.patrik.fancycomposedialogs.dialogs.ErrorFancyDialog
import com.patrik.fancycomposedialogs.enums.DialogActionType
import com.patrik.fancycomposedialogs.enums.DialogStyle
import com.patrik.fancycomposedialogs.properties.DialogButtonProperties
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.R

@Composable
fun ProfileErrorFancyDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    ErrorFancyDialog(
        title = stringResource(R.string.profile_dialog_error_title),
        showTitle = true,
        showMessage = true,
        message = message,
        isCancelable = true,
        dialogActionType = DialogActionType.INFORMATIVE,
        dialogProperties =
            DialogButtonProperties(
                neutralButtonText = R.string.profile_dialog_error_cancel,
                buttonColor = MaterialTheme.colorScheme.primary,
                buttonTextColor = MaterialTheme.colorScheme.onPrimary,
            ),
        dialogStyle = DialogStyle.UPPER_CUTTING,
        neutralButtonClick = onDismiss,
        dismissTouchOutside = onDismiss,
    )
}

@Preview(name = "Error Dialog", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProfileErrorFancyDialogPreview() {
    TargaryenTheme {
        ProfileErrorFancyDialog(message = "Falha ao gravar no pergaminho.", onDismiss = {})
    }
}
