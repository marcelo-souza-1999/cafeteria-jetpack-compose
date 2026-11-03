package com.targaryen.cafeteria.core_network.tracker

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.annotation.Single

enum class PaymentStatus {
    SUCCESS,
    FAILURE,
    PENDING,
    CANCELLED,
}

@Single
class PaymentStatusTracker {
    val paymentStatus: SharedFlow<PaymentStatus>
        field: MutableSharedFlow<PaymentStatus> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    fun updateStatus(status: PaymentStatus) {
        paymentStatus.tryEmit(status)
    }
}
