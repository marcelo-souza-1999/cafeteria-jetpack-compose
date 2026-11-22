package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal data class RestoredCheckoutSnapshot(
    val state: CheckoutState,
    val isPaymentCompleted: Boolean,
)

/**
 * Encapsula leitura, persistência e limpeza de estado efêmero do checkout no [SavedStateHandle]
 * para sobrevivência contra Process Death pelo sistema operacional.
 */
internal class CheckoutSavedStateStore(
    private val savedStateHandle: SavedStateHandle,
) {
    fun hasSavedState(): Boolean = savedStateHandle.contains(KEY_SAVED_MARKER)

    fun updateAndSave(
        uiState: MutableStateFlow<CheckoutState>,
        isPaymentCompleted: Boolean,
        transform: (CheckoutState) -> CheckoutState,
    ) {
        uiState.update(transform)
        save(uiState.value, isPaymentCompleted)
    }

    fun save(
        state: CheckoutState,
        isPaymentCompleted: Boolean,
    ) {
        savedStateHandle[KEY_SAVED_MARKER] = true
        savedStateHandle[KEY_CEP] = state.cep
        savedStateHandle[KEY_STREET] = state.street
        savedStateHandle[KEY_CITY] = state.city
        savedStateHandle[KEY_STATE] = state.state
        savedStateHandle[KEY_NUMBER] = state.number
        savedStateHandle[KEY_REFERENCE_POINT] = state.referencePoint
        savedStateHandle[KEY_RECIPIENT_NAME] = state.recipientName
        savedStateHandle[KEY_IS_REDIRECTING] = state.isRedirecting
        savedStateHandle[KEY_IS_PAYMENT_COMPLETED] = isPaymentCompleted
    }

    fun restore(baseState: CheckoutState): RestoredCheckoutSnapshot {
        val restored =
            baseState.copy(
                cep = savedStateHandle.get<String>(KEY_CEP).orEmpty(),
                street = savedStateHandle.get<String>(KEY_STREET).orEmpty(),
                city = savedStateHandle.get<String>(KEY_CITY).orEmpty(),
                state = savedStateHandle.get<String>(KEY_STATE).orEmpty(),
                number = savedStateHandle.get<String>(KEY_NUMBER).orEmpty(),
                referencePoint = savedStateHandle.get<String>(KEY_REFERENCE_POINT).orEmpty(),
                recipientName = savedStateHandle.get<String>(KEY_RECIPIENT_NAME).orEmpty(),
                isRedirecting = savedStateHandle.get<Boolean>(KEY_IS_REDIRECTING) ?: false,
            )
        val paymentDone = savedStateHandle.get<Boolean>(KEY_IS_PAYMENT_COMPLETED) ?: false
        return RestoredCheckoutSnapshot(state = restored, isPaymentCompleted = paymentDone)
    }

    fun clear() {
        ALL_KEYS.forEach { key ->
            savedStateHandle.remove<Any?>(key)
        }
    }

    companion object {
        const val KEY_SAVED_MARKER = "checkout_state_saved"
        const val KEY_CEP = "checkout_cep"
        const val KEY_STREET = "checkout_street"
        const val KEY_CITY = "checkout_city"
        const val KEY_STATE = "checkout_state"
        const val KEY_NUMBER = "checkout_number"
        const val KEY_REFERENCE_POINT = "checkout_reference_point"
        const val KEY_RECIPIENT_NAME = "checkout_recipient_name"
        const val KEY_IS_REDIRECTING = "checkout_is_redirecting"
        const val KEY_IS_PAYMENT_COMPLETED = "checkout_is_payment_completed"

        private val ALL_KEYS =
            listOf(
                KEY_SAVED_MARKER,
                KEY_CEP,
                KEY_STREET,
                KEY_CITY,
                KEY_STATE,
                KEY_NUMBER,
                KEY_REFERENCE_POINT,
                KEY_RECIPIENT_NAME,
                KEY_IS_REDIRECTING,
                KEY_IS_PAYMENT_COMPLETED,
            )
    }
}
