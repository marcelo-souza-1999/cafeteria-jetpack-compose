package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState

@Composable
fun CheckoutDeliverySection(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = state.number,
        onValueChange = { onIntent(CheckoutIntent.OnNumberChanged(it)) },
        label = {
            Text(
                stringResource(R.string.checkout_label_number),
                color = Color.Gray,
            )
        },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        singleLine = true,
        maxLines = 1,
        modifier = modifier.fillMaxWidth(),
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                focusedIndicatorColor = BloodRed,
            ),
    )

    OutlinedTextField(
        value = state.referencePoint,
        onValueChange = { onIntent(CheckoutIntent.OnReferencePointChanged(it)) },
        label = {
            Text(
                stringResource(R.string.checkout_label_reference),
                color = Color.Gray,
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = ValyrianGold,
            )
        },
        singleLine = true,
        maxLines = 1,
        modifier = modifier.fillMaxWidth(),
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                focusedIndicatorColor = BloodRed,
            ),
    )

    OutlinedTextField(
        value = state.recipientName,
        onValueChange = { onIntent(CheckoutIntent.OnRecipientNameChanged(it)) },
        label = {
            Text(
                stringResource(R.string.checkout_label_recipient),
                color = Color.Gray,
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = ValyrianGold,
            )
        },
        singleLine = true,
        maxLines = 1,
        modifier = modifier.fillMaxWidth(),
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                focusedIndicatorColor = BloodRed,
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckoutDeliverySectionPreview() {
    TargaryenTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Obsidian)
                    .padding(TargaryenTheme.dimens.spaceNormal),
        ) {
            CheckoutDeliverySection(
                state =
                    CheckoutState(
                        number = "100",
                        referencePoint = "Catedral da Sé",
                        recipientName = "Aegon Targaryen",
                    ),
                onIntent = {},
            )
        }
    }
}
