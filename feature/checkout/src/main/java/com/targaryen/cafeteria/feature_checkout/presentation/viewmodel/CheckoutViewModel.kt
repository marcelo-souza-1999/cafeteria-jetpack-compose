package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.feature_catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import com.targaryen.cafeteria.feature_checkout.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

import com.targaryen.cafeteria.core_network.PaymentStatus
import com.targaryen.cafeteria.core_network.PaymentStatusTracker

@KoinViewModel
class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val catalogRepository: CatalogRepository,
    private val paymentStatusTracker: PaymentStatusTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutState())
    val uiState: StateFlow<CheckoutState> = _uiState.asStateFlow()

    private var isPaymentCompleted = false

    init {
        onIntent(CheckoutIntent.OnLoadCartItems)
        observePaymentStatus()
    }

    private fun observePaymentStatus() {
        viewModelScope.launch {
            paymentStatusTracker.paymentStatus.collectLatest { status ->
                when (status) {
                    PaymentStatus.SUCCESS -> {
                        isPaymentCompleted = true
                        viewModelScope.launch {
                            catalogRepository.clearCart()
                        }
                        _uiState.update { it.copy(isRedirecting = false, showSuccessNotice = true) }
                    }
                    PaymentStatus.FAILURE -> {
                        _uiState.update { it.copy(isRedirecting = false, errorResId = R.string.error_checkout_payment_failed) }
                    }
                    PaymentStatus.PENDING -> {
                        isPaymentCompleted = true
                        viewModelScope.launch {
                            catalogRepository.clearCart()
                        }
                        _uiState.update { it.copy(isRedirecting = false, showSuccessNotice = true) }
                    }
                    PaymentStatus.CANCELLED -> {
                        _uiState.update { it.copy(isRedirecting = false, showCancelNotice = true) }
                    }
                }
            }
        }
    }

    fun onIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.OnLoadCartItems -> loadCartItems()
            is CheckoutIntent.OnCepChanged -> handleCepChange(intent.cep)
            is CheckoutIntent.OnNumberChanged -> _uiState.update { it.copy(number = intent.number) }
            is CheckoutIntent.OnReferencePointChanged -> _uiState.update { it.copy(referencePoint = intent.reference) }
            is CheckoutIntent.OnRecipientNameChanged -> _uiState.update { it.copy(recipientName = intent.name) }
            is CheckoutIntent.OnSubmitPayment -> submitPayment()
            is CheckoutIntent.OnSearchAddressClicked -> _uiState.update { it.copy(showCepModal = true) }
            is CheckoutIntent.OnDismissCepModal -> _uiState.update { it.copy(showCepModal = false) }
            is CheckoutIntent.OnSearchReverseCep -> searchReverseCep(intent.state, intent.city, intent.street)
            is CheckoutIntent.OnPaymentInitiated -> {
                _uiState.update { it.copy(preferenceId = null, sandboxInitPoint = null, isRedirecting = true) }
            }
            is CheckoutIntent.OnDismissError -> _uiState.update { it.copy(errorResId = null) }
            is CheckoutIntent.OnCancelCheckout -> {
                if (!isPaymentCompleted) {
                    _uiState.update { it.copy(isRedirecting = false, showCancelNotice = true) }
                } else {
                    _uiState.update { it.copy(isRedirecting = false) }
                }
            }
            is CheckoutIntent.OnDismissCancelNotice -> _uiState.update { it.copy(showCancelNotice = false) }
            is CheckoutIntent.OnDismissSuccessNotice -> _uiState.update { it.copy(showSuccessNotice = false) }
            is CheckoutIntent.OnDisabledFieldClick -> {
                _uiState.update { it.copy(showAddressFieldsError = true) }
            }
        }
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            catalogRepository.getProducts()
                .map { products -> products.filter { it.quantityInCart > 0 } }
                .collectLatest { cartItems ->
                    _uiState.update { it.copy(cartItems = cartItems) }
                }
        }
    }

    private fun handleCepChange(newCep: String) {
        val digitsOnly = newCep.filter { it.isDigit() }
        if (digitsOnly.length <= 8) {
            _uiState.update { it.copy(cep = digitsOnly, showAddressFieldsError = false) }
            
            if (digitsOnly.length == 8) {
                fetchAddress(digitsOnly)
            } else {
                _uiState.update { it.copy(street = "", city = "", state = "", errorResId = null) }
            }
        }
    }

    private fun fetchAddress(cep: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAddress = true, errorResId = null) }
            checkoutRepository.fetchAddressByCep(cep)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoadingAddress = false,
                            street = response.logradouro ?: "",
                            city = response.localidade ?: "",
                            state = response.uf ?: "",
                            showAddressFieldsError = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingAddress = false,
                            errorResId = R.string.error_checkout_cep_failed
                        )
                    }
                }
        }
    }

    private fun searchReverseCep(state: String, city: String, street: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAddress = true, showCepModal = false, errorResId = null) }
            checkoutRepository.searchCepByAddress(state, city, street)
                .onSuccess { results ->
                    if (results.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoadingAddress = false,
                                errorResId = R.string.error_checkout_cep_not_found
                            )
                        }
                    } else {
                        val firstMatch = results.first()
                        _uiState.update {
                            it.copy(
                                isLoadingAddress = false,
                                cep = firstMatch.cep?.filter { c -> c.isDigit() } ?: "",
                                street = firstMatch.logradouro ?: "",
                                city = firstMatch.localidade ?: "",
                                state = firstMatch.uf ?: "",
                                showAddressFieldsError = false
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingAddress = false,
                            errorResId = R.string.error_checkout_cep_failed
                        )
                    }
                }
        }
    }

    private fun submitPayment() {
        val currentState = _uiState.value
        
        if (currentState.cep.length != 8 || currentState.number.isBlank() || currentState.referencePoint.isBlank()) {
            _uiState.update { it.copy(errorResId = R.string.error_checkout_required_fields) }
            return
        }

        isPaymentCompleted = false

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPreference = true, errorResId = null) }
            
            checkoutRepository.createPreferenceForCart(currentState.cartItems)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isCreatingPreference = false,
                            preferenceId = response.id,
                            sandboxInitPoint = response.sandboxInitPoint
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isCreatingPreference = false,
                            errorResId = R.string.error_checkout_payment_failed
                        )
                    }
                }
        }
    }
}
