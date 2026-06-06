package com.targaryen.cafeteria.feature_checkout.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState
import com.targaryen.cafeteria.feature_checkout.presentation.viewmodel.CheckoutViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel: CheckoutViewModel = mockk(relaxed = true)
    private val uiStateFlow = MutableStateFlow(CheckoutState())

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        every { mockViewModel.uiState } returns uiStateFlow
    }

    private fun setContent(onNavigateBack: () -> Unit = {}) {
        composeTestRule.setContent {
            TargaryenTheme {
                CheckoutScreen(
                    state = uiStateFlow.value,
                    onIntent = { intent -> mockViewModel.onIntent(intent) },
                    onNavigateBack = onNavigateBack,
                    onPaymentSuccess = {}
                )
            }
        }
    }

    @Test
    fun checkoutScreen_initialState_shouldDisplayTitleAndFields() {
        uiStateFlow.value = CheckoutState()
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.checkout_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.checkout_label_cep)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.checkout_action_submit)).assertIsDisplayed()
    }

    @Test
    fun checkoutScreen_whenTypingCep_shouldDispatchIntent() {
        uiStateFlow.value = CheckoutState()
        setContent()

        val cep = "01001000"
        composeTestRule.onNodeWithText(context.getString(R.string.checkout_label_cep)).performTextInput(cep)

        verify { mockViewModel.onIntent(CheckoutIntent.OnCepChanged(cep)) }
    }
}
