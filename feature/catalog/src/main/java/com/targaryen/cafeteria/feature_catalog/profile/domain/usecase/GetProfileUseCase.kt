package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetProfileUseCase(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<Resource<UserEntity, ProfileError>> = repository.getProfile()
}
