package com.targaryen.cafeteria.feature_catalog.catalog.presentation.viewmodel

import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.GetCatalogUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase.ToggleFavoriteUseCase
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCatalogUseCase: GetCatalogUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk()
    private val repository: CatalogRepository = mockk()

    private val mockProducts =
        listOf(
            Product(
                id = "1",
                name = "Espresso",
                description = "Strong black coffee",
                price = 5.0,
                imageUrl = "",
                category = "DRAGON_FIRE",
                isFavorite = true,
                quantityInCart = 2,
            ),
            Product(
                id = "2",
                name = "Iced Latte",
                description = "Cold milk coffee",
                price = 6.0,
                imageUrl = "",
                category = "ICE_BREATH",
                isFavorite = false,
                quantityInCart = 0,
            ),
        )

    private val catalogFlow = MutableStateFlow(mockProducts)

    @Before
    fun setup() {
        every { getCatalogUseCase() } returns catalogFlow
    }

    @Test
    fun `initialization should fetch products and update uiState`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            val state = viewModel.uiState.value

            assertEquals(2, state.products.size)
            assertEquals("Espresso", state.products[0].name)
            assertEquals(1, state.favoriteProducts.size)
            assertEquals("Espresso", state.favoriteProducts[0].name)
            assertEquals(2, state.badgeCount)
        }

    @Test
    fun `when Search intent is received, uiState should filter products`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)

            viewModel.onIntent(CatalogIntent.Search("iced"))

            val state = viewModel.uiState.value
            assertEquals("iced", state.searchQuery)
            assertEquals(1, state.products.size)
            assertEquals("Iced Latte", state.products[0].name)
        }

    @Test
    fun `when SelectCategory intent is received, uiState should filter products by category`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)

            viewModel.onIntent(CatalogIntent.SelectCategory("DRAGON_FIRE"))

            val state = viewModel.uiState.value
            assertEquals("DRAGON_FIRE", state.selectedCategory)
            assertEquals(1, state.products.size)
            assertEquals("Espresso", state.products[0].name)
        }

    @Test
    fun `when ClearSearch intent is received, searchQuery should be cleared`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)

            viewModel.onIntent(CatalogIntent.Search("iced"))
            assertEquals("iced", viewModel.uiState.value.searchQuery)

            viewModel.onIntent(CatalogIntent.ClearSearch)
            val state = viewModel.uiState.value
            assertEquals("", state.searchQuery)
            assertEquals(2, state.products.size)
        }

    @Test
    fun `when SelectProduct intent is received, selectedProduct should be updated`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            val productUi =
                ProductUiModel(
                    id = "1",
                    name = "Espresso",
                    description = "Desc",
                    price = 5.0,
                    category = "DRAGON_FIRE",
                    imageUrl = "",
                    isFavorite = true,
                    quantityInCart = 2,
                )

            assertNull(viewModel.uiState.value.selectedProduct)

            viewModel.onIntent(CatalogIntent.SelectProduct(productUi))

            assertEquals(productUi, viewModel.uiState.value.selectedProduct)
        }

    @Test
    fun `when ToggleFavorite intent is received, it should delegate to ToggleFavoriteUseCase`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            val productUi =
                ProductUiModel(
                    id = "1",
                    name = "Espresso",
                    description = "Desc",
                    price = 5.0,
                    category = "DRAGON_FIRE",
                    imageUrl = "",
                    isFavorite = true,
                    quantityInCart = 2,
                )
            coEvery { toggleFavoriteUseCase(productUi.id) } returns Unit

            viewModel.onIntent(CatalogIntent.ToggleFavorite(productUi))

            coVerify { toggleFavoriteUseCase(productUi.id) }
        }

    @Test
    fun `when UpdateProductQuantity intent is received, it should call repository`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            coEvery { repository.updateProductQuantity("1", 5) } returns Unit

            viewModel.onIntent(CatalogIntent.UpdateProductQuantity("1", 5))

            coVerify { repository.updateProductQuantity("1", 5) }
        }

    @Test
    fun `when AddToCart intent is received, it should call repository with incremented quantity`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            val productUi =
                ProductUiModel(
                    id = "1",
                    name = "Espresso",
                    description = "Desc",
                    price = 5.0,
                    category = "DRAGON_FIRE",
                    imageUrl = "",
                    isFavorite = true,
                    quantityInCart = 2,
                )
            coEvery { repository.updateProductQuantity("1", 3) } returns Unit

            viewModel.onIntent(CatalogIntent.AddToCart(productUi))

            coVerify { repository.updateProductQuantity("1", 3) }
        }

    @Test
    fun `when RemoveFromCart intent is received, it should call repository with decremented quantity`() =
        runTest {
            val viewModel = CatalogViewModel(getCatalogUseCase, toggleFavoriteUseCase, repository)
            val productUi =
                ProductUiModel(
                    id = "1",
                    name = "Espresso",
                    description = "Desc",
                    price = 5.0,
                    category = "DRAGON_FIRE",
                    imageUrl = "",
                    isFavorite = true,
                    quantityInCart = 2,
                )
            coEvery { repository.updateProductQuantity("1", 1) } returns Unit

            viewModel.onIntent(CatalogIntent.RemoveFromCart(productUi))

            coVerify { repository.updateProductQuantity("1", 1) }
        }
}
