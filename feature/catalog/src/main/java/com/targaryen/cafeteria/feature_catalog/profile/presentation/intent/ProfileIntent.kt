package com.targaryen.cafeteria.feature_catalog.profile.presentation.intent

import android.net.Uri
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem

sealed interface ProfileIntent {
    object FetchProfile : ProfileIntent

    data class UpdatePhoto(
        val photoUrl: String,
    ) : ProfileIntent

    data class UploadPhoto(
        val uri: Uri,
    ) : ProfileIntent

    data class ChangePassword(
        val currentPass: String,
        val newPass: String,
    ) : ProfileIntent

    data class ChangeName(
        val newName: String,
    ) : ProfileIntent

    data class ChangeEmail(
        val newEmail: String,
    ) : ProfileIntent

    object ShowDeleteDialog : ProfileIntent

    object DismissDeleteDialog : ProfileIntent

    object ConfirmDeleteAccount : ProfileIntent

    // Security Modal
    object ShowSecurityModal : ProfileIntent

    object DismissSecurityModal : ProfileIntent

    // Email Success Modal
    object ShowEmailSuccessDialog : ProfileIntent

    object DismissEmailSuccessDialog : ProfileIntent

    // Purchase Detail Modal
    data class ShowPurchaseDetail(
        val item: PurchaseHistoryItem,
    ) : ProfileIntent

    object DismissPurchaseDetail : ProfileIntent

    object ToggleHistoryExpansion : ProfileIntent

    object ClearMessages : ProfileIntent
}
