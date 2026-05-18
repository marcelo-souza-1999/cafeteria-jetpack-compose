package com.targaryen.cafeteria.feature.auth.presentation.register

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.targaryen.cafeteria.core_designsystem.components.TargaryenButton
import com.targaryen.cafeteria.core_designsystem.components.TargaryenGoogleSignInButton
import com.targaryen.cafeteria.core_designsystem.components.TargaryenPasswordField
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTextField
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTopBar
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature.auth.R
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.presentation.components.AuthErrorFancyDialog
import com.targaryen.cafeteria.feature.auth.presentation.util.GoogleAuthUiClient
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private const val QUALIFIER_WEB_CLIENT_ID = "WebClientId"

class RegisterCallbacks(
    val onNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onRegisterClick: () -> Unit,
    val onGoogleSignInClick: () -> Unit,
    val onBackClick: () -> Unit
)

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (authError, setAuthError) = remember { mutableStateOf<AuthError?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId: String = koinInject(named(QUALIFIER_WEB_CLIENT_ID))
    val googleAuthUiClient = remember { GoogleAuthUiClient(context, webClientId) }

    val callbacks = remember {
        RegisterCallbacks(
            onNameChange = viewModel::onNameChanged,
            onEmailChange = viewModel::onEmailChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
            onRegisterClick = viewModel::onRegisterClick,
            onGoogleSignInClick = {
                coroutineScope.launch {
                    try {
                        val idToken = googleAuthUiClient.signIn()
                        if (idToken != null) {
                            viewModel.onGoogleSignIn(idToken)
                        }
                    } catch (e: GetCredentialException) {
                        viewModel.onGoogleSignInError(e.message)
                    }
                }
            },
            onBackClick = onNavigateBack
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.RegisterSuccess -> {
                    onRegisterSuccess()
                }

                is RegisterEvent.ShowErrorDialog -> {
                    setAuthError(event.error)
                }
            }
        }
    }

    authError?.let { error ->
        val errorMessage = getErrorMessage(error)
        AuthErrorFancyDialog(
            title = stringResource(id = R.string.dialog_error_title),
            message = errorMessage,
            isCancelable = false,
            onRetryClick = { setAuthError(null) },
            onDismissRequest = { setAuthError(null) }
        )
    }

    RegisterContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun getErrorMessage(error: AuthError): String {
    return when (error) {
        is AuthError.EmailAlreadyInUse -> stringResource(id = R.string.error_auth_email_already_in_use)
        is AuthError.InvalidCredentials -> stringResource(id = R.string.error_auth_invalid_credentials)
        is AuthError.UserNotFound -> stringResource(id = R.string.error_auth_user_not_found)
        is AuthError.NetworkError -> stringResource(id = R.string.error_auth_network)
        is AuthError.TooManyRequests -> stringResource(id = R.string.error_auth_too_many_requests)
        is AuthError.Unknown -> stringResource(
            id = R.string.error_auth_unknown,
            error.message ?: ""
        )
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RegisterContent(
    uiState: RegisterUiState,
    callbacks: RegisterCallbacks,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TargaryenTopBar(
                title = stringResource(id = R.string.title_register),
                onBackClick = callbacks.onBackClick
            )
        },
        containerColor = Obsidian,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = TargaryenTheme.dimens.spaceLarge,
                        vertical = TargaryenTheme.dimens.spaceNormal
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_login_screen),
                    contentDescription = null,
                    modifier = Modifier
                        .size(TargaryenTheme.dimens.logoAuth)
                        .padding(bottom = TargaryenTheme.dimens.spaceNormal)
                )

                Text(
                    text = stringResource(id = R.string.subtitle_register),
                    color = com.targaryen.cafeteria.core_designsystem.theme.DimmedGold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceExtraLarge)
                )
                TargaryenTextField(
                    value = uiState.name,
                    onValueChange = callbacks.onNameChange,
                    label = stringResource(id = R.string.label_name),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    isError = uiState.nameError,
                    supportingText = if (uiState.nameError) {
                        { Text(text = stringResource(id = R.string.error_name_empty)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                TargaryenTextField(
                    value = uiState.email,
                    onValueChange = callbacks.onEmailChange,
                    label = stringResource(id = R.string.label_email),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    isError = uiState.emailError,
                    supportingText = if (uiState.emailError) {
                        { Text(text = stringResource(id = R.string.error_invalid_email)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                TargaryenPasswordField(
                    value = uiState.password,
                    onValueChange = callbacks.onPasswordChange,
                    label = stringResource(id = R.string.label_password),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    isError = uiState.passwordError,
                    supportingText = if (uiState.passwordError) {
                        { Text(text = stringResource(id = R.string.error_weak_password)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                TargaryenPasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = callbacks.onConfirmPasswordChange,
                    label = stringResource(id = R.string.label_confirm_password),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    isError = uiState.confirmPasswordError,
                    supportingText = if (uiState.confirmPasswordError) {
                        { Text(text = stringResource(id = R.string.error_passwords_not_match)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraLarge))

                TargaryenButton(
                    text = stringResource(id = R.string.action_do_register),
                    onClick = callbacks.onRegisterClick,
                    enabled = uiState.canRegister,
                    isLoading = uiState.isEmailLoading
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                TargaryenGoogleSignInButton(
                    text = stringResource(id = R.string.action_register_google),
                    onClick = callbacks.onGoogleSignInClick,
                    enabled = !uiState.isEmailLoading
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

                Text(
                    text = stringResource(id = R.string.action_already_have_account),
                    color = com.targaryen.cafeteria.core_designsystem.theme.SilverHair,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { callbacks.onBackClick() }
                )
            }

            if (uiState.isGoogleLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Obsidian.copy(alpha = TargaryenTheme.dimens.alphaOverlay))
                        .zIndex(TargaryenTheme.dimens.zIndexOverlay),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ValyrianGold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreviewLight() {
    TargaryenTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            callbacks = RegisterCallbacks({}, {}, {}, {}, {}, {}, {})
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegisterScreenPreviewDark() {
    TargaryenTheme {
        RegisterContent(
            uiState = RegisterUiState(
                name = "Aegon Targaryen",
                email = "aegon@dragonstone.com",
                password = "fire",
                passwordError = true,
                confirmPassword = "fire"
            ),
            callbacks = RegisterCallbacks({}, {}, {}, {}, {}, {}, {})
        )
    }
}