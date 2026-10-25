package com.targaryen.cafeteria.feature_catalog.chat.domain.repository

interface ChatSessionProvider {
    fun getCurrentUserUid(): String?
}
