package com.targaryen.cafeteria.feature.auth.presentation.login

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.presentation.components.AuthErrorFancyDialog
import com.targaryen.cafeteria.feature.auth.presentation.components.AuthSuccessFancyDialog
import com.targaryen.cafeteria.feature.auth.presentation.util.GoogleAuthUiClient
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

private const val QUALIFIER_WEB_CLIENT_ID = "WebClientId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (authError, setAuthError) = remember { mutableStateOf<AuthError?>(null) }
    val (showSuccessDialog, setShowSuccessDialog) = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId: String = koinInject(named(QUALIFIER_WEB_CLIENT_ID))
    val googleAuthUiClient = remember { GoogleAuthUiClient(context, webClientId) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginClick()
                is LoginEvent.ResetPasswordEmailSent -> {
                    setShowSuccessDialog(true)
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        viewModel.onForgotPasswordDismiss()
                    }
                }
                is LoginEvent.ShowErrorDialog -> setAuthError(event.error)
            }
        }
    }

    LoginDialogs(authError, showSuccessDialog, { setAuthError(null) }, { setShowSuccessDialog(false) })

    if (uiState.isForgotPasswordSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onForgotPasswordDismiss,
            sheetState = bottomSheetState,
            containerColor = Obsidian,
            contentColor = SilverHair,
        ) {
            ForgotPasswordContent(uiState, viewModel::onForgotPasswordEmailChanged, viewModel::onSendPasswordResetClick)
        }
    }

    val actions =
        remember {
            LoginActions(
                onEmailChange = viewModel::onEmailChanged,
                onPasswordChange = viewModel::onPasswordChanged,
                onForgotPasswordClick = viewModel::onForgotPasswordClick,
                onLoginClick = viewModel::onLoginClick,
                onRegisterClick = onRegisterClick,
                onGoogleSignInClick = {
                    coroutineScope.launch {
                        try {
                            val idToken = googleAuthUiClient.signIn()
                            if (idToken != null) viewModel.onGoogleSignIn(idToken)
                        } catch (e: GetCredentialException) {
                            viewModel.onGoogleSignInError(e.message)
                        }
                    }
                },
            )
        }

    Box(modifier = modifier.fillMaxSize()) {
        LoginContent(uiState, actions, Modifier.matchParentSize())
        if (uiState.isGoogleLoading) LoginLoadingOverlay()
    }
}

@Composable
private fun LoginDialogs(
    authError: AuthError?,
    showSuccessDialog: Boolean,
    onAuthErrorDismiss: () -> Unit,
    onSuccessDialogDismiss: () -> Unit,
) {
    if (showSuccessDialog) {
        AuthSuccessFancyDialog(
            title = stringResource(id = R.string.dialog_success_title),
            message = stringResource(id = R.string.msg_reset_email_sent),
            isCancelable = false,
            onConfirmClick = onSuccessDialogDismiss,
            onDismissRequest = onSuccessDialogDismiss,
        )
    }

    if (authError != null) {
        AuthErrorFancyDialog(
            title = stringResource(id = R.string.dialog_error_title),
            message = getErrorMessage(authError),
            isCancelable = false,
            onRetryClick = onAuthErrorDismiss,
            onDismissRequest = onAuthErrorDismiss,
        )
    }
}

@Composable
private fun getErrorMessage(error: AuthError): String =
    when (error) {
        is AuthError.InvalidCredentials -> stringResource(id = R.string.error_auth_invalid_credentials)
        is AuthError.UserNotFound -> stringResource(id = R.string.error_auth_user_not_found)
        is AuthError.EmailAlreadyInUse -> stringResource(id = R.string.error_auth_email_already_in_use)
        is AuthError.NetworkError -> stringResource(id = R.string.error_auth_network)
        is AuthError.TooManyRequests -> stringResource(id = R.string.error_auth_too_many_requests)
        is AuthError.Unknown -> stringResource(id = R.string.error_auth_unknown, error.message ?: "")
    }

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun ForgotPasswordContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(TargaryenTheme.dimens.spaceLarge)
                .padding(bottom = TargaryenTheme.dimens.spaceExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.title_forgot_password),
            color = DimmedGold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceNormal),
        )
        Text(
            text = stringResource(id = R.string.msg_forgot_password),
            color = SilverHair,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceLarge),
        )
        TargaryenTextField(
            value = uiState.forgotPasswordEmail,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.label_email),
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = ValyrianGold)
            },
            isError = uiState.forgotPasswordEmailError,
            supportingText =
                if (uiState.forgotPasswordEmailError) {
                    { Text(text = stringResource(id = R.string.error_invalid_email)) }
                } else {
                    null
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
        TargaryenButton(
            text = stringResource(id = R.string.action_send_reset_email),
            onClick = onSendClick,
            enabled = uiState.canSendPasswordReset,
            isLoading = uiState.isForgotPasswordLoading,
        )
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    actions: LoginActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Obsidian)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoginHeader()
        LoginForm(uiState, actions)
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraLarge))
        LoginFooter(uiState, actions)
    }
}

@Composable
private fun LoginHeader() {
    Image(
        painter = painterResource(id = DesignSystemR.drawable.ic_logo_login_screen),
        contentDescription = null,
        modifier =
            Modifier
                .size(TargaryenTheme.dimens.logoSplash)
                .padding(bottom = TargaryenTheme.dimens.spaceNormal),
    )
    Text(
        text = stringResource(id = R.string.title_login),
        color = DimmedGold,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceExtraLarge),
    )
}

@Composable
private fun ColumnScope.LoginForm(
    uiState: LoginUiState,
    actions: LoginActions,
) {
    TargaryenTextField(
        value = uiState.email,
        onValueChange = actions.onEmailChange,
        label = stringResource(id = R.string.label_email),
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = ValyrianGold)
        },
        isError = uiState.emailError,
        supportingText =
            if (uiState.emailError) {
                { Text(text = stringResource(id = R.string.error_invalid_email)) }
            } else {
                null
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
    TargaryenPasswordField(
        value = uiState.password,
        onValueChange = actions.onPasswordChange,
        label = stringResource(id = R.string.label_password),
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = ValyrianGold)
        },
        isError = uiState.passwordError,
        supportingText =
            if (uiState.passwordError) {
                { Text(text = stringResource(id = R.string.error_weak_password)) }
            } else {
                null
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
    Text(
        text = stringResource(id = R.string.action_forgot_password),
        color = SilverHair,
        style = MaterialTheme.typography.bodySmall,
        modifier =
            Modifier
                .align(Alignment.End)
                .clickable { actions.onForgotPasswordClick() },
    )
}

@Composable
private fun LoginFooter(
    uiState: LoginUiState,
    actions: LoginActions,
) {
    TargaryenButton(
        text = stringResource(id = R.string.action_login),
        onClick = actions.onLoginClick,
        enabled = uiState.canLogin && !uiState.isGoogleLoading,
        isLoading = uiState.isEmailLoading,
    )
    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
    TargaryenGoogleSignInButton(
        text = stringResource(id = R.string.action_login_google),
        onClick = actions.onGoogleSignInClick,
        enabled = !uiState.isEmailLoading,
    )
    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
    Text(
        text = stringResource(id = R.string.action_register),
        color = SilverHair,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.clickable { actions.onRegisterClick() },
    )
}

@Composable
private fun LoginLoadingOverlay() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Obsidian.copy(alpha = TargaryenTheme.dimens.alphaOverlay))
                .zIndex(TargaryenTheme.dimens.zIndexOverlay),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ValyrianGold)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreviewLight() {
    TargaryenTheme {
        val actions = LoginActions({}, {}, {}, {}, {}, {})
        LoginContent(LoginUiState(), actions)
    }
}
