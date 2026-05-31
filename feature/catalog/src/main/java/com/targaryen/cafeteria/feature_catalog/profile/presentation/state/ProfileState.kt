package com.targaryen.cafeteria.feature_catalog.profile.presentation.state

import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem

data class ProfileState(
    val isLoading: Boolean = false,
    val isUpdatingPhoto: Boolean = false,
    val isHistoryExpanded: Boolean = false,
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val purchaseHistory: List<PurchaseHistoryItem> = emptyList(),
    
    val error: String? = null,
    val successMessage: String? = null,
    
    val showDeleteConfirmation: Boolean = false,
    val showSecurityModal: Boolean = false,
    val showEmailSuccessDialog: Boolean = false,
    val isAccountDeleted: Boolean = false,
    
    val selectedPurchase: PurchaseHistoryItem? = null
)
