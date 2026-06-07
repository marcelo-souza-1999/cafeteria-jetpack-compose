package com.targaryen.cafeteria.feature_catalog.profile.domain.repository

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import kotlinx.coroutines.flow.Flow

sealed interface ProfileError {
    object NetworkError : ProfileError

    object UserNotFound : ProfileError

    object InvalidPassword : ProfileError

    data class Unknown(
        val message: String?,
    ) : ProfileError
}

interface ProfileRepository {
    fun getProfile(): Flow<Resource<UserEntity, ProfileError>>

    fun updateProfilePhoto(photoUrl: String): Flow<Resource<Unit, ProfileError>>

    fun uploadProfilePhoto(uri: android.net.Uri): Flow<Resource<String, ProfileError>>

    fun updateProfileName(name: String): Flow<Resource<Unit, ProfileError>>

    fun updateEmail(email: String): Flow<Resource<Unit, ProfileError>>

    fun getPurchaseHistory(): Flow<Resource<List<PurchaseHistoryItem>, ProfileError>>

    fun updatePassword(
        currentPass: String,
        newPass: String,
    ): Flow<Resource<Unit, ProfileError>>

    fun deleteAccount(): Flow<Resource<Unit, ProfileError>>
}
