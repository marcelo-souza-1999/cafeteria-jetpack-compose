package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import com.targaryen.cafeteria.coredatabase.address.AddressProvider
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

private const val CEP_LENGTH = 8

@KoinViewModel
class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val catalogRepository: CatalogRepository,
    private val paymentStatusTracker: PaymentStatusTracker,
    private val addressProvider: AddressProvider,
) : ViewModel() {
    val uiState: StateFlow<CheckoutState>
        field: MutableStateFlow<CheckoutState> = MutableStateFlow(CheckoutState())

    private var isPaymentCompleted = false
    private var fetchAddressJob: kotlinx.coroutines.Job? = null

    init {
        onIntent(CheckoutIntent.OnLoadCartItems)
        observePaymentStatus()
        resetState()
    }

    private fun observePaymentStatus() {
        viewModelScope.launch {
            paymentStatusTracker.paymentStatus.collectLatest { status ->
                when (status) {
                    PaymentStatus.SUCCESS, PaymentStatus.PENDING -> {
                        isPaymentCompleted = true
                        addressProvider.clearAddress()

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val userId = currentUser?.uid
                        val cartItems = uiState.value.cartItems
                        if (userId != null && cartItems.isNotEmpty()) {
                            val itemsSummary =
                                cartItems.joinToString { product ->
                                    "${product.quantityInCart}x ${product.name}"
                                }
                            val totalPrice =
                                cartItems.sumOf { product ->
                                    product.price * product.quantityInCart
                                }
                            val orderStatus = if (status == PaymentStatus.SUCCESS) "Aprovado" else "Pendente"

                            viewModelScope.launch {
                                checkoutRepository.saveOrder(
                                    userId = userId,
                                    itemsSummary = itemsSummary,
                                    totalPrice = totalPrice,
                                    status = orderStatus,
                                )
                            }
                        }

                        viewModelScope.launch {
                            catalogRepository.clearCart()
                        }

                        uiState.update { state ->
                            state.copy(isRedirecting = false, showSuccessNotice = true)
                        }
                    }
                    PaymentStatus.FAILURE -> {
                        uiState.update { state ->
                            state.copy(isRedirecting = false, errorResId = R.string.error_checkout_payment_failed)
                        }
                    }
                    PaymentStatus.CANCELLED -> {
                        uiState.update { state ->
                            state.copy(isRedirecting = false, showCancelNotice = true)
                        }
                    }
                }
            }
        }
    }

    fun onIntent(intent: CheckoutIntent) {
        if (handleSimpleIntent(intent)) return
        handleComplexIntent(intent)
    }

    private fun handleSimpleIntent(intent: CheckoutIntent): Boolean =
        when (intent) {
            is CheckoutIntent.OnNumberChanged -> {
                uiState.update { state -> state.copy(number = intent.number) }
                true
            }
            is CheckoutIntent.OnReferencePointChanged -> {
                uiState.update { state -> state.copy(referencePoint = intent.reference) }
                true
            }
            is CheckoutIntent.OnRecipientNameChanged -> {
                uiState.update { state -> state.copy(recipientName = intent.name) }
                true
            }
            is CheckoutIntent.OnSearchAddressClicked -> {
                uiState.update { state -> state.copy(showCepModal = true) }
                true
            }
            is CheckoutIntent.OnDismissCepModal -> {
                uiState.update { state -> state.copy(showCepModal = false) }
                true
            }
            is CheckoutIntent.OnDismissError -> {
                uiState.update { state -> state.copy(errorResId = null) }
                true
            }
            is CheckoutIntent.OnDismissCancelNotice -> {
                uiState.update { state -> state.copy(showCancelNotice = false) }
                true
            }
            is CheckoutIntent.OnDismissSuccessNotice -> {
                uiState.update { state -> state.copy(showSuccessNotice = false) }
                true
            }
            is CheckoutIntent.OnDisabledFieldClick -> {
                uiState.update { state -> state.copy(showAddressFieldsError = true) }
                true
            }
            else -> false
        }

    private fun handleComplexIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.OnLoadCartItems -> loadCartItems()
            is CheckoutIntent.OnResetState -> resetState()
            is CheckoutIntent.OnCepChanged -> handleCepChange(intent.cep)
            is CheckoutIntent.OnSubmitPayment -> submitPayment()
            is CheckoutIntent.OnSearchReverseCep -> searchReverseCep(intent.state, intent.city, intent.street)
            is CheckoutIntent.OnPaymentInitiated -> {
                uiState.update { state ->
                    state.copy(preferenceId = null, sandboxInitPoint = null, initPoint = null, isRedirecting = true)
                }
            }
            is CheckoutIntent.OnCancelCheckout -> {
                if (!isPaymentCompleted) {
                    uiState.update { state -> state.copy(isRedirecting = false, showCancelNotice = true) }
                } else {
                    uiState.update { state -> state.copy(isRedirecting = false) }
                }
            }
            else -> Unit
        }
    }

    private fun resetState() {
        val savedAddress = addressProvider.getAddress()
        if (savedAddress.cep.isNotBlank()) {
            val cleanCep = savedAddress.cep.filter { char -> char.isDigit() }
            uiState.update { state ->
                state.copy(
                    cep = cleanCep,
                    street = savedAddress.street,
                    city = savedAddress.city,
                    state = savedAddress.state,
                    number = savedAddress.number,
                    referencePoint = savedAddress.referencePoint,
                    recipientName = savedAddress.recipientName,
                )
            }
            if (cleanCep.length == CEP_LENGTH && savedAddress.street.isBlank()) {
                fetchAddress(cleanCep)
            }
            isPaymentCompleted = false
            return
        }
        uiState.update { state ->
            state.copy(
                cep = "",
                street = "",
                city = "",
                state = "",
                number = "",
                referencePoint = "",
                recipientName = "",
                isLoadingAddress = false,
                isCreatingPreference = false,
                errorResId = null,
                preferenceId = null,
                sandboxInitPoint = null,
                initPoint = null,
                showCepModal = false,
                isRedirecting = false,
                showCancelNotice = false,
                showSuccessNotice = false,
                showAddressFieldsError = false,
            )
        }
        isPaymentCompleted = false
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            catalogRepository
                .getProducts()
                .map { products -> products.filter { product -> product.quantityInCart > 0 } }
                .collectLatest { cartItems ->
                    uiState.update { state -> state.copy(cartItems = cartItems) }
                }
        }
    }

    private fun handleCepChange(newCep: String) {
        val digitsOnly = newCep.filter { char -> char.isDigit() }
        if (digitsOnly.length <= CEP_LENGTH) {
            uiState.update { state -> state.copy(cep = digitsOnly, showAddressFieldsError = false) }

            if (digitsOnly.length == CEP_LENGTH) {
                fetchAddress(digitsOnly)
            } else {
                uiState.update { state -> state.copy(street = "", city = "", state = "", errorResId = null) }
            }
        }
    }

    private fun fetchAddress(cep: String) {
        fetchAddressJob?.cancel()
        fetchAddressJob =
            viewModelScope.launch {
                uiState.update { state -> state.copy(isLoadingAddress = true, errorResId = null) }
                checkoutRepository
                    .fetchAddressByCep(cep)
                    .onSuccess { response ->
                        uiState.update { state ->
                            state.copy(
                                isLoadingAddress = false,
                                street = response.logradouro ?: "",
                                city = response.localidade ?: "",
                                state = response.uf ?: "",
                                showAddressFieldsError = false,
                            )
                        }
                    }.onFailure { exception ->
                        val errorId =
                            if (exception.message == "CEP não encontrado") {
                                R.string.error_checkout_cep_not_found
                            } else {
                                R.string.error_checkout_cep_failed
                            }
                        uiState.update { state ->
                            state.copy(
                                isLoadingAddress = false,
                                errorResId = errorId,
                            )
                        }
                    }
            }
    }

    private fun searchReverseCep(
        state: String,
        city: String,
        street: String,
    ) {
        fetchAddressJob?.cancel()
        fetchAddressJob =
            viewModelScope.launch {
                uiState.update { state ->
                    state.copy(isLoadingAddress = true, showCepModal = false, errorResId = null)
                }
                checkoutRepository
                    .searchCepByAddress(state, city, street)
                    .onSuccess { results ->
                        if (results.isEmpty()) {
                            uiState.update { state ->
                                state.copy(
                                    isLoadingAddress = false,
                                    errorResId = R.string.error_checkout_cep_not_found,
                                )
                            }
                        } else {
                            val firstMatch = results.first()
                            uiState.update { state ->
                                state.copy(
                                    isLoadingAddress = false,
                                    cep = firstMatch.cep?.filter { char -> char.isDigit() } ?: "",
                                    street = firstMatch.logradouro ?: "",
                                    city = firstMatch.localidade ?: "",
                                    state = firstMatch.uf ?: "",
                                    showAddressFieldsError = false,
                                )
                            }
                        }
                    }.onFailure {
                        uiState.update { state ->
                            state.copy(
                                isLoadingAddress = false,
                                errorResId = R.string.error_checkout_cep_failed,
                            )
                        }
                    }
            }
    }

    private fun submitPayment() {
        val currentState = uiState.value

        if (currentState.cep.length != CEP_LENGTH ||
            currentState.number.isBlank() ||
            currentState.referencePoint.isBlank()
        ) {
            uiState.update { state -> state.copy(errorResId = R.string.error_checkout_required_fields) }
            return
        }

        isPaymentCompleted = false

        viewModelScope.launch {
            uiState.update { state -> state.copy(isCreatingPreference = true, errorResId = null) }

            checkoutRepository
                .createPreferenceForCart(currentState.cartItems)
                .onSuccess { response ->
                    uiState.update { state ->
                        state.copy(
                            isCreatingPreference = false,
                            preferenceId = response.id,
                            sandboxInitPoint = response.sandboxInitPoint,
                            initPoint = response.initPoint,
                        )
                    }
                }.onFailure {
                    uiState.update { state ->
                        state.copy(
                            isCreatingPreference = false,
                            errorResId = R.string.error_checkout_payment_failed,
                        )
                    }
                }
        }
    }
}
