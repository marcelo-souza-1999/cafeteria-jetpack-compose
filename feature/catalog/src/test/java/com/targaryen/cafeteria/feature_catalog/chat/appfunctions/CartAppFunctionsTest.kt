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

class CartAppFunctionsTest {
    private val catalogRepository: CatalogRepository = mockk(relaxed = true)
    private val appFunctionContext: AppFunctionContext = mockk(relaxed = true)
    private lateinit var cartAppFunctions: CartAppFunctions

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
                quantityInCart = 2,
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
        cartAppFunctions = CartAppFunctions(catalogRepository)
    }

    @Test
    fun addProductToCart_withValidProduct_increasesCartQuantity() =
        runTest {
            val response =
                cartAppFunctions.addProductToCart(
                    appFunctionContext,
                    AddProductRequest("Café Expresso Valíria", 1),
                )

            assertTrue(response.success)
            assertTrue(response.message.contains("Adicionado 1 de 'Café Expresso Valíria'"))
            coVerify { catalogRepository.updateProductQuantity("1", 3) }
        }

    @Test
    fun addProductToCart_withPartialName_findsAndAddsProduct() =
        runTest {
            val response =
                cartAppFunctions.addProductToCart(
                    appFunctionContext,
                    AddProductRequest("Mocha", 2),
                )

            assertTrue(response.success)
            assertTrue(response.message.contains("Adicionado 2 de 'Mocha Fogo de Dragão'"))
            coVerify { catalogRepository.updateProductQuantity("2", 2) }
        }

    @Test
    fun addProductToCart_withInvalidProduct_returnsErrorWithSuggestions() =
        runTest {
            val response =
                cartAppFunctions.addProductToCart(
                    appFunctionContext,
                    AddProductRequest("Inexistente", 1),
                )

            assertFalse(response.success)
            assertTrue(response.message.contains("Erro: Produto 'Inexistente' não encontrado"))
            assertTrue(response.message.contains("Experimente pedir"))
        }

    @Test
    fun getCartSummary_returnsCorrectTotalAndItemList() =
        runTest {
            val response = cartAppFunctions.getCartSummary(appFunctionContext)

            assertEquals(1, response.items.size)
            assertEquals("Café Expresso Valíria", response.items[0].name)
            assertEquals(2, response.items[0].quantity)
            assertEquals(17.0, response.totalPrice, 0.01)
            assertTrue(response.message.contains("Café Expresso Valíria"))
        }

    @Test
    fun clearCart_clearsRepositoryCartSuccessfully() =
        runTest {
            val response = cartAppFunctions.clearCart(appFunctionContext)

            assertTrue(response.success)
            assertTrue(response.message.contains("Carrinho imperial limpo com sucesso"))
            coVerify { catalogRepository.clearCart() }
        }

    @Test
    fun removeProductFromCart_whenCartIsEmpty_returnsEmptyCartMessage() =
        runTest {
            val emptyCartProducts = mockProducts.map { prod -> prod.copy(quantityInCart = 0) }
            every { catalogRepository.getProducts() } returns flowOf(emptyCartProducts)

            val response =
                cartAppFunctions.removeProductFromCart(
                    appFunctionContext,
                    RemoveProductRequest("Café Expresso Valíria", 1),
                )

            assertFalse(response.success)
            assertTrue(response.message.contains("Seu carrinho imperial está vazio"))
        }

    @Test
    fun removeProductFromCart_withExistingItem_decrementsQuantity() =
        runTest {
            val response =
                cartAppFunctions.removeProductFromCart(
                    appFunctionContext,
                    RemoveProductRequest("Café Expresso Valíria", 1),
                )

            assertTrue(response.success)
            assertTrue(response.message.contains("reduzida para 1"))
            coVerify { catalogRepository.updateProductQuantity("1", 1) }
        }

    @Test
    fun removeProductFromCart_withQuantityGreaterThanCart_removesItemCompletely() =
        runTest {
            val response =
                cartAppFunctions.removeProductFromCart(
                    appFunctionContext,
                    RemoveProductRequest("Café Expresso Valíria", 5),
                )

            assertTrue(response.success)
            assertTrue(response.message.contains("removido completamente do carrinho"))
            coVerify { catalogRepository.updateProductQuantity("1", 0) }
        }

    @Test
    fun removeProductFromCart_withGenericCoffeeQuery_removesCoffeePresentInCart() =
        runTest {
            val response =
                cartAppFunctions.removeProductFromCart(
                    appFunctionContext,
                    RemoveProductRequest("café", 1),
                )

            assertTrue(response.success)
            assertTrue(response.message.contains("Café Expresso Valíria"))
            coVerify { catalogRepository.updateProductQuantity("1", 1) }
        }
}
