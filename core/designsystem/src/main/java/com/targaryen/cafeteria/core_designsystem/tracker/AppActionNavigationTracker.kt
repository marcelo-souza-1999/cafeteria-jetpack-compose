package com.targaryen.cafeteria.core_designsystem.tracker

import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
class AppActionNavigationTracker {
    val navigationEvents: SharedFlow<TargaryenTab>
        field: MutableSharedFlow<TargaryenTab> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    val openLatestOrderRequest: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun navigateToTab(tab: TargaryenTab) {
        navigationEvents.tryEmit(tab)
    }

    fun requestOpenLatestOrder() {
        openLatestOrderRequest.value = true
    }

    fun consumeOpenLatestOrder() {
        openLatestOrderRequest.value = false
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun consumeEvent() {
        navigationEvents.resetReplayCache()
    }
}
