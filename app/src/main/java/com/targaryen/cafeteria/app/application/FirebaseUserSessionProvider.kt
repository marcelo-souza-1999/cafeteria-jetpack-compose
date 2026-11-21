package com.targaryen.cafeteria.app.application

import com.google.firebase.auth.FirebaseAuth
import com.targaryen.cafeteria.core_network.auth.UserSessionProvider
import org.koin.core.annotation.Single

/**
 * Implementação de [UserSessionProvider] respaldada pelo [FirebaseAuth] gerenciado pelo Koin.
 */
@Single
class FirebaseUserSessionProvider(
    private val firebaseAuth: FirebaseAuth,
) : UserSessionProvider {
    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null
}
