package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SignUpWithEmailUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(
        name: String,
        email: String,
        pass: String,
    ): Flow<Resource<Unit, AuthError>> = repository.signUpWithEmail(name, email, pass)
}
