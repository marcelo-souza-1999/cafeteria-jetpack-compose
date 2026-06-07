package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class UpdateProfilePhotoUseCase(
    private val repository: ProfileRepository,
) {
    operator fun invoke(photoUrl: String): Flow<Resource<Unit, ProfileError>> = repository.updateProfilePhoto(photoUrl)
}
