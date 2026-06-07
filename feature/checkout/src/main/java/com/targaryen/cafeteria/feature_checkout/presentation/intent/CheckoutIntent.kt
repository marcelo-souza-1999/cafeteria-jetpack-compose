package com.targaryen.cafeteria.feature_checkout.presentation.intent

sealed interface CheckoutIntent {
    data class OnCepChanged(
        val cep: String,
    ) : CheckoutIntent

    data class OnNumberChanged(
        val number: String,
    ) : CheckoutIntent

    data class OnReferencePointChanged(
        val reference: String,
    ) : CheckoutIntent

    data class OnRecipientNameChanged(
        val name: String,
    ) : CheckoutIntent

    data object OnSearchAddressClicked : CheckoutIntent

    data object OnDismissCepModal : CheckoutIntent

    data class OnSearchReverseCep(
        val state: String,
        val city: String,
        val street: String,
    ) : CheckoutIntent

    data object OnSubmitPayment : CheckoutIntent

    data object OnPaymentInitiated : CheckoutIntent

    data object OnLoadCartItems : CheckoutIntent

    data object OnDismissError : CheckoutIntent

    data object OnCancelCheckout : CheckoutIntent

    data object OnDismissCancelNotice : CheckoutIntent

    data object OnDismissSuccessNotice : CheckoutIntent

    data object OnDisabledFieldClick : CheckoutIntent

    data object OnResetState : CheckoutIntent
}
