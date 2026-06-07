package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkoutRepository: CheckoutRepository = mockk()
    private val catalogRepository: CatalogRepository = mockk()
    private val paymentStatusTracker: PaymentStatusTracker = mockk()

    private val mockCartProducts =
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

    private val paymentStatusFlow = MutableSharedFlow<PaymentStatus>(replay = 1)
    private val mockAuth: FirebaseAuth = mockk()
    private val mockUser: FirebaseUser = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_uid"

        every { catalogRepository.getProducts() } returns flowOf(mockCartProducts)
        every { paymentStatusTracker.paymentStatus } returns paymentStatusFlow

        coEvery { checkoutRepository.fetchAddressByCep(any()) } returns Result.success(mockk(relaxed = true))
    }

    @After
    fun teardown() {
        unmockkStatic(FirebaseAuth::class)
    }

    @Test
    fun `initialization should load cart items and start observing payment status`() =
        runTest {
            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)
            val state = viewModel.uiState.value

            assertEquals(1, state.cartItems.size)
            assertEquals("Espresso", state.cartItems[0].name)
        }

    @Test
    fun `when CEP changes and reaches 8 digits, it should fetch address`() =
        runTest {
            val mockAddress =
                ViaCepResponse(
                    cep = "01001-000",
                    logradouro = "Praca da Se",
                    localidade = "Sao Paulo",
                    uf = "SP",
                )
            coEvery { checkoutRepository.fetchAddressByCep("01001000") } returns Result.success(mockAddress)

            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)

            viewModel.onIntent(CheckoutIntent.OnCepChanged("01001000"))

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingAddress)
            assertEquals("Praca da Se", state.street)
            assertEquals("Sao Paulo", state.city)
            assertEquals("SP", state.state)
        }

    @Test
    fun `when submit payment is clicked with invalid fields, it should show error`() =
        runTest {
            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)

            viewModel.onIntent(CheckoutIntent.OnSubmitPayment)

            val state = viewModel.uiState.value
            assertEquals(
                R.string.error_checkout_required_fields,
                state.errorResId,
            )
        }

    @Test
    fun `when submit payment is clicked with valid fields, it should create preference`() =
        runTest {
            val mockPreference =
                MpPreferenceResponse(
                    id = "pref_123",
                    initPoint = "http://init.point",
                    sandboxInitPoint = "http://sandbox.init.point",
                )
            val preferenceResult = Result.success(mockPreference)
            coEvery { checkoutRepository.createPreferenceForCart(mockCartProducts) } returns preferenceResult

            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)

            // Pre-fill fields
            viewModel.onIntent(CheckoutIntent.OnCepChanged("12345678"))
            viewModel.onIntent(CheckoutIntent.OnNumberChanged("100"))
            viewModel.onIntent(CheckoutIntent.OnReferencePointChanged("Red Keep"))
            viewModel.onIntent(CheckoutIntent.OnRecipientNameChanged("Rhaenyra"))

            viewModel.onIntent(CheckoutIntent.OnSubmitPayment)

            val state = viewModel.uiState.value
            assertEquals("pref_123", state.preferenceId)
            assertEquals("http://init.point", state.initPoint)
            assertFalse(state.isCreatingPreference)
        }

    @Test
    fun `when payment status becomes success, it should save order and clear cart`() =
        runTest {
            val saveResult = Result.success(Unit)
            coEvery { checkoutRepository.saveOrder("test_uid", "2x Espresso", 10.0, "Aprovado") } returns saveResult
            coEvery { catalogRepository.clearCart() } returns Unit

            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)

            paymentStatusFlow.emit(PaymentStatus.SUCCESS)

            val state = viewModel.uiState.value
            assertTrue(state.showSuccessNotice)
            assertFalse(state.isRedirecting)

            coVerify { checkoutRepository.saveOrder("test_uid", "2x Espresso", 10.0, "Aprovado") }
            coVerify { catalogRepository.clearCart() }
        }

    @Test
    fun `when resetState intent is received, fields should clear`() =
        runTest {
            val viewModel = CheckoutViewModel(checkoutRepository, catalogRepository, paymentStatusTracker)

            viewModel.onIntent(CheckoutIntent.OnCepChanged("12345678"))
            assertEquals("12345678", viewModel.uiState.value.cep)

            viewModel.onIntent(CheckoutIntent.OnResetState)
            val state = viewModel.uiState.value
            assertEquals("", state.cep)
            assertFalse(state.showSuccessNotice)
        }
}
