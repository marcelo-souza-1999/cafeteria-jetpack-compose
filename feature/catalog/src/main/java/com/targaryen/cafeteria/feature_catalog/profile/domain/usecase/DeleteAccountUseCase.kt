package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class DeleteAccountUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<Resource<Unit, ProfileError>> {
        return repository.deleteAccount()
    }
}
