package com.targaryen.cafeteria.feature_catalog.catalog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.ToggleFavoriteUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.state.CatalogUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalStdlibApi::class)
@KoinViewModel
class CatalogViewModel(
    getCatalogUseCase: GetCatalogUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: CatalogRepository
) : ViewModel() {

    val uiState: StateFlow<CatalogUiState>
        field: MutableStateFlow<CatalogUiState> = MutableStateFlow(
            CatalogUiState(selectedCategory = CatalogCategories.ALL)
        )

    private var currentAllUiProducts: List<ProductUiModel> = emptyList()

    init {
        getCatalogUseCase()
            .onEach { products ->
                val uiProducts = products.map { domainProduct ->
                    ProductUiModel(
                        id = domainProduct.id,
                        name = domainProduct.name,
                        description = domainProduct.description,
                        price = domainProduct.price,
                        category = domainProduct.category,
                        imageUrl = domainProduct.imageUrl,
                        isFavorite = domainProduct.isFavorite,
                        quantityInCart = domainProduct.quantityInCart
                    )
                }
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
                viewModelScope.launch {
                    repository.updateProductQuantity(intent.productId, intent.quantity)
                }
            }

            is CatalogIntent.AddToCart -> {
                val currentQty =
                    currentAllUiProducts.find { product -> product.id == intent.product.id }?.quantityInCart
                        ?: 0
                viewModelScope.launch {
                    repository.updateProductQuantity(intent.product.id, currentQty + 1)
                }
            }

            is CatalogIntent.RemoveFromCart -> {
                val currentQty =
                    currentAllUiProducts.find { product -> product.id == intent.product.id }?.quantityInCart
                        ?: 0
                if (currentQty > 0) {
                    viewModelScope.launch {
                        repository.updateProductQuantity(intent.product.id, currentQty - 1)
                    }
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

    private fun updateFilteredProducts() {
        val query = uiState.value.searchQuery
        val category = uiState.value.selectedCategory

        val filtered = currentAllUiProducts.filter { product ->
            val matchesCategory = category == CatalogCategories.ALL ||
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
