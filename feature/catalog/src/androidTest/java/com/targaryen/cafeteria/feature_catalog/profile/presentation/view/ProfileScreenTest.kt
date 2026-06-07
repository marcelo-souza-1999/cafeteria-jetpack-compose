package com.targaryen.cafeteria.feature_catalog.profile.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.presentation.intent.ProfileIntent
import com.targaryen.cafeteria.feature_catalog.profile.presentation.state.ProfileState
import com.targaryen.cafeteria.feature_catalog.profile.presentation.viewmodel.ProfileViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel: ProfileViewModel = mockk(relaxed = true)
    private val uiStateFlow = MutableStateFlow(ProfileState())

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        every { mockViewModel.uiState } returns uiStateFlow
    }

    private fun setContent(onLogout: () -> Unit = {}) {
        composeTestRule.setContent {
            TargaryenTheme {
                ProfileScreen(
                    onLogout = onLogout,
                    viewModel = mockViewModel,
                )
            }
        }
    }

    @Test
    fun profileScreen_initialState_shouldDisplayUserProfileDetails() {
        uiStateFlow.value =
            ProfileState(
                name = "Daemon Targaryen",
                email = "daemon@dragonstone.com",
                photoUrl = "preset_crown",
            )
        setContent()

        composeTestRule.onNodeWithText("Daemon Targaryen").assertIsDisplayed()
        composeTestRule.onNodeWithText("daemon@dragonstone.com").assertIsDisplayed()

        // Expandir seção de segurança para tornar o botão visível
        composeTestRule.onNodeWithText(context.getString(R.string.profile_section_security_title)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.profile_btn_delete_account)).assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenDeleteAccountClicked_shouldDispatchIntent() {
        uiStateFlow.value =
            ProfileState(
                name = "Daemon Targaryen",
                email = "daemon@dragonstone.com",
            )
        setContent()

        // Expandir seção de segurança para tornar o botão visível
        composeTestRule.onNodeWithText(context.getString(R.string.profile_section_security_title)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.profile_btn_delete_account)).performClick()

        verify { mockViewModel.onIntent(ProfileIntent.ShowDeleteDialog) }
    }
}
