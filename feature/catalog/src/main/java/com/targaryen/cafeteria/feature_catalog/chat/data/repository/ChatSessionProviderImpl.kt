package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import org.koin.core.annotation.Single

@Single
class ChatSessionProviderImpl(
    private val firebaseAuth: FirebaseAuth,
) : ChatSessionProvider {
    override fun getCurrentUserUid(): String? = firebaseAuth.currentUser?.uid
}
