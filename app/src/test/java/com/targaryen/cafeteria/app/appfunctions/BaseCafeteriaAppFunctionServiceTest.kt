package com.targaryen.cafeteria.app.appfunctions

import com.targaryen.cafeteria.feature.auth.appfunctions.AuthAppFunctions
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.AddProductRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CartAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CatalogAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.FavoritesAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.SearchCatalogRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.ToggleFavoriteRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseCafeteriaAppFunctionServiceTest {
    private val sampleProducts =
        listOf(
            Product(
                id = "prod_valiria",
                name = "Café Expresso Valíria",
                description = "Café encorpado de Valíria",
                price = 12.0,
                imageUrl = "",
                category = "bebidas",
                isFavorite = false,
                quantityInCart = 1,
            ),
        )

    @Test
    fun addProductToCart_validProduct_returnsSuccessResponse() =
        runTest {
            // Arrange
            val repo: CatalogRepository = mockk()
            coEvery { repo.getProducts() } returns flowOf(sampleProducts)
            coEvery { repo.updateProductQuantity("prod_valiria", 3) } returns Unit
            val cartFunctions = CartAppFunctions(repo)

            // Act
            val response =
                cartFunctions.addProductToCart(
                    AddProductRequest(productName = "Café Expresso Valíria", quantity = 2),
                )

            // Assert
            assertTrue(response.success)
            assertTrue(response.message.contains("Café Expresso Valíria"))
            coVerify(exactly = 1) { repo.updateProductQuantity("prod_valiria", 3) }
        }

    @Test
    fun getCartSummary_withItems_returnsCorrectTotals() =
        runTest {
            // Arrange
            val repo: CatalogRepository = mockk()
            coEvery { repo.getProducts() } returns flowOf(sampleProducts)
            val cartFunctions = CartAppFunctions(repo)

            // Act
            val response = cartFunctions.getCartSummary()

            // Assert
            assertEquals(1, response.items.size)
            assertEquals(12.0, response.totalPrice, 0.01)
            assertTrue(response.message.contains("Café Expresso Valíria"))
        }

    @Test
    fun clearCart_invokesRepository_returnsSuccess() =
        runTest {
            // Arrange
            val repo: CatalogRepository = mockk()
            coEvery { repo.clearCart() } returns Unit
            val cartFunctions = CartAppFunctions(repo)

            // Act
            val response = cartFunctions.clearCart()

            // Assert
            assertTrue(response.success)
            coVerify(exactly = 1) { repo.clearCart() }
        }

    @Test
    fun searchCatalog_matchingQuery_returnsProducts() =
        runTest {
            // Arrange
            val repo: CatalogRepository = mockk()
            coEvery { repo.getProducts() } returns flowOf(sampleProducts)
            val catalogFunctions = CatalogAppFunctions(repo)

            // Act
            val response = catalogFunctions.searchCatalog(SearchCatalogRequest(query = "Valíria"))

            // Assert
            assertEquals(1, response.products.size)
            assertEquals("Café Expresso Valíria", response.products.first().name)
        }

    @Test
    fun toggleFavorite_existingProduct_flipsFavoriteState() =
        runTest {
            // Arrange
            val repo: CatalogRepository = mockk()
            coEvery { repo.getProducts() } returns flowOf(sampleProducts)
            coEvery { repo.toggleFavorite("prod_valiria") } returns Unit
            val favoritesFunctions = FavoritesAppFunctions(repo)

            // Act
            val response =
                favoritesFunctions.toggleFavoriteProduct(
                    ToggleFavoriteRequest(productName = "Café Expresso Valíria"),
                )

            // Assert
            assertTrue(response.success)
            assertTrue(response.isFavorite)
            coVerify(exactly = 1) { repo.toggleFavorite("prod_valiria") }
        }

    @Test
    fun checkAuthSession_loggedInUser_returnsTrueWithUid() {
        // Arrange
        val authRepo: AuthRepository = mockk()
        coEvery { authRepo.isUserLoggedIn() } returns true
        coEvery { authRepo.getCurrentUserUid() } returns "user_imperial_123"
        val authFunctions = AuthAppFunctions(authRepo)

        // Act
        val response = authFunctions.checkAuthSession()

        // Assert
        assertTrue(response.isLoggedIn)
        assertEquals("user_imperial_123", response.userUid)
    }

    @Test
    fun checkAuthSession_loggedOutUser_returnsFalseWithNullUid() {
        // Arrange
        val authRepo: AuthRepository = mockk()
        coEvery { authRepo.isUserLoggedIn() } returns false
        coEvery { authRepo.getCurrentUserUid() } returns null
        val authFunctions = AuthAppFunctions(authRepo)

        // Act
        val response = authFunctions.checkAuthSession()

        // Assert
        assertFalse(response.isLoggedIn)
        assertEquals(null, response.userUid)
    }
}
