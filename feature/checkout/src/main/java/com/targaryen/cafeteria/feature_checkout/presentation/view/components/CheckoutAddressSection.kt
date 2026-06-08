package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState

@Composable
fun ColumnScope.CheckoutAddressSection(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
) {
    val streetInteraction = remember { MutableInteractionSource() }
    val cityInteraction = remember { MutableInteractionSource() }
    val stateInteraction = remember { MutableInteractionSource() }

    val isStreetPressed by streetInteraction.collectIsPressedAsState()
    val isCityPressed by cityInteraction.collectIsPressedAsState()
    val isStatePressed by stateInteraction.collectIsPressedAsState()

    LaunchedEffect(isStreetPressed) {
        if (isStreetPressed && state.street.isBlank()) {
            onIntent(CheckoutIntent.OnDisabledFieldClick)
        }
    }

    LaunchedEffect(isCityPressed) {
        if (isCityPressed && state.city.isBlank()) {
            onIntent(CheckoutIntent.OnDisabledFieldClick)
        }
    }

    LaunchedEffect(isStatePressed) {
        if (isStatePressed && state.state.isBlank()) {
            onIntent(CheckoutIntent.OnDisabledFieldClick)
        }
    }

    Text(
        text = stringResource(R.string.checkout_address_section),
        style =
            MaterialTheme.typography.titleLarge.copy(
                color = ValyrianGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
    )

    OutlinedTextField(
        value = state.cep,
        onValueChange = { onIntent(CheckoutIntent.OnCepChanged(it)) },
        label = { Text(stringResource(R.string.checkout_label_cep), color = Color.Gray) },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        singleLine = true,
        maxLines = 1,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = BloodRed,
                unfocusedIndicatorColor = Color.Gray,
            ),
        modifier = Modifier.fillMaxWidth(),
    )

    TextButton(
        onClick = { onIntent(CheckoutIntent.OnSearchAddressClicked) },
        modifier = Modifier.align(Alignment.End),
    ) {
        Text(stringResource(R.string.checkout_action_no_cep), color = ValyrianGold)
    }

    if (state.isLoadingAddress) {
        LinearProgressIndicator(
            color = BloodRed,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    OutlinedTextField(
        value = state.street,
        onValueChange = { },
        readOnly = true,
        interactionSource = streetInteraction,
        label = {
            Text(
                stringResource(R.string.checkout_label_street),
                color = Color.Gray,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        isError = state.showAddressFieldsError,
        supportingText =
            if (state.showAddressFieldsError) {
                {
                    Text(
                        stringResource(R.string.checkout_message_disabled_fields),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                null
            },
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                disabledTextColor = Color.Gray,
            ),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceNormal)) {
        OutlinedTextField(
            value = state.city,
            onValueChange = { },
            readOnly = true,
            interactionSource = cityInteraction,
            label = {
                Text(
                    stringResource(R.string.checkout_label_city),
                    color = Color.Gray,
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            maxLines = 1,
            isError = state.showAddressFieldsError,
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                ),
        )

        OutlinedTextField(
            value = state.state,
            onValueChange = { },
            readOnly = true,
            interactionSource = stateInteraction,
            label = {
                Text(
                    stringResource(R.string.checkout_label_state),
                    color = Color.Gray,
                )
            },
            modifier = Modifier.width(TargaryenTheme.dimens.stateFieldWidth),
            singleLine = true,
            maxLines = 1,
            isError = state.showAddressFieldsError,
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutAddressSectionPreview() {
    TargaryenTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Obsidian)
                    .padding(TargaryenTheme.dimens.spaceNormal),
        ) {
            CheckoutAddressSection(
                state =
                    CheckoutState(
                        cep = "01001-000",
                        street = "Praça da Sé",
                        city = "São Paulo",
                        state = "SP",
                    ),
                onIntent = {},
            )
        }
    }
}
