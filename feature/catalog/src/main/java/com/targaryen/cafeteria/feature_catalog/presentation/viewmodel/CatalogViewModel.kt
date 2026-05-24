package com.targaryen.cafeteria.feature_catalog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.targaryen.cafeteria.core_designsystem.model.CatalogConstants
import com.targaryen.cafeteria.feature_catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.presentation.state.CatalogUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalStdlibApi::class)
@KoinViewModel
class CatalogViewModel : ViewModel() {

    val uiState: StateFlow<CatalogUiState>
        field: MutableStateFlow<CatalogUiState> = MutableStateFlow(
            CatalogUiState(selectedCategory = CatalogConstants.CATEGORY_ALL)
        )

    private val masterProducts: MutableList<ProductUiModel> = mutableListOf(
        ProductUiModel(
            id = "1",
            name = "Targaryen Blood Blend",
            description = "Café extraído sob o fogo do dragão, encorpado e com notas " +
                "intensas de especiarias e frutas vermelhas da antiga Valíria.",
            price = 12.5,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE
        ),
        ProductUiModel(
            id = "2",
            name = "Valyrian Velvet Latte",
            description = "Uma combinação sedosa e mística de café espresso robusto, " +
                "leite vaporizado cremoso e um toque sutil de cacau sagrado.",
            price = 15.0,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE
        ),
        ProductUiModel(
            id = "3",
            name = "Dragonstone Brew",
            description = "Cold brew maturado em rochas vulcânicas de Dragonstone, " +
                "extremamente refrescante, infundido com notas cítricas de laranja e menta.",
            price = 10.0,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE
        ),
        ProductUiModel(
            id = "4",
            name = "Winterfell Frost",
            description = "Café gelado batido com menta selvagem colhida além da " +
                "Muralha e um xarope doce artesanal das terras do Norte.",
            price = 11.0,
            category = CatalogConstants.CATEGORY_ICE_BREATH
        ),
        ProductUiModel(
            id = "5",
            name = "Banquete de Aegon",
            description = "Uma seleção rústica e farta de pães artesanais de Westeros " +
                "servidos quentes com geleia de frutas silvestres e manteiga trufada.",
            price = 35.0,
            category = CatalogConstants.CATEGORY_ROYAL_FEAST
        ),
        ProductUiModel(
            id = "6",
            name = "Torta de Limão de Sansa",
            description = "Fatia generosa de torta de limão siciliano selvagem, com " +
                "massa folhada e merengue dourado perfeitamente tostado.",
            price = 18.0,
            category = CatalogConstants.CATEGORY_ROYAL_FEAST
        ),
        ProductUiModel(
            id = "7",
            name = "Lágrimas de Lys",
            description = "Elixir doce e perigoso com infusão de flores raras das ilhas " +
                "de Lys, servido gelado para os que ousam desafiar o destino.",
            price = 22.0,
            category = CatalogConstants.CATEGORY_CROWN_ELIXIRS
        )
    )

    init {
        updateFilteredProducts()
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
                val currentQty = intent.product.quantityInCart
                updateQuantity(intent.product.id, currentQty + 1)
            }
            is CatalogIntent.RemoveFromCart -> {
                val currentQty = intent.product.quantityInCart
                if (currentQty > 0) {
                    updateQuantity(intent.product.id, currentQty - 1)
                }
            }
            is CatalogIntent.ClearSearch -> {
                uiState.update { state -> state.copy(searchQuery = "") }
                updateFilteredProducts()
            }
            is CatalogIntent.ToggleFavorite -> {
                toggleFavorite(intent.product.id)
            }
        }
    }

    private fun updateQuantity(productId: String, quantity: Int) {
        val index = masterProducts.indexOfFirst { product -> product.id == productId }
        if (index != -1) {
            val updatedProduct = masterProducts[index].copy(quantityInCart = quantity)
            masterProducts[index] = updatedProduct

            // Se o produto alterado for o selecionado atualmente no BottomSheet, atualize-o também no estado
            val currentSelected = uiState.value.selectedProduct
            val updatedSelected = if (currentSelected != null && currentSelected.id == productId) {
                updatedProduct
            } else {
                currentSelected
            }

            uiState.update { state ->
                state.copy(selectedProduct = updatedSelected)
            }
            updateFilteredProducts()
        }
    }

    private fun toggleFavorite(productId: String) {
        val index = masterProducts.indexOfFirst { product -> product.id == productId }
        if (index != -1) {
            val updatedProduct = masterProducts[index].copy(isFavorite = !masterProducts[index].isFavorite)
            masterProducts[index] = updatedProduct

            val currentSelected = uiState.value.selectedProduct
            val updatedSelected = if (currentSelected != null && currentSelected.id == productId) {
                updatedProduct
            } else {
                currentSelected
            }

            uiState.update { state ->
                state.copy(selectedProduct = updatedSelected)
            }
            updateFilteredProducts()
        }
    }

    private fun updateFilteredProducts() {
        val query = uiState.value.searchQuery
        val category = uiState.value.selectedCategory

        val filtered = masterProducts.filter { product ->
            val matchesCategory = category == CatalogConstants.CATEGORY_ALL ||
                product.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val totalBadgeCount = masterProducts.sumOf { product -> product.quantityInCart }
        val favorites = masterProducts.filter { product -> product.isFavorite }

        uiState.update { state ->
            state.copy(
                products = filtered,
                favoriteProducts = favorites,
                badgeCount = totalBadgeCount
            )
        }
    }
}
