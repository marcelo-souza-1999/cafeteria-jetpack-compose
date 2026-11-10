package com.targaryen.cafeteria.core_designsystem.tracker

import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.annotation.Single

@Single
class AppActionNavigationTracker {
    val navigationEvents: SharedFlow<TargaryenTab>
        field: MutableSharedFlow<TargaryenTab> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    fun navigateToTab(tab: TargaryenTab) {
        navigationEvents.tryEmit(tab)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun consumeEvent() {
        navigationEvents.resetReplayCache()
    }
}
