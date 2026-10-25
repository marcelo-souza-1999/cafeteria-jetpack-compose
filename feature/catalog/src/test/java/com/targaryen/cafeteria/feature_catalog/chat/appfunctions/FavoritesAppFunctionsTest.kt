package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesAppFunctionsTest {
    private val catalogRepository: CatalogRepository = mockk(relaxed = true)
    private val appFunctionContext: AppFunctionContext = mockk(relaxed = true)
    private lateinit var favoritesAppFunctions: FavoritesAppFunctions

    private val mockProducts =
        listOf(
            Product(
                id = "1",
                name = "Café Expresso Valíria",
                description = "Expresso forte",
                price = 8.5,
                imageUrl = "",
                category = "bebidas",
                isFavorite = false,
                quantityInCart = 0,
            ),
            Product(
                id = "2",
                name = "Mocha Fogo de Dragão",
                description = "Mocha picante",
                price = 14.9,
                imageUrl = "",
                category = "bebidas",
                isFavorite = true,
                quantityInCart = 0,
            ),
        )

    @Before
    fun setUp() {
        every { catalogRepository.getProducts() } returns flowOf(mockProducts)
        favoritesAppFunctions = FavoritesAppFunctions(catalogRepository)
    }

    @Test
    fun toggleFavoriteProduct_whenProductExists_togglesFavoriteState() =
        runTest {
            val response =
                favoritesAppFunctions.toggleFavoriteProduct(
                    appFunctionContext,
                    ToggleFavoriteRequest("Café Expresso Valíria"),
                )

            assertTrue(response.success)
            assertTrue(response.isFavorite)
            assertTrue(response.message.contains("adicionado aos seus banquetes favoritos"))
            coVerify { catalogRepository.toggleFavorite("1") }
        }

    @Test
    fun toggleFavoriteProduct_whenProductNotFound_returnsErrorMessage() =
        runTest {
            val response =
                favoritesAppFunctions.toggleFavoriteProduct(
                    appFunctionContext,
                    ToggleFavoriteRequest("Inexistente"),
                )

            assertFalse(response.success)
            assertFalse(response.isFavorite)
            assertTrue(response.message.contains("Erro: Produto 'Inexistente' não encontrado"))
        }

    @Test
    fun getFavoriteProducts_whenHasFavorites_returnsListWithPrices() =
        runTest {
            val response = favoritesAppFunctions.getFavoriteProducts(appFunctionContext)

            assertEquals(1, response.favorites.size)
            assertEquals("Mocha Fogo de Dragão", response.favorites[0].name)
            assertTrue(response.message.contains("Mocha Fogo de Dragão (14.9 Dragões de Ouro)"))
        }

    @Test
    fun getFavoriteProducts_whenNoFavorites_returnsEmptyMessage() =
        runTest {
            val noFavoritesList = mockProducts.map { prod -> prod.copy(isFavorite = false) }
            every { catalogRepository.getProducts() } returns flowOf(noFavoritesList)

            val response = favoritesAppFunctions.getFavoriteProducts(appFunctionContext)

            assertEquals(0, response.favorites.size)
            assertTrue(response.message.contains("Nenhum banquete ou café marcado como favorito"))
        }
}
