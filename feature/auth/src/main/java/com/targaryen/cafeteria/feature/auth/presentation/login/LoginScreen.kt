package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.targaryen.cafeteria.core_designsystem.components.TargaryenButton
import com.targaryen.cafeteria.core_designsystem.components.TargaryenGoogleSignInButton
import com.targaryen.cafeteria.core_designsystem.components.TargaryenPasswordField
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTextField
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature.auth.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel()
) {
    LoginContent(
        email = viewModel.email,
        password = viewModel.password,
        isLoading = viewModel.isLoading,
        emailError = viewModel.emailError,
        passwordError = viewModel.passwordError,
        canLogin = viewModel.canLogin,
        onEmailChange = { viewModel.email = it },
        onPasswordChange = { viewModel.password = it },
        onLoginClick = { viewModel.onLoginClick(onLoginClick) },
        onRegisterClick = onRegisterClick,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    isLoading: Boolean,
    emailError: Boolean,
    passwordError: Boolean,
    canLogin: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_login_screen),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = TargaryenTheme.dimens.spaceNormal)
        )

        Text(
            text = stringResource(id = R.string.title_login),
            color = DimmedGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceExtraLarge)
        )

        TargaryenTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.label_email),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = ValyrianGold
                )
            },
            isError = emailError,
            supportingText = if (emailError) {
                { Text(text = stringResource(id = R.string.error_invalid_email)) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

        TargaryenPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(id = R.string.label_password),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = ValyrianGold
                )
            },
            isError = passwordError,
            supportingText = if (passwordError) {
                { Text(text = stringResource(id = R.string.error_weak_password)) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

        Text(
            text = stringResource(id = R.string.action_forgot_password),
            color = SilverHair,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { /* O futuro julgará esta rota */ }
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraLarge))

        TargaryenButton(
            text = stringResource(id = R.string.action_login),
            onClick = onLoginClick,
            enabled = canLogin,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

        TargaryenGoogleSignInButton(
            text = stringResource(id = R.string.action_login_google),
            onClick = { /* O futuro julgará esta rota */ }
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

        Text(
            text = stringResource(id = R.string.action_register),
            color = SilverHair,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable { onRegisterClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreviewLight() {
    TargaryenTheme(darkTheme = false) {
        LoginContent(
            email = "",
            password = "",
            isLoading = false,
            emailError = false,
            passwordError = false,
            canLogin = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreviewDark() {
    TargaryenTheme(darkTheme = true) {
        LoginContent(
            email = "rhaenyra",
            password = "123",
            isLoading = false,
            emailError = true,
            passwordError = true,
            canLogin = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}
