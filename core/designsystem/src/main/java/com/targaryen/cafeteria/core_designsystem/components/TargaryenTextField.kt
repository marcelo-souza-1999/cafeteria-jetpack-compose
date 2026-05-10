package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun TargaryenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DragonScale,
            unfocusedContainerColor = DragonScale,
            errorContainerColor = DragonScale,
            focusedTextColor = TargaryenWhite,
            unfocusedTextColor = TargaryenWhite,
            errorTextColor = TargaryenWhite,
            focusedBorderColor = BloodRed,
            unfocusedBorderColor = SilverHair,
            errorBorderColor = BloodRed,
            focusedLabelColor = BloodRed,
            unfocusedLabelColor = SilverHair,
            errorLabelColor = BloodRed,
            cursorColor = BloodRed
        ),
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium)
    )
}

@Preview(showBackground = true)
@Composable
private fun TargaryenTextFieldPreviewLight() {
    TargaryenTheme(darkTheme = false) {
        TargaryenTextField(
            value = "",
            onValueChange = {},
            label = "E-mail"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TargaryenTextFieldPreviewDark() {
    TargaryenTheme(darkTheme = true) {
        TargaryenTextField(
            value = "",
            onValueChange = {},
            label = "E-mail"
        )
    }
}