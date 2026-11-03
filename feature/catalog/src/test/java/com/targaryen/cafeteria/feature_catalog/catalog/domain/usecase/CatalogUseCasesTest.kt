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
    fun getCatalogUseCase_invoke_delegatesToRepositoryGetProducts() =
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
    fun toggleFavoriteUseCase_invoke_delegatesToRepositoryToggleFavorite() =
        runTest {
            val productId = "1"
            coEvery { repository.toggleFavorite(productId) } returns Unit

            ToggleFavoriteUseCase(repository)(productId)

            coVerify { repository.toggleFavorite(productId) }
        }
}
