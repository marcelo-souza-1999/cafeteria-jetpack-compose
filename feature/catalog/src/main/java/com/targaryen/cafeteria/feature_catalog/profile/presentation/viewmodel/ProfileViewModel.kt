package com.targaryen.cafeteria.feature_catalog.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_designsystem.tracker.AppActionNavigationTracker
import com.targaryen.cafeteria.core_network.telemetry.AppTelemetry
import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import com.targaryen.cafeteria.feature_catalog.profile.presentation.intent.ProfileIntent
import com.targaryen.cafeteria.feature_catalog.profile.presentation.state.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val navigationTracker: AppActionNavigationTracker,
    private val telemetry: AppTelemetry,
) : ViewModel() {
    val uiState: StateFlow<ProfileState>
        field: MutableStateFlow<ProfileState> = MutableStateFlow(ProfileState())

    init {
        onIntent(ProfileIntent.FetchProfile)
        viewModelScope.launch {
            navigationTracker.openLatestOrderRequest.collect { shouldOpen ->
                if (shouldOpen) {
                    val latestOrder = uiState.value.purchaseHistory.firstOrNull()
                    if (latestOrder != null) {
                        uiState.update { state ->
                            state.copy(
                                isHistoryExpanded = true,
                                selectedPurchase = latestOrder,
                            )
                        }
                        navigationTracker.consumeOpenLatestOrder()
                    }
                }
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.FetchProfile -> fetchProfile()
            is ProfileIntent.UpdatePhoto -> updatePhoto(intent.photoUrl)
            is ProfileIntent.UploadPhoto -> uploadPhoto(intent)
            is ProfileIntent.ChangePassword -> changePassword(intent.currentPass, intent.newPass)
            is ProfileIntent.ChangeName -> changeName(intent.newName)
            is ProfileIntent.ChangeEmail -> changeEmail(intent.newEmail)

            is ProfileIntent.ShowDeleteDialog,
            is ProfileIntent.DismissDeleteDialog,
            is ProfileIntent.ConfirmDeleteAccount,
            is ProfileIntent.ShowSecurityModal,
            is ProfileIntent.DismissSecurityModal,
            is ProfileIntent.ShowEmailSuccessDialog,
            is ProfileIntent.DismissEmailSuccessDialog,
            is ProfileIntent.ShowPurchaseDetail,
            is ProfileIntent.DismissPurchaseDetail,
            is ProfileIntent.ToggleHistoryExpansion,
            -> handleNavigationAndDialogs(intent)

            is ProfileIntent.ClearMessages ->
                uiState.update { state ->
                    state.copy(
                        error = null,
                        successMessage = null,
                    )
                }
        }
    }

    private fun handleNavigationAndDialogs(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ShowDeleteDialog -> uiState.update { state -> state.copy(showDeleteConfirmation = true) }
            is ProfileIntent.DismissDeleteDialog ->
                uiState.update { state ->
                    state.copy(showDeleteConfirmation = false)
                }
            is ProfileIntent.ConfirmDeleteAccount -> deleteAccount()
            is ProfileIntent.ShowSecurityModal -> uiState.update { state -> state.copy(showSecurityModal = true) }
            is ProfileIntent.DismissSecurityModal -> uiState.update { state -> state.copy(showSecurityModal = false) }
            is ProfileIntent.ShowEmailSuccessDialog ->
                uiState.update { state ->
                    state.copy(showEmailSuccessDialog = true)
                }
            is ProfileIntent.DismissEmailSuccessDialog ->
                uiState.update { state ->
                    state.copy(showEmailSuccessDialog = false)
                }
            is ProfileIntent.ShowPurchaseDetail -> {
                telemetry.trackEvent("profile_order_detail_viewed")
                uiState.update { state ->
                    state.copy(selectedPurchase = intent.item)
                }
            }
            is ProfileIntent.DismissPurchaseDetail -> uiState.update { state -> state.copy(selectedPurchase = null) }
            is ProfileIntent.ToggleHistoryExpansion ->
                uiState.update { state ->
                    state.copy(isHistoryExpanded = !state.isHistoryExpanded)
                }
            else -> {}
        }
    }

    private fun fetchProfile() {
        uiState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                name = resource.data.name,
                                email = resource.data.email,
                                photoUrl = resource.data.photoUrl,
                            )
                        }
                        fetchPurchaseHistory()
                    }
                    is Resource.Error -> {
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchPurchaseHistory() {
        viewModelScope.launch {
            repository.getPurchaseHistory().collect { resource ->
                if (resource is Resource.Success) {
                    val history = resource.data
                    val shouldOpenLatest = navigationTracker.openLatestOrderRequest.value && history.isNotEmpty()
                    uiState.update { state ->
                        state.copy(
                            purchaseHistory = history,
                            isHistoryExpanded = state.isHistoryExpanded || shouldOpenLatest,
                            selectedPurchase = if (shouldOpenLatest) history.first() else state.selectedPurchase,
                        )
                    }
                    if (navigationTracker.openLatestOrderRequest.value) {
                        navigationTracker.consumeOpenLatestOrder()
                    }
                }
            }
        }
    }

    private fun updatePhoto(url: String) {
        uiState.update { state -> state.copy(isUpdatingPhoto = true) }
        viewModelScope.launch {
            repository.updateProfilePhoto(url).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("profile_avatar_updated", mapOf("success" to true))
                        uiState.update { state ->
                            state.copy(
                                isUpdatingPhoto = false,
                                photoUrl = url,
                                successMessage = "Avatar atualizado com sucesso.",
                            )
                        }
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("profile_avatar_updated", mapOf("success" to false))
                        telemetry.trackNonFatalError(
                            "profile_update_photo",
                            IllegalStateException(resource.error.toString()),
                        )
                        uiState.update { state ->
                            state.copy(
                                isUpdatingPhoto = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun uploadPhoto(intent: ProfileIntent.UploadPhoto) {
        uiState.update { state -> state.copy(isUpdatingPhoto = true) }
        viewModelScope.launch {
            repository.uploadProfilePhoto(intent.uri).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val uploadedUrl = resource.data
                        updatePhoto(uploadedUrl)
                    }
                    is Resource.Error -> {
                        telemetry.trackNonFatalError(
                            "profile_upload_photo",
                            IllegalStateException(resource.error.toString()),
                        )
                        uiState.update { state ->
                            state.copy(
                                isUpdatingPhoto = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun changePassword(
        currentPass: String,
        newPass: String,
    ) {
        uiState.update { state -> state.copy(isLoading = true, showSecurityModal = false) }
        viewModelScope.launch {
            repository.updatePassword(currentPass, newPass).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("profile_password_updated", mapOf("success" to true))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                successMessage = "Segredos alterados com glória. Suas defesas estão renovadas.",
                            )
                        }
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("profile_password_updated", mapOf("success" to false))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun changeName(name: String) {
        uiState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            repository.updateProfileName(name).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("profile_name_updated", mapOf("success" to true))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                name = name,
                                successMessage = "Título real alterado.",
                            )
                        }
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("profile_name_updated", mapOf("success" to false))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun changeEmail(email: String) {
        uiState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            repository.updateEmail(email).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("profile_email_updated", mapOf("success" to true))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                showEmailSuccessDialog = true,
                            )
                        }
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("profile_email_updated", mapOf("success" to false))
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun deleteAccount() {
        uiState.update { state -> state.copy(isLoading = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            repository.deleteAccount().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        telemetry.trackEvent("profile_account_deleted", mapOf("success" to true))
                        telemetry.setCustomKey("is_authenticated", false)
                        uiState.update { state -> state.copy(isLoading = false, isAccountDeleted = true) }
                    }
                    is Resource.Error -> {
                        telemetry.trackEvent("profile_account_deleted", mapOf("success" to false))
                        telemetry.trackNonFatalError(
                            "profile_delete_account",
                            IllegalStateException(resource.error.toString()),
                        )
                        uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = resource.error.toFormattedString(),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ProfileError.toFormattedString(): String =
    when (this) {
        is ProfileError.NetworkError -> "As muralhas de Westeros estão instáveis. Verifique sua conexão com o reino."
        is ProfileError.UserNotFound -> "Linhagem de nobreza não localizada nos pergaminhos reais."
        is ProfileError.InvalidPassword -> "Selo inválido. Reautorize seu acesso antes de alterar seus segredos."
        is ProfileError.Unknown -> this.message ?: "O fogo do dragão causou uma anomalia desconhecida."
    }
