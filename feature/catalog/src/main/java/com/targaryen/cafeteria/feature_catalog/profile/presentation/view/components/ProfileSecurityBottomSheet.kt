package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.components.TargaryenPasswordField
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSecurityBottomSheet(
    onDismissRequest: () -> Unit,
    onChangePassword: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var currentPassError by remember { mutableStateOf<String?>(null) }
    var newPassError by remember { mutableStateOf<String?>(null) }

    val emptyPasswordError = stringResource(R.string.profile_error_password_empty)
    val weakPasswordError = stringResource(R.string.profile_error_password_weak)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CharcoalBlack,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceLarge),
        ) {
            Text(
                text = stringResource(R.string.profile_sheet_security_title),
                style = MaterialTheme.typography.titleLarge,
                color = ValyrianGold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
            Text(
                text = stringResource(R.string.profile_sheet_security_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = TargaryenWhite.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

            TargaryenPasswordField(
                value = currentPass,
                onValueChange = {
                    currentPass = it
                    currentPassError = null
                },
                label = stringResource(R.string.profile_label_current_password),
                isError = currentPassError != null,
                supportingText =
                    currentPassError?.let { error ->
                        { Text(text = error, color = BloodRed) }
                    },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = ValyrianGold)
                },
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            TargaryenPasswordField(
                value = newPass,
                onValueChange = {
                    newPass = it
                    newPassError = null
                },
                label = stringResource(R.string.profile_label_new_password),
                isError = newPassError != null,
                supportingText =
                    newPassError?.let { error ->
                        { Text(text = error, color = BloodRed) }
                    },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ValyrianGold)
                },
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

            Button(
                onClick = {
                    val isCurrentEmpty = currentPass.isBlank()
                    val isNewEmpty = newPass.isBlank()
                    val isNewWeak = !isNewEmpty && newPass.length < 6

                    currentPassError =
                        if (isCurrentEmpty) {
                            emptyPasswordError
                        } else {
                            null
                        }

                    newPassError =
                        when {
                            isNewEmpty -> emptyPasswordError
                            isNewWeak -> weakPasswordError
                            else -> null
                        }

                    if (currentPassError == null && newPassError == null) {
                        onChangePassword(currentPass, newPass)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            ) {
                Text(text = "Validar", color = TargaryenWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
        }
    }
}

@Preview(name = "Security Bottom Sheet")
@Composable
fun ProfileSecurityBottomSheetPreview() {
    TargaryenTheme {
        ProfileSecurityBottomSheet(
            onDismissRequest = {},
            onChangePassword = { _, _ -> },
        )
    }
}
