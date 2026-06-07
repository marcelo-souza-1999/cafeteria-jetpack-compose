package com.targaryen.cafeteria.core_network.tracker

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

enum class PaymentStatus {
    SUCCESS,
    FAILURE,
    PENDING,
    CANCELLED,
}

@Single
class PaymentStatusTracker {
    private val _paymentStatus = MutableSharedFlow<PaymentStatus>(replay = 1, extraBufferCapacity = 1)
    val paymentStatus: SharedFlow<PaymentStatus> = _paymentStatus.asSharedFlow()

    fun updateStatus(status: PaymentStatus) {
        _paymentStatus.tryEmit(status)
    }
}
