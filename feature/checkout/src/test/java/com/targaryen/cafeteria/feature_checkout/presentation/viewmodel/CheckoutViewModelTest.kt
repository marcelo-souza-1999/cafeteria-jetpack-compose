package com.targaryen.cafeteria.feature_checkout.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.targaryen.cafeteria.core_network.auth.UserSessionProvider
import com.targaryen.cafeteria.core_network.model.MpPreferenceResponse
import com.targaryen.cafeteria.core_network.model.ViaCepResponse
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import com.targaryen.cafeteria.coredatabase.address.AddressProvider
import com.targaryen.cafeteria.coredatabase.address.DeliveryAddress
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.domain.CheckoutRepository
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.telemetry.CheckoutRuntimeDependencies
import com.targaryen.cafeteria.feature_checkout.telemetry.CheckoutTelemetry
import com.targaryen.cafeteria.feature_checkout.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
    private val addressProvider: AddressProvider = mockk()
    private val userSessionProvider: UserSessionProvider = mockk()
    private val checkoutTelemetry: CheckoutTelemetry = mockk(relaxed = true)
    private lateinit var runtimeDependencies: CheckoutRuntimeDependencies

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

    @Before
    fun setup() {
        every { userSessionProvider.getCurrentUserId() } returns "test_uid"
        every { userSessionProvider.isAuthenticated() } returns true
        runtimeDependencies = CheckoutRuntimeDependencies(userSessionProvider, checkoutTelemetry)

        every { catalogRepository.getProducts() } returns flowOf(mockCartProducts)
        every { paymentStatusTracker.paymentStatus } returns paymentStatusFlow
        every { addressProvider.getAddress() } returns DeliveryAddress()
        justRun { addressProvider.clearAddress() }

        coEvery { checkoutRepository.fetchAddressByCep(any()) } returns
            Result.success(
                ViaCepResponse(
                    cep = "12345-678",
                    logradouro = "Dragonstone Way",
                    localidade = "Westeros",
                    uf = "WS",
                ),
            )
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): CheckoutViewModel =
        CheckoutViewModel(
            checkoutRepository = checkoutRepository,
            catalogRepository = catalogRepository,
            paymentStatusTracker = paymentStatusTracker,
            addressProvider = addressProvider,
            runtimeDependencies = runtimeDependencies,
            savedStateHandle = savedStateHandle,
        )

    @Test
    fun init_loadsCartItemsAndObservesPaymentStatus() =
        runTest {
            val viewModel = createViewModel()
            val state = viewModel.uiState.value

            assertEquals(1, state.cartItems.size)
            assertEquals("Espresso", state.cartItems[0].name)
        }

    @Test
    fun onIntent_onCepChanged_whenReaches8Digits_fetchesAddressAndTracksTelemetry() =
        runTest {
            val mockAddress =
                ViaCepResponse(
                    cep = "01001-000",
                    logradouro = "Praca da Se",
                    localidade = "Sao Paulo",
                    uf = "SP",
                )
            coEvery { checkoutRepository.fetchAddressByCep("01001000") } returns Result.success(mockAddress)
            val savedState = SavedStateHandle()

            val viewModel = createViewModel(savedState)

            viewModel.onIntent(CheckoutIntent.OnCepChanged("01001000"))

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingAddress)
            assertEquals("Praca da Se", state.street)
            assertEquals("Sao Paulo", state.city)
            assertEquals("SP", state.state)
            assertEquals("01001000", savedState.get<String>(CheckoutSavedStateStore.KEY_CEP))
            verify(exactly = 1) { checkoutTelemetry.trackCepLookup(success = true, isReverseSearch = false) }
        }

    @Test
    fun onIntent_onSubmitPayment_withInvalidFields_showsError() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onIntent(CheckoutIntent.OnSubmitPayment)

            val state = viewModel.uiState.value
            assertEquals(
                R.string.error_checkout_required_fields,
                state.errorResId,
            )
        }

    @Test
    fun onIntent_onSubmitPayment_withValidFields_createsPreference() =
        runTest {
            val mockPreference =
                MpPreferenceResponse(
                    id = "pref_123",
                    initPoint = "http://init.point",
                    sandboxInitPoint = "http://sandbox.init.point",
                )
            coEvery { checkoutRepository.createPreferenceForCart(mockCartProducts) } returns
                Result.success(mockPreference)

            val viewModel = createViewModel()

            viewModel.onIntent(CheckoutIntent.OnCepChanged("12345678"))
            viewModel.onIntent(CheckoutIntent.OnNumberChanged("100"))
            viewModel.onIntent(CheckoutIntent.OnReferencePointChanged("Red Keep"))
            viewModel.onIntent(CheckoutIntent.OnRecipientNameChanged("Rhaenyra"))

            viewModel.onIntent(CheckoutIntent.OnSubmitPayment)

            val state = viewModel.uiState.value
            assertEquals("pref_123", state.preferenceId)
            assertEquals("http://init.point", state.initPoint)
            assertFalse(state.isCreatingPreference)
            verify(exactly = 1) {
                checkoutTelemetry.trackPaymentPreferenceCreated(success = true, itemCount = 1)
            }
        }

    @Test
    fun paymentStatus_whenSuccess_savesOrderAndClearsCart() =
        runTest {
            coEvery { checkoutRepository.saveOrder("test_uid", "2x Espresso", 10.0, "Aprovado") } returns
                Result.success(Unit)
            coEvery { catalogRepository.clearCart() } returns Unit

            val viewModel = createViewModel()

            paymentStatusFlow.emit(PaymentStatus.SUCCESS)

            val state = viewModel.uiState.value
            assertTrue(state.showSuccessNotice)
            assertFalse(state.isRedirecting)

            coVerify { checkoutRepository.saveOrder("test_uid", "2x Espresso", 10.0, "Aprovado") }
            coVerify { catalogRepository.clearCart() }
            verify(exactly = 1) { checkoutTelemetry.trackPaymentResult(PaymentStatus.SUCCESS) }
        }

    @Test
    fun init_withRestoredSavedStateHandle_survivesProcessDeathAndFirstReset() =
        runTest {
            val restoredHandle =
                SavedStateHandle(
                    mapOf(
                        CheckoutSavedStateStore.KEY_SAVED_MARKER to true,
                        CheckoutSavedStateStore.KEY_CEP to "01001000",
                        CheckoutSavedStateStore.KEY_STREET to "Praça da Sé",
                        CheckoutSavedStateStore.KEY_CITY to "São Paulo",
                        CheckoutSavedStateStore.KEY_STATE to "SP",
                        CheckoutSavedStateStore.KEY_NUMBER to "42",
                        CheckoutSavedStateStore.KEY_REFERENCE_POINT to "Torre Sul",
                        CheckoutSavedStateStore.KEY_RECIPIENT_NAME to "Daemon",
                        CheckoutSavedStateStore.KEY_IS_REDIRECTING to true,
                        CheckoutSavedStateStore.KEY_IS_PAYMENT_COMPLETED to false,
                    ),
                )

            val viewModel = createViewModel(restoredHandle)

            // Simula LaunchedEffect(Unit) da CheckoutScreen após recriação do processo
            viewModel.onIntent(CheckoutIntent.OnResetState)

            val state = viewModel.uiState.value
            assertEquals("01001000", state.cep)
            assertEquals("Praça da Sé", state.street)
            assertEquals("42", state.number)
            assertEquals("Torre Sul", state.referencePoint)
            assertEquals("Daemon", state.recipientName)
            assertTrue(state.isRedirecting)
            verify(exactly = 1) { checkoutTelemetry.trackProcessDeathRestored() }
        }

    @Test
    fun onIntent_onResetState_clearsFieldsAndSavedState() =
        runTest {
            val savedState = SavedStateHandle()
            val viewModel = createViewModel(savedState)

            viewModel.onIntent(CheckoutIntent.OnCepChanged("12345678"))
            assertEquals("12345678", viewModel.uiState.value.cep)

            viewModel.onIntent(CheckoutIntent.OnResetState)
            val state = viewModel.uiState.value
            assertEquals("", state.cep)
            assertFalse(state.showSuccessNotice)
            assertFalse(savedState.contains(CheckoutSavedStateStore.KEY_SAVED_MARKER))
        }

    @Test
    fun onFailure_whenPreferenceFails_tracksNonFatalError() =
        runTest {
            val error = IllegalStateException("network failure")
            coEvery { checkoutRepository.createPreferenceForCart(mockCartProducts) } returns Result.failure(error)

            val viewModel = createViewModel()
            viewModel.onIntent(CheckoutIntent.OnCepChanged("12345678"))
            viewModel.onIntent(CheckoutIntent.OnNumberChanged("100"))
            viewModel.onIntent(CheckoutIntent.OnReferencePointChanged("Red Keep"))
            viewModel.onIntent(CheckoutIntent.OnSubmitPayment)

            verify(exactly = 1) {
                checkoutTelemetry.trackPaymentPreferenceCreated(success = false, itemCount = 1)
            }
            verify(exactly = 1) {
                checkoutTelemetry.trackNonFatalError("payment_preference", error)
            }
        }
}
