package com.targaryen.cafeteria.feature_catalog.catalog.domain.usecase

import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CatalogUseCasesTest {
    private val repository: CatalogRepository = mockk()

    @Test
    fun `GetCatalogUseCase should delegate to repository getProducts`() =
        runTest {
            val mockProducts =
                listOf(
                    Product(
                        id = "1",
                        name = "Espresso",
                        description = "Strong black coffee",
                        price = 5.0,
                        imageUrl = "",
                        category = "DRAGON_FIRE",
                        isFavorite = false,
                        quantityInCart = 0,
                    ),
                )
            every { repository.getProducts() } returns flowOf(mockProducts)

            GetCatalogUseCase(repository)().collect()

            verify { repository.getProducts() }
        }

    @Test
    fun `ToggleFavoriteUseCase should delegate to repository toggleFavorite`() =
        runTest {
            val productId = "1"
            coEvery { repository.toggleFavorite(productId) } returns Unit

            ToggleFavoriteUseCase(repository)(productId)

            coVerify { repository.toggleFavorite(productId) }
        }
}
