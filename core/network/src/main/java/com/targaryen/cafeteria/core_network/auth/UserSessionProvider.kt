package com.targaryen.cafeteria.core_network.auth

/**
 * Abstração pura da sessão do usuário autenticado para desacoplar
 * camadas de apresentação e domínio de singletons estáticos do FirebaseAuth.
 */
interface UserSessionProvider {
    fun getCurrentUserId(): String?

    fun isAuthenticated(): Boolean
}
