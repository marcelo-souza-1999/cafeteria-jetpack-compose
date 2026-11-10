package com.targaryen.cafeteria.app.navigation

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.tracker.AppActionNavigationTracker
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.findBestMatchingProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class CafeteriaIntentRouter(
    private val paymentStatusTracker: PaymentStatusTracker,
    private val catalogRepository: CatalogRepository,
    private val appActionNavigationTracker: AppActionNavigationTracker,
) {
    fun routeIntent(
        intent: Intent?,
        scope: CoroutineScope,
    ) {
        if (intent == null) return

        when {
            intent.action == Intent.ACTION_SEARCH || intent.action == AppActionConstants.GMS_SEARCH_ACTION -> {
                handleSearchIntent(intent, scope)
            }
            !intent.getStringExtra(AppActionConstants.Extra.ACTION).isNullOrBlank() -> {
                handleExtraAction(intent, scope)
            }
            intent.data?.scheme == AppActionConstants.SCHEME -> {
                intent.data?.let { uri -> handleUriIntent(uri, scope) }
            }
        }
    }

    private fun handleSearchIntent(
        intent: Intent,
        scope: CoroutineScope,
    ) {
        val query = intent.getStringExtra(SearchManager.QUERY).orEmpty()
        if (query.isNotBlank()) {
            handleOrderActionByName(query, 1, scope)
        } else {
            appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleExtraAction(
        intent: Intent,
        scope: CoroutineScope,
    ) {
        val action = intent.getStringExtra(AppActionConstants.Extra.ACTION).orEmpty()
        when (action) {
            AppActionConstants.Action.ORDER -> {
                val productName = intent.getStringExtra(AppActionConstants.Extra.PRODUCT_NAME)
                val quantity = intent.getStringExtra(AppActionConstants.Extra.QUANTITY)?.toIntOrNull() ?: 1
                if (!productName.isNullOrBlank()) {
                    handleOrderActionByName(productName, quantity, scope)
                } else {
                    appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
                }
            }
            AppActionConstants.Action.CLEAR_CART -> handleClearCartAction(scope)
            AppActionConstants.Action.CART_SUMMARY,
            AppActionConstants.Action.CART,
            -> appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
            AppActionConstants.Action.FAVORITES -> appActionNavigationTracker.navigateToTab(TargaryenTab.FAVORITES)
            AppActionConstants.Action.CHAT -> appActionNavigationTracker.navigateToTab(TargaryenTab.CHAT)
            AppActionConstants.Action.STATUS -> appActionNavigationTracker.navigateToTab(TargaryenTab.PROFILE)
            AppActionConstants.Action.SEARCH -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
            else -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleUriIntent(
        data: Uri,
        scope: CoroutineScope,
    ) {
        when (data.host) {
            AppActionConstants.Host.CHECKOUT -> handleCheckoutIntent(data)
            AppActionConstants.Host.ACTION -> handleActionIntent(data, scope)
        }
    }

    private fun handleCheckoutIntent(data: Uri) {
        val status =
            when (data.lastPathSegment) {
                AppActionConstants.CheckoutStatus.SUCCESS -> PaymentStatus.SUCCESS
                AppActionConstants.CheckoutStatus.FAILURE -> PaymentStatus.FAILURE
                AppActionConstants.CheckoutStatus.PENDING -> PaymentStatus.PENDING
                else -> PaymentStatus.CANCELLED
            }
        paymentStatusTracker.updateStatus(status)
    }

    private fun handleActionIntent(
        data: Uri,
        scope: CoroutineScope,
    ) {
        val actionType = data.pathSegments.firstOrNull() ?: data.host
        when (actionType) {
            AppActionConstants.Action.ORDER -> handleOrderAction(data, scope)
            AppActionConstants.Action.CLEAR_CART -> handleClearCartAction(scope)
            AppActionConstants.Action.CART_SUMMARY,
            AppActionConstants.Action.CART,
            -> appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
            AppActionConstants.Action.FAVORITES -> appActionNavigationTracker.navigateToTab(TargaryenTab.FAVORITES)
            AppActionConstants.Action.CHAT -> appActionNavigationTracker.navigateToTab(TargaryenTab.CHAT)
            AppActionConstants.Action.STATUS -> appActionNavigationTracker.navigateToTab(TargaryenTab.PROFILE)
            AppActionConstants.Action.SEARCH -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
            AppActionConstants.Action.FEATURE -> handleFeatureAction(data)
            else -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleOrderAction(
        data: Uri,
        scope: CoroutineScope,
    ) {
        val productName = data.getQueryParameter(AppActionConstants.Param.PRODUCT_NAME)
        val quantity = data.getQueryParameter(AppActionConstants.Param.QUANTITY)?.toIntOrNull() ?: 1
        if (!productName.isNullOrBlank()) {
            handleOrderActionByName(productName, quantity, scope)
        } else {
            appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
        }
    }

    private fun handleOrderActionByName(
        productName: String,
        quantity: Int,
        scope: CoroutineScope,
    ) {
        scope.launch {
            val products = catalogRepository.getProducts().first { productList -> productList.isNotEmpty() }
            val product = products.findBestMatchingProduct(productName)
            if (product != null) {
                catalogRepository.updateProductQuantity(
                    product.id,
                    product.quantityInCart + quantity.coerceAtLeast(1),
                )
            }
            appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
        }
    }

    private fun handleClearCartAction(scope: CoroutineScope) {
        scope.launch {
            catalogRepository.clearCart()
            appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleFeatureAction(data: Uri) {
        val featureName = data.getQueryParameter(AppActionConstants.Param.FEATURE_NAME)?.lowercase()
        val targetTab =
            when (featureName) {
                AppActionConstants.FeatureName.CARRINHO,
                AppActionConstants.FeatureName.CART,
                -> TargaryenTab.CART
                AppActionConstants.FeatureName.FAVORITOS,
                AppActionConstants.FeatureName.FAVORITES,
                -> TargaryenTab.FAVORITES
                AppActionConstants.FeatureName.CHAT,
                AppActionConstants.FeatureName.ASSISTENTE,
                AppActionConstants.FeatureName.RHAENYRA,
                -> TargaryenTab.CHAT
                AppActionConstants.FeatureName.PERFIL,
                AppActionConstants.FeatureName.PROFILE,
                -> TargaryenTab.PROFILE
                else -> TargaryenTab.CATALOG
            }
        appActionNavigationTracker.navigateToTab(targetTab)
    }
}
