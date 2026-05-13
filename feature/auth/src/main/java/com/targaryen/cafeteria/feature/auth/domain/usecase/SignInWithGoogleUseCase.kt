package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Resource<Unit, AuthError>> {
        return repository.signInWithGoogle(idToken)
    }
}
