package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import android.net.Uri
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProfileUseCasesTest {
    private val repository: ProfileRepository = mockk()

    @Test
    fun `GetProfileUseCase should delegate to repository`() =
        runTest {
            every { repository.getProfile() } returns flowOf(Resource.Success(mockk()))

            GetProfileUseCase(repository)().collect()

            verify { repository.getProfile() }
        }

    @Test
    fun `DeleteAccountUseCase should delegate to repository`() =
        runTest {
            every { repository.deleteAccount() } returns flowOf(Resource.Success(Unit))

            DeleteAccountUseCase(repository)().collect()

            verify { repository.deleteAccount() }
        }

    @Test
    fun `GetPurchaseHistoryUseCase should delegate to repository`() =
        runTest {
            val mockHistory = listOf<PurchaseHistoryItem>()
            every { repository.getPurchaseHistory() } returns flowOf(Resource.Success(mockHistory))

            GetPurchaseHistoryUseCase(repository)().collect()

            verify { repository.getPurchaseHistory() }
        }

    @Test
    fun `UpdateEmailUseCase should delegate to repository`() =
        runTest {
            val email = "new@test.com"
            every { repository.updateEmail(email) } returns flowOf(Resource.Success(Unit))

            UpdateEmailUseCase(repository)(email).collect()

            verify { repository.updateEmail(email) }
        }

    @Test
    fun `UpdatePasswordUseCase should delegate to repository`() =
        runTest {
            val currentPass = "current"
            val newPass = "new"
            every { repository.updatePassword(currentPass, newPass) } returns flowOf(Resource.Success(Unit))

            UpdatePasswordUseCase(repository)(currentPass, newPass).collect()

            verify { repository.updatePassword(currentPass, newPass) }
        }

    @Test
    fun `UpdateProfileNameUseCase should delegate to repository`() =
        runTest {
            val newName = "New Name"
            every { repository.updateProfileName(newName) } returns flowOf(Resource.Success(Unit))

            UpdateProfileNameUseCase(repository)(newName).collect()

            verify { repository.updateProfileName(newName) }
        }

    @Test
    fun `UpdateProfilePhotoUseCase should delegate to repository`() =
        runTest {
            val photoUrl = "http://test.com/photo.jpg"
            every { repository.updateProfilePhoto(photoUrl) } returns flowOf(Resource.Success(Unit))

            UpdateProfilePhotoUseCase(repository)(photoUrl).collect()

            verify { repository.updateProfilePhoto(photoUrl) }
        }

    @Test
    fun `UploadProfilePhotoUseCase should delegate to repository`() =
        runTest {
            val mockUri = mockk<Uri>()
            every { repository.uploadProfilePhoto(mockUri) } returns flowOf(Resource.Success("http://photo.url"))

            UploadProfilePhotoUseCase(repository)(mockUri).collect()

            verify { repository.uploadProfilePhoto(mockUri) }
        }
}
