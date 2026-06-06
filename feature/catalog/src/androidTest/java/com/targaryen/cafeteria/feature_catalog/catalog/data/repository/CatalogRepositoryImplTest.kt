package com.targaryen.cafeteria.feature_catalog.catalog.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.model.ProductEntity
import com.targaryen.cafeteria.feature_catalog.catalog.data.remote.FirestoreDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogRepositoryImplTest {

    private val localDao: ProductDao = mockk(relaxed = true)
    private val remoteDataSource: FirestoreDataSource = mockk()
    
    private lateinit var repository: CatalogRepositoryImpl

    private val mockEntities = listOf(
        ProductEntity(
            id = "1",
            name = "Espresso",
            description = "Desc",
            price = 5.0,
            imageUrl = "",
            category = "DRAGON_FIRE",
            isFavorite = true,
            quantityInCart = 2
        )
    )

    @Before
    fun setup() {
        every { remoteDataSource.streamProducts() } returns flowOf()
        repository = CatalogRepositoryImpl(localDao, remoteDataSource)
    }

    @Test
    fun getProducts_shouldFetchFromLocalDaoAndMapToDomain() = runTest {
        every { localDao.getProducts() } returns flowOf(mockEntities)

        val result = repository.getProducts().first()

        assertEquals(1, result.size)
        assertEquals("Espresso", result[0].name)
        assertTrue(result[0].isFavorite)
        assertEquals(2, result[0].quantityInCart)
        verify { localDao.getProducts() }
    }

    @Test
    fun toggleFavorite_whenProductExists_shouldInvertFavoriteStatus() = runTest {
        every { localDao.getProduct("1") } returns flowOf(mockEntities[0])
        coEvery { localDao.updateFavoriteStatus("1", false) } returns Unit

        repository.toggleFavorite("1")

        coVerify { localDao.updateFavoriteStatus("1", false) }
    }

    @Test
    fun updateProductQuantity_shouldCallLocalDao() = runTest {
        coEvery { localDao.updateCartQuantity("1", 5) } returns Unit

        repository.updateProductQuantity("1", 5)

        coVerify { localDao.updateCartQuantity("1", 5) }
    }

    @Test
    fun clearCart_shouldCallLocalDao() = runTest {
        coEvery { localDao.clearCart() } returns Unit

        repository.clearCart()

        coVerify { localDao.clearCart() }
    }
}
