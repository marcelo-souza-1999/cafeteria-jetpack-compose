package com.targaryen.cafeteria.app.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.targaryen.cafeteria.app.MainActivity
import com.targaryen.cafeteria.app.ui.main.MainScreenUiState
import com.targaryen.cafeteria.app.ui.main.MainScreenViewModel
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import com.targaryen.cafeteria.feature.auth.R as AuthR
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class AuthIntegrationTest : KoinTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val mainViewModel: MainScreenViewModel = mockk(relaxed = true)
    private val mainUiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Success(listOf("Targaryen Cafe")))

    private val testModule = module {
        single<AuthRepository>(createdAtStart = true) { authRepository }
        single<MainScreenViewModel> { mainViewModel }
    }

    @Before
    fun setup() {
        every { mainViewModel.uiState } returns mainUiState
        loadKoinModules(testModule)
    }

    @After
    fun tearDown() {
        unloadKoinModules(testModule)
    }

    @Test
    fun loginFlow_shouldNavigateToMainScreen_onSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Arrange
        every { authRepository.isUserLoggedIn() } returns false
        every { authRepository.signInWithEmail(any(), any()) } returns flowOf(Resource.Success(Unit))

        // Act & Assert
        // 1. Wait for Login Screen
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(context.getString(AuthR.string.title_login)).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Fill credentials
        composeTestRule.onNodeWithText(context.getString(AuthR.string.label_email)).performTextInput("queen@targaryen.com")
        composeTestRule.onNodeWithText(context.getString(AuthR.string.label_password)).performTextInput("Dracarys123")

        Espresso.closeSoftKeyboard()

        // 3. Click Login
        composeTestRule.onNodeWithText(context.getString(AuthR.string.action_login)).performClick()

        // 4. Verify we reach MainScreen
        composeTestRule.waitUntil(timeoutMillis = 20000) {
            composeTestRule.onAllNodesWithText("Targaryen Cafe", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Targaryen Cafe", substring = true).assertIsDisplayed()
    }
}
