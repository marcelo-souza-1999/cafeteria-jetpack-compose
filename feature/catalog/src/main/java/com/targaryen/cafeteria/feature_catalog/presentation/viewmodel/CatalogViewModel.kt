package com.targaryen.cafeteria.feature_catalog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_designsystem.model.CatalogConstants
import com.targaryen.cafeteria.feature_catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.domain.usecase.ToggleFavoriteUseCase
import com.targaryen.cafeteria.feature_catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.presentation.state.CatalogUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalStdlibApi::class)
@KoinViewModel
class CatalogViewModel(
    getCatalogUseCase: GetCatalogUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val uiState: StateFlow<CatalogUiState>
        field: MutableStateFlow<CatalogUiState> = MutableStateFlow(
            CatalogUiState(selectedCategory = CatalogConstants.CATEGORY_ALL)
        )

    private val cartQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    private var currentAllUiProducts: List<ProductUiModel> = emptyList()

    init {
        getCatalogUseCase()
            .combine(cartQuantities) { products, cartMap ->
                products.map { domainProduct ->
                    ProductUiModel(
                        id = domainProduct.id,
                        name = domainProduct.name,
                        description = domainProduct.description,
                        price = domainProduct.price,
                        category = domainProduct.category,
                        imageUrl = domainProduct.imageUrl,
                        isFavorite = domainProduct.isFavorite,
                        quantityInCart = cartMap[domainProduct.id] ?: 0
                    )
                }
            }
            .onEach { uiProducts ->
                currentAllUiProducts = uiProducts
                updateFilteredProducts()
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: CatalogIntent) {
        when (intent) {
            is CatalogIntent.Search -> {
                uiState.update { state -> state.copy(searchQuery = intent.query) }
                updateFilteredProducts()
            }
            is CatalogIntent.SelectCategory -> {
                uiState.update { state -> state.copy(selectedCategory = intent.category) }
                updateFilteredProducts()
            }
            is CatalogIntent.SelectProduct -> {
                uiState.update { state -> state.copy(selectedProduct = intent.product) }
            }
            is CatalogIntent.UpdateProductQuantity -> {
                updateQuantity(intent.productId, intent.quantity)
            }
            is CatalogIntent.AddToCart -> {
                val currentQty = cartQuantities.value[intent.product.id] ?: 0
                updateQuantity(intent.product.id, currentQty + 1)
            }
            is CatalogIntent.RemoveFromCart -> {
                val currentQty = cartQuantities.value[intent.product.id] ?: 0
                if (currentQty > 0) {
                    updateQuantity(intent.product.id, currentQty - 1)
                }
            }
            is CatalogIntent.ClearSearch -> {
                uiState.update { state -> state.copy(searchQuery = "") }
                updateFilteredProducts()
            }
            is CatalogIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    toggleFavoriteUseCase(intent.product.id)
                }
            }
        }
    }

    private fun updateQuantity(productId: String, quantity: Int) {
        cartQuantities.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            newMap[productId] = quantity
            newMap
        }
    }

    private fun updateFilteredProducts() {
        val query = uiState.value.searchQuery
        val category = uiState.value.selectedCategory

        val filtered = currentAllUiProducts.filter { product ->
            val matchesCategory = category == CatalogConstants.CATEGORY_ALL ||
                product.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val totalBadgeCount = currentAllUiProducts.sumOf { it.quantityInCart }
        val favorites = currentAllUiProducts.filter { it.isFavorite }
        
        val updatedSelected = uiState.value.selectedProduct?.let { sel ->
            currentAllUiProducts.find { it.id == sel.id } ?: sel
        }

        uiState.update { state ->
            state.copy(
                products = filtered,
                favoriteProducts = favorites,
                badgeCount = totalBadgeCount,
                selectedProduct = updatedSelected
            )
        }
    }
}
