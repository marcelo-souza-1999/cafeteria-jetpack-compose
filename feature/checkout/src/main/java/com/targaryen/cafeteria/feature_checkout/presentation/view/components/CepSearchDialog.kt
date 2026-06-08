package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R

@Composable
fun CepSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var uf by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }

    var ufError by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }
    var streetError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DragonScale,
        title = {
            Text(stringResource(R.string.dialog_cep_search_title), color = ValyrianGold)
        },
        text = {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = uf,
                    onValueChange = {
                        uf = it
                        if (it.isNotBlank()) ufError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_uf)) },
                    isError = ufError,
                    supportingText =
                        if (ufError) {
                            {
                                Text(
                                    text = stringResource(R.string.error_required_field),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    maxLines = 1,
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Obsidian,
                            unfocusedContainerColor = Obsidian,
                        ),
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        if (it.isNotBlank()) cityError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_city)) },
                    isError = cityError,
                    supportingText =
                        if (cityError) {
                            {
                                Text(
                                    text = stringResource(R.string.error_required_field),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    maxLines = 1,
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Obsidian,
                            unfocusedContainerColor = Obsidian,
                        ),
                )
                OutlinedTextField(
                    value = street,
                    onValueChange = {
                        street = it
                        if (it.isNotBlank()) streetError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_street)) },
                    isError = streetError,
                    supportingText =
                        if (streetError) {
                            {
                                Text(
                                    text = stringResource(R.string.error_required_field),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    maxLines = 1,
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Obsidian,
                            unfocusedContainerColor = Obsidian,
                        ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val isUfEmpty = uf.isBlank()
                val isCityEmpty = city.isBlank()
                val isStreetEmpty = street.isBlank()

                ufError = isUfEmpty
                cityError = isCityEmpty
                streetError = isStreetEmpty

                if (!isUfEmpty && !isCityEmpty && !isStreetEmpty) {
                    onSearch(uf, city, street)
                }
            }) {
                Text(stringResource(R.string.dialog_cep_search_action_search), color = BloodRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cep_search_action_cancel), color = Color.Gray)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CepSearchDialogPreview() {
    TargaryenTheme {
        CepSearchDialog(
            onDismiss = {},
            onSearch = { _, _, _ -> },
        )
    }
}
