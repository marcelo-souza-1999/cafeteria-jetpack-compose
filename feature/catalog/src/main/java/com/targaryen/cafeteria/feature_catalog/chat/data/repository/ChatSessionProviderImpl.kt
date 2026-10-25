package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatSessionProvider
import org.koin.core.annotation.Single

@Single
class ChatSessionProviderImpl : ChatSessionProvider {
    override fun getCurrentUserUid(): String? = FirebaseAuth.getInstance().currentUser?.uid
}
