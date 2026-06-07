package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view

import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab

data class CatalogActions(
    val onTabSelected: (TargaryenTab) -> Unit,
    val onMenuClick: () -> Unit,
    val onLogoutClick: () -> Unit,
    val onCheckoutClick: () -> Unit,
)
