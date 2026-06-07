package com.targaryen.cafeteria.feature_checkout.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.core_network.remote.MercadoPagoDataSource
import com.targaryen.cafeteria.core_network.remote.ViaCepDataSource
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutRepositoryImplTest {
    private val viaCepDataSource: ViaCepDataSource = mockk()
    private val mercadoPagoDataSource: MercadoPagoDataSource = mockk()
    private val firestore: FirebaseFirestore = mockk(relaxed = true)

    private lateinit var repository: CheckoutRepositoryImpl

    private val mockProducts =
        listOf(
            Product(
                id = "1",
                name = "Espresso",
                description = "Desc",
                price = 5.0,
                imageUrl = "",
                category = "DRAGON_FIRE",
                isFavorite = false,
                quantityInCart = 2,
            ),
        )

    @Before
    fun setup() {
        repository =
            CheckoutRepositoryImpl(
                viaCepDataSource = viaCepDataSource,
                mercadoPagoDataSource = mercadoPagoDataSource,
                firestore = firestore,
            )
    }

    @Test
    fun fetchAddressByCep_shouldDelegateToViaCepDataSource() =
        runTest {
            val mockResponse = ViaCepResponse(cep = "01001-000", logradouro = "Praca da Se")
            coEvery { viaCepDataSource.fetchAddressByCep("01001000") } returns Result.success(mockResponse)

            val result = repository.fetchAddressByCep("01001000")

            assertTrue(result.isSuccess)
            assertEquals(mockResponse, result.getOrNull())
            coVerify { viaCepDataSource.fetchAddressByCep("01001000") }
        }

    @Test
    fun createPreferenceForCart_whenCartNotEmpty_shouldDelegateToMercadoPagoDataSource() =
        runTest {
            val mockResponse = MpPreferenceResponse(id = "pref_123")
            coEvery { mercadoPagoDataSource.createPreference(any()) } returns Result.success(mockResponse)

            val result = repository.createPreferenceForCart(mockProducts)

            assertTrue(result.isSuccess)
            assertEquals(mockResponse, result.getOrNull())
            coVerify { mercadoPagoDataSource.createPreference(any()) }
        }

    @Test
    fun saveOrder_shouldInsertIntoFirestoreOrdersCollection() =
        runTest {
            val collectionRef: CollectionReference = mockk()
            val docRef: DocumentReference = mockk()
            every { firestore.collection("orders") } returns collectionRef
            every { collectionRef.add(any()) } returns Tasks.forResult(docRef)

            val result =
                repository.saveOrder(
                    userId = "user_123",
                    itemsSummary = "2x Espresso",
                    totalPrice = 10.0,
                    status = "Aprovado",
                )

            assertTrue(result.isSuccess)
            verify { firestore.collection("orders") }
        }
}
