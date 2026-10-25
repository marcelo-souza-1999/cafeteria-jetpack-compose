package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatalogAppFunctionsTest {
    private val catalogRepository: CatalogRepository = mockk(relaxed = true)
    private val appFunctionContext: AppFunctionContext = mockk(relaxed = true)
    private lateinit var catalogAppFunctions: CatalogAppFunctions

    private val mockProducts =
        listOf(
            Product(
                id = "1",
                name = "Café Expresso Valíria",
                description = "Expresso forte e aromático",
                price = 8.5,
                imageUrl = "",
                category = "bebidas",
                isFavorite = false,
                quantityInCart = 0,
            ),
            Product(
                id = "2",
                name = "Mocha Fogo de Dragão",
                description = "Café com chocolate e pimenta",
                price = 14.9,
                imageUrl = "",
                category = "bebidas",
                isFavorite = true,
                quantityInCart = 1,
            ),
            Product(
                id = "3",
                name = "Torta de Limão de Sansa",
                description = "Doce clássico de Winterfell",
                price = 12.0,
                imageUrl = "",
                category = "comidas",
                isFavorite = false,
                quantityInCart = 0,
            ),
        )

    @Before
    fun setUp() {
        every { catalogRepository.getProducts() } returns flowOf(mockProducts)
        catalogAppFunctions = CatalogAppFunctions(catalogRepository)
    }

    @Test
    fun normalizeForSearch_removesAccentsAndLowercases() {
        assertEquals("cafe expresso valiria", "Café Expresso Valíria".normalizeForSearch())
        assertEquals("fogo de dragao", "Fogo de Dragão".normalizeForSearch())
    }

    @Test
    fun findBestMatchingProduct_findsExactOrPartialMatch() {
        val exact = mockProducts.findBestMatchingProduct("Café Expresso Valíria")
        assertNotNull(exact)
        assertEquals("1", exact?.id)

        val unaccented = mockProducts.findBestMatchingProduct("cafe expresso valiria")
        assertNotNull(unaccented)
        assertEquals("1", unaccented?.id)

        val partial = mockProducts.findBestMatchingProduct("Mocha")
        assertNotNull(partial)
        assertEquals("2", partial?.id)

        val notFound = mockProducts.findBestMatchingProduct("Pizza")
        assertNull(notFound)
    }

    @Test
    fun isCoffeeBeverage_identifiesCoffeeProductsCorrectly() {
        assertTrue(mockProducts[0].isCoffeeBeverage())
        assertTrue(mockProducts[1].isCoffeeBeverage())
        assertFalse(mockProducts[2].isCoffeeBeverage())
    }

    @Test
    fun searchCatalog_withExactName_returnsMatchedProduct() =
        runTest {
            val response =
                catalogAppFunctions.searchCatalog(
                    appFunctionContext,
                    SearchCatalogRequest("Café Expresso Valíria"),
                )

            assertEquals(1, response.products.size)
            assertEquals("Café Expresso Valíria", response.products[0].name)
            assertTrue(response.message.contains("Encontrado(s) 1 produto(s)"))
        }

    @Test
    fun searchCatalog_withAccentInsensitiveQuery_returnsMatchedProduct() =
        runTest {
            val response =
                catalogAppFunctions.searchCatalog(
                    appFunctionContext,
                    SearchCatalogRequest("valiria"),
                )

            assertEquals(1, response.products.size)
            assertEquals("Café Expresso Valíria", response.products[0].name)
        }

    @Test
    fun searchCatalog_withGenericCoffeeQuery_returnsAllCoffeeBeverages() =
        runTest {
            val response =
                catalogAppFunctions.searchCatalog(
                    appFunctionContext,
                    SearchCatalogRequest("cafes"),
                )

            assertEquals(2, response.products.size)
            assertTrue(response.products.any { prod -> prod.name == "Café Expresso Valíria" })
            assertTrue(response.products.any { prod -> prod.name == "Mocha Fogo de Dragão" })
        }

    @Test
    fun searchCatalog_withUnknownQuery_returnsZeroAndSuggestsAvailableCoffees() =
        runTest {
            val response =
                catalogAppFunctions.searchCatalog(
                    appFunctionContext,
                    SearchCatalogRequest("hamburguer"),
                )

            assertEquals(0, response.products.size)
            assertTrue(response.message.contains("Nenhum item exato"))
            assertTrue(response.message.contains("Sugestões imperiais disponíveis"))
            assertTrue(response.message.contains("Café Expresso Valíria"))
        }
}
