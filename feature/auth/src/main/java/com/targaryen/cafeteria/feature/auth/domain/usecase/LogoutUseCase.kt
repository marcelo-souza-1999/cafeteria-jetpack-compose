package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
