package com.targaryen.cafeteria.feature.auth.domain.repository

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signInWithEmail(email: String, pass: String): Flow<Resource<Unit, AuthError>>
    fun isUserLoggedIn(): Boolean
}
