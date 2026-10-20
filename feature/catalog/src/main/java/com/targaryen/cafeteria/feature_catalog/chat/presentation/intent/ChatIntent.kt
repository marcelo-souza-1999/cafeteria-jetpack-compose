package com.targaryen.cafeteria.feature_catalog.chat.presentation.intent

import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel

sealed interface ChatIntent {
    data class SendMessage(
        val text: String,
    ) : ChatIntent

    data class UpdateInputText(
        val text: String,
    ) : ChatIntent

    object ClearChat : ChatIntent

    object NewSession : ChatIntent

    data class SelectSession(
        val sessionId: String,
    ) : ChatIntent

    data class ToggleHistory(
        val show: Boolean,
    ) : ChatIntent

    data class DeleteSession(
        val sessionId: String,
    ) : ChatIntent

    data class SelectProductSuggestion(
        val product: ProductUiModel,
    ) : ChatIntent
}
