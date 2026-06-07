package com.targaryen.cafeteria.feature_checkout.presentation.state

import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product

data class CheckoutState(
    val cep: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val number: String = "",
    val referencePoint: String = "",
    val recipientName: String = "",
    val cartItems: List<Product> = emptyList(),
    val isLoadingAddress: Boolean = false,
    val isCreatingPreference: Boolean = false,
    val errorResId: Int? = null,
    val preferenceId: String? = null,
    val sandboxInitPoint: String? = null,
    val initPoint: String? = null,
    val showCepModal: Boolean = false,
    val isRedirecting: Boolean = false,
    val showCancelNotice: Boolean = false,
    val showSuccessNotice: Boolean = false,
    val showAddressFieldsError: Boolean = false,
)
