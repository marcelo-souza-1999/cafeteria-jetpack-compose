package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.components.CategoryFilters
import com.targaryen.cafeteria.core_designsystem.components.TargaryenBottomBar
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTopBar
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.state.CatalogUiState
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CartScreen
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CartScreenActions
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CatalogDrawerContent
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CatalogSearchBar
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.FavoritesScreenStub
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.GeminiSimulatorBottomSheet
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.LogoutConfirmationDialog
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.ProductDetailBottomSheet
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.ProductGrid
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.viewmodel.CatalogViewModel
import com.targaryen.cafeteria.feature_catalog.chat.presentation.view.ChatScreen
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.ProfileScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@Composable
fun CatalogScreen(
    onTabSelected: (TargaryenTab) -> Unit,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val actions =
        remember(onTabSelected, onMenuClick, onLogoutClick, onCheckoutClick) {
            CatalogActions(onTabSelected, onMenuClick, onLogoutClick, onCheckoutClick)
        }

    CatalogScreenContent(
        uiState = uiState,
        onIntent = { intent -> viewModel.onIntent(intent) },
        actions = actions,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreenContent(
    uiState: CatalogUiState,
    onIntent: (CatalogIntent) -> Unit,
    actions: CatalogActions,
    modifier: Modifier = Modifier,
) {
    val categories =
        listOf(
            CatalogCategories.ALL,
            CatalogCategories.DRAGON_FIRE,
            CatalogCategories.ICE_BREATH,
            CatalogCategories.ROYAL_FEAST,
            CatalogCategories.CROWN_ELIXIRS,
        )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showLogoutConfirmation = remember { mutableStateOf(false) }
    val showGeminiSimulator = remember { mutableStateOf(false) }
    val simulatorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val pagerState = rememberPagerState(initialPage = 0) { TargaryenTab.entries.size }

    val currentTab = TargaryenTab.entries[pagerState.currentPage]

    val cartItemsCount =
        remember(uiState.products) {
            uiState.products.count { product -> product.quantityInCart > 0 }
        }

    LaunchedEffect(cartItemsCount) {
        if (cartItemsCount == 0 && pagerState.currentPage == TargaryenTab.CART.ordinal) {
            pagerState.scrollToPage(TargaryenTab.CATALOG.ordinal)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            CatalogDrawerContent(
                currentTab = currentTab,
                onTabClick = { tab ->
                    scope.launch {
                        drawerState.close()
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                },
                onLogoutClick = {
                    scope.launch {
                        drawerState.close()
                        showLogoutConfirmation.value = true
                    }
                },
                onDebugSimulatorClick = {
                    scope.launch {
                        drawerState.close()
                        showGeminiSimulator.value = true
                    }
                },
            )
        },
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TargaryenTopBar(
                    title = stringResource(DesignSystemR.string.top_bar_title),
                    onMenuClick = {
                        actions.onMenuClick()
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onLogoutClick = { showLogoutConfirmation.value = true },
                )
            },
            bottomBar = {
                TargaryenBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        scope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                        actions.onTabSelected(tab)
                    },
                    badgeCount = uiState.badgeCount,
                )
            },
            containerColor = Obsidian,
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Obsidian)
                        .padding(paddingValues),
            ) { page ->
                when (page) {
                    TargaryenTab.CATALOG.ordinal -> {
                        CatalogTabContent(
                            uiState = uiState,
                            categories = categories,
                            onIntent = onIntent,
                        )
                    }

                    TargaryenTab.FAVORITES.ordinal -> {
                        FavoritesTabContent(
                            favoriteProducts = uiState.favoriteProducts,
                            onIntent = onIntent,
                        )
                    }

                    TargaryenTab.CHAT.ordinal -> {
                        ChatScreen(
                            onNavigateToCheckout = actions.onCheckoutClick,
                            viewModel = koinViewModel(),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    TargaryenTab.CART.ordinal ->
                        CartScreen(
                            products = uiState.products.filter { product -> product.quantityInCart > 0 },
                            actions =
                                CartScreenActions(
                                    onIncreaseQuantity = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                                    onDecreaseQuantity = { item -> onIntent(CatalogIntent.RemoveFromCart(item)) },
                                    onRemoveProduct = { item ->
                                        onIntent(
                                            CatalogIntent.UpdateProductQuantity(
                                                item.id,
                                                0,
                                            ),
                                        )
                                    },
                                    onProductClick = { item -> onIntent(CatalogIntent.SelectProduct(item)) },
                                    onCheckoutClick = actions.onCheckoutClick,
                                ),
                        )

                    TargaryenTab.PROFILE.ordinal ->
                        ProfileScreen(
                            viewModel = koinViewModel(),
                            onLogout = { showLogoutConfirmation.value = true },
                        )
                }
            }

            if (uiState.selectedProduct != null) {
                ProductDetailBottomSheet(
                    product = uiState.selectedProduct,
                    onDismiss = { onIntent(CatalogIntent.SelectProduct(null)) },
                    onIncreaseQuantity = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                    onDecreaseQuantity = { item -> onIntent(CatalogIntent.RemoveFromCart(item)) },
                    onToggleFavorite = { item -> onIntent(CatalogIntent.ToggleFavorite(item)) },
                )
            }

            if (showLogoutConfirmation.value) {
                LogoutConfirmationDialog(
                    onConfirm = {
                        showLogoutConfirmation.value = false
                        actions.onLogoutClick()
                    },
                    onDismiss = { showLogoutConfirmation.value = false },
                )
            }

            if (showGeminiSimulator.value) {
                GeminiSimulatorBottomSheet(
                    sheetState = simulatorSheetState,
                    onDismissRequest = { showGeminiSimulator.value = false },
                )
            }
        }
    }
}

@Composable
private fun CatalogTabContent(
    uiState: CatalogUiState,
    categories: List<String>,
    onIntent: (CatalogIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

        CatalogSearchBar(
            query = uiState.searchQuery,
            onQueryChange = { text -> onIntent(CatalogIntent.Search(text)) },
            onClearQuery = { onIntent(CatalogIntent.ClearSearch) },
            modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceNormal),
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

        CategoryFilters(
            categories = categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category ->
                onIntent(
                    CatalogIntent.SelectCategory(
                        category,
                    ),
                )
            },
        )

        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

        ProductGrid(
            products = uiState.products,
            onProductClick = { item -> onIntent(CatalogIntent.SelectProduct(item)) },
            onAddToCart = { item -> onIntent(CatalogIntent.AddToCart(item)) },
            onIncreaseQuantity = { item -> onIntent(CatalogIntent.AddToCart(item)) },
            onDecreaseQuantity = { item ->
                onIntent(
                    CatalogIntent.RemoveFromCart(
                        item,
                    ),
                )
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f),
        )
    }
}

@Composable
private fun FavoritesTabContent(
    favoriteProducts: List<ProductUiModel>,
    onIntent: (CatalogIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favoriteProducts.isEmpty()) {
        FavoritesScreenStub(modifier = modifier)
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
            Text(
                text = stringResource(DesignSystemR.string.favorites_tab_title),
                style = MaterialTheme.typography.titleMedium,
                color = ValyrianGold,
                modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceNormal),
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
            ProductGrid(
                products = favoriteProducts,
                onProductClick = { item ->
                    onIntent(
                        CatalogIntent.SelectProduct(
                            item,
                        ),
                    )
                },
                onAddToCart = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                onIncreaseQuantity = { item ->
                    onIntent(
                        CatalogIntent.AddToCart(
                            item,
                        ),
                    )
                },
                onDecreaseQuantity = { item ->
                    onIntent(
                        CatalogIntent.RemoveFromCart(
                            item,
                        ),
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(name = "Catalog Screen - Dark Mode")
@Composable
fun CatalogScreenPreview() {
    val mockProducts =
        listOf(
            ProductUiModel(
                id = "1",
                name = "Targaryen Blood Blend",
                description =
                    "Café extraído sob o fogo do dragão, encorpado e com notas " +
                        "intensas de especiarias e frutas vermelhas da antiga Valíria.",
                price = 12.5,
                category = CatalogCategories.DRAGON_FIRE,
                quantityInCart = 1,
            ),
            ProductUiModel(
                id = "2",
                name = "Valyrian Velvet Latte",
                description =
                    "Uma combinação sedosa e mística de café espresso robusto, " +
                        "leite vaporizado cremoso e um toque sutil de cacau sagrado.",
                price = 15.0,
                category = CatalogCategories.DRAGON_FIRE,
                quantityInCart = 0,
            ),
            ProductUiModel(
                id = "3",
                name = "Dragonstone Brew",
                description =
                    "Cold brew maturado em rochas vulcânicas de Dragonstone, " +
                        "extremamente refrescante, infundido com notas cítricas de laranja e menta.",
                price = 10.0,
                category = CatalogCategories.DRAGON_FIRE,
                quantityInCart = 0,
            ),
            ProductUiModel(
                id = "4",
                name = "Winterfell Frost",
                description =
                    "Café gelado batido com menta selvagem colhida além da " +
                        "Muralha e um xarope doce artesanal das terras do Norte.",
                price = 11.0,
                category = CatalogCategories.ICE_BREATH,
                quantityInCart = 0,
            ),
            ProductUiModel(
                id = "5",
                name = "Banquete de Aegon",
                description =
                    "Uma seleção rústica e farta de pães artesanais de Westeros " +
                        "servidos quentes com geleia de frutas silvestres e manteiga trufada.",
                price = 35.0,
                category = CatalogCategories.ROYAL_FEAST,
                quantityInCart = 0,
            ),
            ProductUiModel(
                id = "6",
                name = "Torta de Limão de Sansa",
                description =
                    "Fatia generosa de torta de limão siciliano selvagem, com " +
                        "massa folhada e merengue dourado perfeitamente tostado.",
                price = 18.0,
                category = CatalogCategories.ROYAL_FEAST,
                quantityInCart = 0,
            ),
            ProductUiModel(
                id = "7",
                name = "Lágrimas de Lys",
                description =
                    "Elixir doce e perigoso com infusão de flores raras das ilhas " +
                        "de Lys, servido gelado para os que ousam desafiar o destino.",
                price = 22.0,
                category = CatalogCategories.CROWN_ELIXIRS,
                quantityInCart = 0,
            ),
        )

    TargaryenTheme {
        CatalogScreenContent(
            uiState =
                CatalogUiState(
                    searchQuery = "",
                    selectedCategory = CatalogCategories.ALL,
                    products = mockProducts,
                    selectedProduct = null,
                    badgeCount = 1,
                ),
            onIntent = {},
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
