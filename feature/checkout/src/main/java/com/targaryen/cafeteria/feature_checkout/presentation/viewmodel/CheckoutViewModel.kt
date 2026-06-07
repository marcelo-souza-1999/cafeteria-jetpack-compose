package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.targaryen.cafeteria.core_network.PaymentStatus
import com.targaryen.cafeteria.core_network.PaymentStatusTracker
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutState())
    val uiState: StateFlow<CheckoutState> = _uiState.asStateFlow()

    private var isPaymentCompleted = false
    private var fetchAddressJob: kotlinx.coroutines.Job? = null

    init {
        onIntent(CheckoutIntent.OnLoadCartItems)
        observePaymentStatus()
    }

    private fun observePaymentStatus() {
        viewModelScope.launch {
            paymentStatusTracker.paymentStatus.collectLatest { status ->
                when (status) {
                    PaymentStatus.SUCCESS, PaymentStatus.PENDING -> {
                        isPaymentCompleted = true

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val userId = currentUser?.uid
                        val cartItems = _uiState.value.cartItems
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

                        _uiState.update { state ->
                            state.copy(isRedirecting = false, showSuccessNotice = true)
                        }
                    }
                    PaymentStatus.FAILURE -> {
                        _uiState.update { state ->
                            state.copy(isRedirecting = false, errorResId = R.string.error_checkout_payment_failed)
                        }
                    }
                    PaymentStatus.CANCELLED -> {
                        _uiState.update { state ->
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
                _uiState.update { state -> state.copy(number = intent.number) }
                true
            }
            is CheckoutIntent.OnReferencePointChanged -> {
                _uiState.update { state -> state.copy(referencePoint = intent.reference) }
                true
            }
            is CheckoutIntent.OnRecipientNameChanged -> {
                _uiState.update { state -> state.copy(recipientName = intent.name) }
                true
            }
            is CheckoutIntent.OnSearchAddressClicked -> {
                _uiState.update { state -> state.copy(showCepModal = true) }
                true
            }
            is CheckoutIntent.OnDismissCepModal -> {
                _uiState.update { state -> state.copy(showCepModal = false) }
                true
            }
            is CheckoutIntent.OnDismissError -> {
                _uiState.update { state -> state.copy(errorResId = null) }
                true
            }
            is CheckoutIntent.OnDismissCancelNotice -> {
                _uiState.update { state -> state.copy(showCancelNotice = false) }
                true
            }
            is CheckoutIntent.OnDismissSuccessNotice -> {
                _uiState.update { state -> state.copy(showSuccessNotice = false) }
                true
            }
            is CheckoutIntent.OnDisabledFieldClick -> {
                _uiState.update { state -> state.copy(showAddressFieldsError = true) }
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
                _uiState.update { state ->
                    state.copy(preferenceId = null, sandboxInitPoint = null, initPoint = null, isRedirecting = true)
                }
            }
            is CheckoutIntent.OnCancelCheckout -> {
                if (!isPaymentCompleted) {
                    _uiState.update { state -> state.copy(isRedirecting = false, showCancelNotice = true) }
                } else {
                    _uiState.update { state -> state.copy(isRedirecting = false) }
                }
            }
            else -> Unit
        }
    }

    private fun resetState() {
        _uiState.update { state ->
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
                .map { products -> products.filter { it.quantityInCart > 0 } }
                .collectLatest { cartItems ->
                    _uiState.update { it.copy(cartItems = cartItems) }
                }
        }
    }

    private fun handleCepChange(newCep: String) {
        val digitsOnly = newCep.filter { it.isDigit() }
        if (digitsOnly.length <= CEP_LENGTH) {
            _uiState.update { state -> state.copy(cep = digitsOnly, showAddressFieldsError = false) }

            if (digitsOnly.length == CEP_LENGTH) {
                fetchAddress(digitsOnly)
            } else {
                _uiState.update { state -> state.copy(street = "", city = "", state = "", errorResId = null) }
            }
        }
    }

    private fun fetchAddress(cep: String) {
        fetchAddressJob?.cancel()
        fetchAddressJob =
            viewModelScope.launch {
                _uiState.update { state -> state.copy(isLoadingAddress = true, errorResId = null) }
                checkoutRepository
                    .fetchAddressByCep(cep)
                    .onSuccess { response ->
                        _uiState.update { state ->
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
                        _uiState.update { state ->
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
                _uiState.update { state ->
                    state.copy(isLoadingAddress = true, showCepModal = false, errorResId = null)
                }
                checkoutRepository
                    .searchCepByAddress(state, city, street)
                    .onSuccess { results ->
                        if (results.isEmpty()) {
                            _uiState.update { state ->
                                state.copy(
                                    isLoadingAddress = false,
                                    errorResId = R.string.error_checkout_cep_not_found,
                                )
                            }
                        } else {
                            val firstMatch = results.first()
                            _uiState.update { state ->
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
                        _uiState.update { state ->
                            state.copy(
                                isLoadingAddress = false,
                                errorResId = R.string.error_checkout_cep_failed,
                            )
                        }
                    }
            }
    }

    private fun submitPayment() {
        val currentState = _uiState.value

        if (currentState.cep.length != CEP_LENGTH ||
            currentState.number.isBlank() ||
            currentState.referencePoint.isBlank()
        ) {
            _uiState.update { state -> state.copy(errorResId = R.string.error_checkout_required_fields) }
            return
        }

        isPaymentCompleted = false

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isCreatingPreference = true, errorResId = null) }

            checkoutRepository
                .createPreferenceForCart(currentState.cartItems)
                .onSuccess { response ->
                    _uiState.update { state ->
                        state.copy(
                            isCreatingPreference = false,
                            preferenceId = response.id,
                            sandboxInitPoint = response.sandboxInitPoint,
                            initPoint = response.initPoint,
                        )
                    }
                }.onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isCreatingPreference = false,
                            errorResId = R.string.error_checkout_payment_failed,
                        )
                    }
                }
        }
    }
}
