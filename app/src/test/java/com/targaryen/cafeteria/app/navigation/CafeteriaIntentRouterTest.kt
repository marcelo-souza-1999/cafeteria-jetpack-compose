package com.targaryen.cafeteria.app.navigation

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.tracker.AppActionNavigationTracker
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CafeteriaIntentRouterTest {
    private lateinit var paymentStatusTracker: PaymentStatusTracker
    private lateinit var catalogRepository: CatalogRepository
    private lateinit var navigationTracker: AppActionNavigationTracker
    private lateinit var router: CafeteriaIntentRouter

    @Before
    fun setup() {
        paymentStatusTracker = mockk(relaxed = true)
        catalogRepository = mockk(relaxed = true)
        navigationTracker = AppActionNavigationTracker()
        router =
            CafeteriaIntentRouter(
                paymentStatusTracker = paymentStatusTracker,
                catalogRepository = catalogRepository,
                appActionNavigationTracker = navigationTracker,
            )
    }

    @Test
    fun `routeIntent with feature query param navigates to favorites`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/feature?feature=favoritos")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.FAVORITES, navigatedTab)
        }

    @Test
    fun `routeIntent with legacy name query param navigates to cart`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/feature?name=carrinho")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.CART, navigatedTab)
        }

    @Test
    fun `routeIntent with feature extra navigates to favorites`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    putExtra(AppActionConstants.Extra.FEATURE, "Meus Favoritos")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.FAVORITES, navigatedTab)
        }

    @Test
    fun `routeIntent with ACTION_SEARCH for carrinho navigates to cart`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_SEARCH).apply {
                    putExtra(SearchManager.QUERY, "abrir carrinho")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.CART, navigatedTab)
        }

    @Test
    fun `routeIntent with cafeteria search URI for favoritos navigates to favorites`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/search?query=favoritos")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.FAVORITES, navigatedTab)
        }

    @Test
    fun `routeIntent with status action requests latest order and navigates to profile`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/status")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.PROFILE, navigatedTab)
            assertTrue(navigationTracker.openLatestOrderRequest.value)
        }

    @Test
    fun `routeIntent with search query filters catalog without mutating cart`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/search?query=balerion")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.CATALOG, navigatedTab)
            assertEquals("balerion", navigationTracker.pendingSearchQuery.value)
            coVerify(exactly = 0) { catalogRepository.updateProductQuantity(any(), any()) }
        }

    @Test
    fun `routeIntent with chat query forwards message and navigates to chat`() =
        runTest {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("cafeteria://action/chat?query=Qual+o+melhor+cafe")
                }

            router.routeIntent(intent, this)

            val navigatedTab = navigationTracker.navigationEvents.first()
            assertEquals(TargaryenTab.CHAT, navigatedTab)
            assertEquals("Qual o melhor cafe", navigationTracker.pendingChatMessage.value)
        }
}
