package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.state.CatalogUiState
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.viewmodel.CatalogViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@RunWith(AndroidJUnit4::class)
class CatalogScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel: CatalogViewModel = mockk(relaxed = true)
    private val uiStateFlow = MutableStateFlow(CatalogUiState())

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val mockUiProducts =
        listOf(
            ProductUiModel(
                id = "1",
                name = "Dragon Espresso",
                description = "Dark and fiery",
                price = 4.5,
                category = "DRAGON_FIRE",
                imageUrl = "",
                isFavorite = false,
                quantityInCart = 0,
            ),
        )

    @Before
    fun setup() {
        every { mockViewModel.uiState } returns uiStateFlow
    }

    private fun setContent() {
        composeTestRule.setContent {
            TargaryenTheme {
                CatalogScreenContent(
                    uiState = uiStateFlow.collectAsState().value,
                    onIntent = { intent -> mockViewModel.onIntent(intent) },
                    actions =
                        CatalogActions(
                            onTabSelected = {},
                            onMenuClick = {},
                            onLogoutClick = {},
                            onCheckoutClick = {},
                        ),
                )
            }
        }
    }

    @Test
    fun catalogScreen_shouldDisplayTopBarAndProducts() {
        uiStateFlow.value = CatalogUiState(products = mockUiProducts)
        setContent()

        // Check if top bar title is displayed
        composeTestRule
            .onAllNodesWithText(
                context.getString(DesignSystemR.string.top_bar_title),
            ).onFirst()
            .assertIsDisplayed()

        // Check if mock product is displayed
        composeTestRule.onNodeWithText("Dragon Espresso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark and fiery").assertIsDisplayed()
    }
}
