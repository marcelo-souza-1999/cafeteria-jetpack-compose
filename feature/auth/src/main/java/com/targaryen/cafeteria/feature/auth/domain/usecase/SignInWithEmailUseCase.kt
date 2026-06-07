package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class SignInWithEmailUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(
        email: String,
        pass: String,
    ) = repository.signInWithEmail(email, pass)
}
