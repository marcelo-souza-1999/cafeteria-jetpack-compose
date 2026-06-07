package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTextField
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R

@Composable
fun ProfileInfoForm(
    name: String,
    email: String,
    onChangeName: (String) -> Unit,
    onChangeEmail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editNameValue by remember { mutableStateOf("") }

    var isEditingEmail by remember { mutableStateOf(false) }
    var editEmailValue by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        if (isEditingName) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TargaryenTextField(
                    value = editNameValue,
                    onValueChange = { editNameValue = it },
                    label = stringResource(R.string.profile_label_new_name),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    if (editNameValue.isNotBlank() && editNameValue != name) {
                        onChangeName(editNameValue)
                    }
                    isEditingName = false
                }) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.profile_desc_save),
                        tint = ValyrianGold,
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                )
                IconButton(onClick = {
                    editNameValue = name
                    isEditingName = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.profile_desc_edit_name),
                        tint = SilverHair,
                        modifier = Modifier.size(TargaryenTheme.dimens.iconSizeMedium),
                    )
                }
            }
        }

        if (isEditingEmail) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TargaryenTextField(
                    value = editEmailValue,
                    onValueChange = { editEmailValue = it },
                    label = stringResource(R.string.profile_label_new_email),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    if (editEmailValue.isNotBlank() && editEmailValue != email) {
                        onChangeEmail(editEmailValue)
                    }
                    isEditingEmail = false
                }) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.profile_desc_save),
                        tint = ValyrianGold,
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverHair,
                )
                IconButton(onClick = {
                    editEmailValue = email
                    isEditingEmail = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.profile_desc_edit_email),
                        tint = SilverHair,
                        modifier = Modifier.size(TargaryenTheme.dimens.iconSizeSmall),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
        HorizontalDivider(color = ValyrianGold.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
    }
}

@Preview(name = "Profile Info Form")
@Composable
fun ProfileInfoFormPreview() {
    TargaryenTheme {
        ProfileInfoForm(
            name = "Aegon Targaryen",
            email = "aegon@dragons.com",
            onChangeName = {},
            onChangeEmail = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
