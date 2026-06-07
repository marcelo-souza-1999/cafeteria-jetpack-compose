package com.targaryen.cafeteria.feature_catalog.profile.presentation.viewmodel

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import com.targaryen.cafeteria.feature_catalog.profile.presentation.intent.ProfileIntent
import com.targaryen.cafeteria.feature_catalog.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ProfileRepository = mockk()

    private val mockUser =
        UserEntity(
            uid = "123",
            name = "Aegon Targaryen",
            email = "aegon@targaryen.com",
            photoUrl = "preset_dragon",
            createdAt = 1000L,
        )

    private val mockHistory =
        listOf(
            PurchaseHistoryItem(
                id = "order_1",
                dateMillis = 2000L,
                totalPrice = 15.5,
                itemsSummary = "2x Dragon Fire Espresso",
                status = "Aprovado",
            ),
        )

    @Before
    fun setup() {
        every { repository.getProfile() } returns flowOf(Resource.Success(mockUser))
        every { repository.getPurchaseHistory() } returns flowOf(Resource.Success(mockHistory))
    }

    @Test
    fun `initialization should fetch profile and purchase history`() =
        runTest {
            val viewModel = ProfileViewModel(repository)
            val state = viewModel.uiState.value

            assertFalse(state.isLoading)
            assertEquals("Aegon Targaryen", state.name)
            assertEquals("aegon@targaryen.com", state.email)
            assertEquals("preset_dragon", state.photoUrl)
            assertEquals(1, state.purchaseHistory.size)
            assertEquals("order_1", state.purchaseHistory[0].id)
        }

    @Test
    fun `when fetchProfile fails, uiState should contain formatted error`() =
        runTest {
            every { repository.getProfile() } returns flowOf(Resource.Error(ProfileError.NetworkError))

            val viewModel = ProfileViewModel(repository)
            val state = viewModel.uiState.value

            assertFalse(state.isLoading)
            assertEquals("As muralhas de Westeros estão instáveis. Verifique sua conexão com o reino.", state.error)
        }

    @Test
    fun `when update photo intent is received, it should delegate to repository`() =
        runTest {
            val photoUrl = "preset_crown"
            every { repository.updateProfilePhoto(photoUrl) } returns flowOf(Resource.Success(Unit))
            val viewModel = ProfileViewModel(repository)

            viewModel.onIntent(ProfileIntent.UpdatePhoto(photoUrl))

            verify { repository.updateProfilePhoto(photoUrl) }
        }

    @Test
    fun `when change password intent is received, it should delegate to repository`() =
        runTest {
            every { repository.updatePassword("old", "new") } returns flowOf(Resource.Success(Unit))
            val viewModel = ProfileViewModel(repository)

            viewModel.onIntent(ProfileIntent.ChangePassword("old", "new"))

            verify { repository.updatePassword("old", "new") }
        }

    @Test
    fun `when change name intent is received, it should delegate to repository`() =
        runTest {
            every { repository.updateProfileName("New Name") } returns flowOf(Resource.Success(Unit))
            val viewModel = ProfileViewModel(repository)

            viewModel.onIntent(ProfileIntent.ChangeName("New Name"))

            verify { repository.updateProfileName("New Name") }
        }

    @Test
    fun `when change email intent is received, it should delegate to repository`() =
        runTest {
            every { repository.updateEmail("new@email.com") } returns flowOf(Resource.Success(Unit))
            val viewModel = ProfileViewModel(repository)

            viewModel.onIntent(ProfileIntent.ChangeEmail("new@email.com"))

            verify { repository.updateEmail("new@email.com") }
        }

    @Test
    fun `when dialog actions intents are received, state should update accordingly`() =
        runTest {
            val viewModel = ProfileViewModel(repository)

            assertFalse(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onIntent(ProfileIntent.ShowDeleteDialog)
            assertTrue(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onIntent(ProfileIntent.DismissDeleteDialog)
            assertFalse(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onIntent(ProfileIntent.ShowSecurityModal)
            assertTrue(viewModel.uiState.value.showSecurityModal)

            viewModel.onIntent(ProfileIntent.DismissSecurityModal)
            assertFalse(viewModel.uiState.value.showSecurityModal)

            viewModel.onIntent(ProfileIntent.ShowPurchaseDetail(mockHistory[0]))
            assertEquals(mockHistory[0], viewModel.uiState.value.selectedPurchase)

            viewModel.onIntent(ProfileIntent.DismissPurchaseDetail)
            assertNull(viewModel.uiState.value.selectedPurchase)

            viewModel.onIntent(ProfileIntent.ToggleHistoryExpansion)
            assertTrue(viewModel.uiState.value.isHistoryExpanded)
        }

    @Test
    fun `when confirm delete account intent is received, it should delete account`() =
        runTest {
            every { repository.deleteAccount() } returns flowOf(Resource.Success(Unit))
            val viewModel = ProfileViewModel(repository)

            viewModel.onIntent(ProfileIntent.ConfirmDeleteAccount)

            verify { repository.deleteAccount() }
        }

    @Test
    fun `when clear messages intent is received, it should reset error and success messages`() =
        runTest {
            every { repository.getProfile() } returns flowOf(Resource.Error(ProfileError.NetworkError))
            val viewModel = ProfileViewModel(repository)

            // Assert initial error state
            assertEquals(
                "As muralhas de Westeros estão instáveis. Verifique sua conexão com o reino.",
                viewModel.uiState.value.error,
            )

            viewModel.onIntent(ProfileIntent.ClearMessages)

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.successMessage)
        }
}
