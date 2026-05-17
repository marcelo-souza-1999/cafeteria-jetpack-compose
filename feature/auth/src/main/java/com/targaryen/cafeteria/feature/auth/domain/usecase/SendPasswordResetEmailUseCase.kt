package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SendPasswordResetEmailUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Resource<Unit, AuthError>> {
        return authRepository.sendPasswordResetEmail(email)
    }
}
