package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class CheckAuthSessionUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isUserLoggedIn()
    }
}
