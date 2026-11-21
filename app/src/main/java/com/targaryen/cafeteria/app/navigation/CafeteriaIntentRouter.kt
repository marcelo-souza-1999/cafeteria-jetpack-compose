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
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.isCoffeeBeverage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private val FEATURE_TAB_RULES: List<Pair<List<String>, TargaryenTab>> =
    listOf(
        listOf(AppActionConstants.FeatureName.CARRINHO, AppActionConstants.FeatureName.CART) to TargaryenTab.CART,
        listOf(AppActionConstants.FeatureName.FAVORITOS, AppActionConstants.FeatureName.FAVORITES) to
            TargaryenTab.FAVORITES,
        listOf(
            AppActionConstants.FeatureName.CHAT,
            AppActionConstants.FeatureName.ASSISTENTE,
            AppActionConstants.FeatureName.RHAENYRA,
        ) to TargaryenTab.CHAT,
        listOf(AppActionConstants.FeatureName.PERFIL, AppActionConstants.FeatureName.PROFILE) to TargaryenTab.PROFILE,
    )

private fun resolveTabByKeyword(rawText: String?): TargaryenTab? {
    val normalized = rawText?.trim()?.lowercase() ?: return null
    return FEATURE_TAB_RULES
        .firstOrNull { rule -> rule.first.any { keyword -> normalized.contains(keyword) } }
        ?.second
}

private fun decodeParam(rawValue: String): String =
    runCatching {
        URLDecoder.decode(rawValue, StandardCharsets.UTF_8.name())
    }.getOrDefault(rawValue)

private fun forwardChatQuery(
    tracker: AppActionNavigationTracker,
    rawQuery: String,
) {
    if (rawQuery.isNotBlank()) {
        tracker.requestChatMessage(decodeParam(rawQuery).trim())
    }
    tracker.navigateToTab(TargaryenTab.CHAT)
}

private fun extractSearchQuery(
    data: Uri,
    intent: Intent,
): String =
    data.getQueryParameter(AppActionConstants.Param.QUERY)
        ?: intent.getStringExtra(AppActionConstants.Extra.QUERY)
        ?: intent.getStringExtra(SearchManager.QUERY).orEmpty()

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
                routeQueryOrCatalog(intent.getStringExtra(SearchManager.QUERY).orEmpty(), scope)
            }
            !intent.getStringExtra(AppActionConstants.Extra.ACTION).isNullOrBlank() -> {
                handleExtraAction(intent, scope)
            }
            intent.data?.scheme == AppActionConstants.SCHEME -> {
                intent.data?.let { uri -> handleUriIntent(intent, uri, scope) }
            }
            !intent.getStringExtra(AppActionConstants.Extra.FEATURE).isNullOrBlank() ||
                !intent.getStringExtra(AppActionConstants.Extra.FEATURE_LEGACY).isNullOrBlank() -> {
                handleFeatureAction(intent, intent.data)
            }
        }
    }

    private fun routeQueryOrCatalog(
        rawQuery: String,
        @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
    ) {
        if (rawQuery.isBlank()) {
            appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
            return
        }
        val decodedQuery = decodeParam(rawQuery).trim()
        val matchedTab = resolveTabByKeyword(decodedQuery)
        if (matchedTab != null) {
            if (matchedTab == TargaryenTab.PROFILE) {
                appActionNavigationTracker.requestOpenLatestOrder()
            }
            appActionNavigationTracker.navigateToTab(matchedTab)
        } else {
            appActionNavigationTracker.requestCatalogSearch(decodedQuery)
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
                val productName = intent.getStringExtra(AppActionConstants.Extra.PRODUCT_NAME).orEmpty()
                val quantity = intent.getStringExtra(AppActionConstants.Extra.QUANTITY)?.toIntOrNull() ?: 1
                handleOrderActionByName(productName, quantity, scope)
            }
            AppActionConstants.Action.CLEAR_CART -> handleClearCartAction(scope)
            AppActionConstants.Action.CART_SUMMARY,
            AppActionConstants.Action.CART,
            -> appActionNavigationTracker.navigateToTab(TargaryenTab.CART)
            AppActionConstants.Action.FAVORITES -> appActionNavigationTracker.navigateToTab(TargaryenTab.FAVORITES)
            AppActionConstants.Action.CHAT ->
                forwardChatQuery(
                    appActionNavigationTracker,
                    intent.getStringExtra(AppActionConstants.Extra.QUERY).orEmpty(),
                )
            AppActionConstants.Action.STATUS -> {
                appActionNavigationTracker.requestOpenLatestOrder()
                appActionNavigationTracker.navigateToTab(TargaryenTab.PROFILE)
            }
            AppActionConstants.Action.SEARCH -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
            else -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleUriIntent(
        intent: Intent,
        data: Uri,
        scope: CoroutineScope,
    ) {
        when (data.host) {
            AppActionConstants.Host.CHECKOUT -> handleCheckoutIntent(data)
            AppActionConstants.Host.ACTION -> handleActionIntent(intent, data, scope)
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
        intent: Intent,
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
            AppActionConstants.Action.CHAT ->
                forwardChatQuery(
                    appActionNavigationTracker,
                    data.getQueryParameter(AppActionConstants.Param.QUERY)
                        ?: intent.getStringExtra(AppActionConstants.Extra.QUERY).orEmpty(),
                )
            AppActionConstants.Action.STATUS -> {
                appActionNavigationTracker.requestOpenLatestOrder()
                appActionNavigationTracker.navigateToTab(TargaryenTab.PROFILE)
            }
            AppActionConstants.Action.SEARCH -> routeQueryOrCatalog(extractSearchQuery(data, intent), scope)
            AppActionConstants.Action.FEATURE -> handleFeatureAction(intent, data)
            else -> appActionNavigationTracker.navigateToTab(TargaryenTab.CATALOG)
        }
    }

    private fun handleOrderAction(
        data: Uri,
        scope: CoroutineScope,
    ) {
        val productName = data.getQueryParameter(AppActionConstants.Param.PRODUCT_NAME).orEmpty()
        val quantity = data.getQueryParameter(AppActionConstants.Param.QUANTITY)?.toIntOrNull() ?: 1
        handleOrderActionByName(productName, quantity, scope)
    }

    private fun handleOrderActionByName(
        productName: String,
        quantity: Int,
        scope: CoroutineScope,
    ) {
        scope.launch {
            val decodedName =
                runCatching {
                    URLDecoder.decode(productName, StandardCharsets.UTF_8.name())
                }.getOrDefault(productName)
            val products = catalogRepository.getProducts().first { productList -> productList.isNotEmpty() }
            val product =
                products.findBestMatchingProduct(decodedName)
                    ?: products.find { item -> item.isCoffeeBeverage() }
                    ?: products.firstOrNull()
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

    private fun handleFeatureAction(
        intent: Intent,
        data: Uri?,
    ) {
        val rawFeature =
            data?.getQueryParameter(AppActionConstants.Param.FEATURE)
                ?: data?.getQueryParameter(AppActionConstants.Param.FEATURE_LEGACY)
                ?: intent.getStringExtra(AppActionConstants.Extra.FEATURE)
                ?: intent.getStringExtra(AppActionConstants.Extra.FEATURE_LEGACY)
        val targetTab = resolveTabByKeyword(rawFeature) ?: TargaryenTab.CATALOG
        appActionNavigationTracker.navigateToTab(targetTab)
    }
}
