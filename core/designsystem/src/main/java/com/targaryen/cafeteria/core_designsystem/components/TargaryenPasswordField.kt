package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite

@Composable
fun TargaryenPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = leadingIcon,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            val icon = if (passwordVisible) {
                Icons.Outlined.Visibility
            } else {
                Icons.Outlined.VisibilityOff
            }
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DimmedGold
                )
            }
        },
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
private fun TargaryenPasswordFieldPreviewLight() {
    TargaryenTheme(darkTheme = false) {
        TargaryenPasswordField(
            value = "senha123",
            onValueChange = {},
            label = "Senha"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TargaryenPasswordFieldPreviewDark() {
    TargaryenTheme(darkTheme = true) {
        TargaryenPasswordField(
            value = "senha123",
            onValueChange = {},
            label = "Senha"
        )
    }
}