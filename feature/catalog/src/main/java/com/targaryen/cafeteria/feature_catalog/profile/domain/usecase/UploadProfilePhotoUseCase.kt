package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import android.net.Uri
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class UploadProfilePhotoUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(uri: Uri): Flow<Resource<String, ProfileError>> {
        return repository.uploadProfilePhoto(uri)
    }
}
