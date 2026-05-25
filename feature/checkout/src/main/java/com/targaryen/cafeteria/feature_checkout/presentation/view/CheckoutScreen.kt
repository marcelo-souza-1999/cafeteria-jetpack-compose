package com.targaryen.cafeteria.feature_checkout.presentation.view

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutErrorFancyDialog
import com.targaryen.cafeteria.feature_checkout.presentation.view.components.CheckoutSuccessFancyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state.preferenceId, state.sandboxInitPoint) {
        if (state.preferenceId != null && state.sandboxInitPoint != null) {
            launchMercadoPagoCheckout(context, state.sandboxInitPoint)
            onIntent(CheckoutIntent.OnPaymentInitiated)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnIntent by rememberUpdatedState(onIntent)
    val currentIsRedirecting by rememberUpdatedState(state.isRedirecting)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (currentIsRedirecting) {
                    currentOnIntent(CheckoutIntent.OnCancelCheckout)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_title), color = ValyrianGold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Obsidian
                )
            )
        },
        containerColor = Obsidian
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.checkout_address_section),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = ValyrianGold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            OutlinedTextField(
                value = state.cep,
                onValueChange = { onIntent(CheckoutIntent.OnCepChanged(it)) },
                label = { Text(stringResource(R.string.checkout_label_cep), color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = BloodRed,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { onIntent(CheckoutIntent.OnSearchAddressClicked) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.checkout_action_no_cep), color = ValyrianGold)
            }

            if (state.isLoadingAddress) {
                LinearProgressIndicator(
                    color = BloodRed,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val streetInteraction = remember { MutableInteractionSource() }
            if (streetInteraction.collectIsPressedAsState().value) {
                onIntent(CheckoutIntent.OnDisabledFieldClick)
            }
            OutlinedTextField(
                value = state.street,
                onValueChange = { },
                readOnly = true,
                interactionSource = streetInteraction,
                label = {
                    Text(
                        stringResource(R.string.checkout_label_street),
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1,
                isError = state.showAddressFieldsError,
                supportingText = if (state.showAddressFieldsError) {
                    { Text(stringResource(R.string.checkout_message_disabled_fields), color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                    disabledTextColor = Color.Gray
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val cityInteraction = remember { MutableInteractionSource() }
                if (cityInteraction.collectIsPressedAsState().value) {
                    onIntent(CheckoutIntent.OnDisabledFieldClick)
                }
                OutlinedTextField(
                    value = state.city,
                    onValueChange = { },
                    readOnly = true,
                    interactionSource = cityInteraction,
                    label = {
                        Text(
                            stringResource(R.string.checkout_label_city),
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    maxLines = 1,
                    isError = state.showAddressFieldsError,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian
                    )
                )
                val stateInteraction = remember { MutableInteractionSource() }
                if (stateInteraction.collectIsPressedAsState().value) {
                    onIntent(CheckoutIntent.OnDisabledFieldClick)
                }
                OutlinedTextField(
                    value = state.state,
                    onValueChange = { },
                    readOnly = true,
                    interactionSource = stateInteraction,
                    label = {
                        Text(
                            stringResource(R.string.checkout_label_state),
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    maxLines = 1,
                    isError = state.showAddressFieldsError,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian
                    )
                )
            }

            OutlinedTextField(
                value = state.number,
                onValueChange = { onIntent(CheckoutIntent.OnNumberChanged(it)) },
                label = {
                    Text(
                        stringResource(R.string.checkout_label_number),
                        color = Color.Gray
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                    focusedIndicatorColor = BloodRed
                )
            )

            OutlinedTextField(
                value = state.referencePoint,
                onValueChange = { onIntent(CheckoutIntent.OnReferencePointChanged(it)) },
                label = {
                    Text(
                        stringResource(R.string.checkout_label_reference),
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ValyrianGold
                    )
                },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                    focusedIndicatorColor = BloodRed
                )
            )

            OutlinedTextField(
                value = state.recipientName,
                onValueChange = { onIntent(CheckoutIntent.OnRecipientNameChanged(it)) },
                label = {
                    Text(
                        stringResource(R.string.checkout_label_recipient),
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = ValyrianGold
                    )
                },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Obsidian,
                    unfocusedContainerColor = Obsidian,
                    focusedIndicatorColor = BloodRed
                )
            )

            Button(
                onClick = { onIntent(CheckoutIntent.OnSubmitPayment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BloodRed,
                    contentColor = Color.White
                ),
                enabled = !state.isCreatingPreference,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isCreatingPreference) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(R.string.checkout_action_submit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (state.showCepModal) {
        CepSearchDialog(
            onDismiss = { onIntent(CheckoutIntent.OnDismissCepModal) },
            onSearch = { uf, city, street ->
                onIntent(CheckoutIntent.OnSearchReverseCep(uf, city, street))
            }
        )
    }

    state.errorResId?.let { errorId ->
        CheckoutErrorFancyDialog(
            title = stringResource(R.string.dialog_checkout_error_title),
            message = stringResource(errorId),
            isCancelable = true,
            onRetryClick = {
                onIntent(CheckoutIntent.OnDismissError)
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissError)
            }
        )
    }

    if (state.isRedirecting) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = DragonScale,
            title = { Text(stringResource(R.string.dialog_redirect_title), color = ValyrianGold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.dialog_redirect_message), color = Color.White)
                    CircularProgressIndicator(color = BloodRed)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onIntent(CheckoutIntent.OnCancelCheckout) }) {
                    Text(stringResource(R.string.dialog_redirect_button_cancel), color = Color.Gray)
                }
            }
        )
    }

    if (state.showCancelNotice) {
        CheckoutErrorFancyDialog(
            title = stringResource(R.string.dialog_checkout_error_title),
            message = "Tributo cancelado pelo usuário.",
            isCancelable = true,
            onRetryClick = {
                onIntent(CheckoutIntent.OnDismissCancelNotice)
                onIntent(CheckoutIntent.OnSubmitPayment)
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissCancelNotice)
            }
        )
    }

    if (state.showSuccessNotice) {
        CheckoutSuccessFancyDialog(
            title = stringResource(R.string.dialog_success_title),
            message = stringResource(R.string.dialog_success_message),
            isCancelable = false,
            onConfirmClick = {
                onIntent(CheckoutIntent.OnDismissSuccessNotice)
                onPaymentSuccess()
            },
            onDismissRequest = {
                onIntent(CheckoutIntent.OnDismissSuccessNotice)
                onPaymentSuccess()
            }
        )
    }
}

@Composable
fun CepSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String, String, String) -> Unit
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uf,
                    onValueChange = { 
                        uf = it
                        if (it.isNotBlank()) ufError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_uf)) },
                    isError = ufError,
                    supportingText = if (ufError) {
                        { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian
                    )
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { 
                        city = it
                        if (it.isNotBlank()) cityError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_city)) },
                    isError = cityError,
                    supportingText = if (cityError) {
                        { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian
                    )
                )
                OutlinedTextField(
                    value = street,
                    onValueChange = { 
                        street = it
                        if (it.isNotBlank()) streetError = false
                    },
                    label = { Text(stringResource(R.string.dialog_cep_search_label_street)) },
                    isError = streetError,
                    supportingText = if (streetError) {
                        { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian
                    )
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
        }
    )
}

private fun launchMercadoPagoCheckout(context: Context, initPoint: String) {
    val uri = initPoint.toUri()
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()

    try {
        customTabsIntent.launchUrl(context, uri)
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutScreenPreview() {
    TargaryenTheme {
        CheckoutScreen(
            state = CheckoutState(
                cep = "01001-000",
                street = "Praça da Sé",
                city = "São Paulo",
                state = "SP",
                number = "100",
                referencePoint = "Catedral da Sé",
                recipientName = "Aegon Targaryen"
            ),
            onIntent = {},
            onNavigateBack = {},
            onPaymentSuccess = {}
        )
    }
}
