package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.R
import com.targaryen.cafeteria.core_designsystem.components.CategoryFilters
import com.targaryen.cafeteria.core_designsystem.components.TargaryenBottomBar
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTopBar
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.state.CatalogUiState
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CartScreen
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.CatalogSearchBar
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.LogoutConfirmationDialog
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.ProductDetailBottomSheet
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components.ProductGrid
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.viewmodel.CatalogViewModel
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
            ModalDrawerSheet(
                drawerContainerColor = DragonScale,
                drawerContentColor = TargaryenWhite,
            ) {
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(TargaryenTheme.dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter =
                            painterResource(
                                id = R.drawable.ic_logo_login_screen,
                            ),
                        contentDescription = null,
                        modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge * BRAND_LOGO_SCALE),
                    )
                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                    Text(
                        text = stringResource(DesignSystemR.string.drawer_brand_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = ValyrianGold,
                    )
                    Text(
                        text = stringResource(DesignSystemR.string.drawer_brand_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverHair.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(DesignSystemR.string.drawer_menu_home),
                            color = ValyrianGold,
                        )
                    },
                    selected = currentTab == TargaryenTab.CATALOG,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            pagerState.animateScrollToPage(TargaryenTab.CATALOG.ordinal)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = ValyrianGold,
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BloodRed.copy(alpha = 0.3f),
                            unselectedContainerColor = Color.Transparent,
                        ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(DesignSystemR.string.drawer_menu_favorites),
                            color = ValyrianGold,
                        )
                    },
                    selected = currentTab == TargaryenTab.FAVORITES,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            pagerState.animateScrollToPage(TargaryenTab.FAVORITES.ordinal)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = ValyrianGold,
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BloodRed.copy(alpha = 0.3f),
                            unselectedContainerColor = Color.Transparent,
                        ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(DesignSystemR.string.drawer_menu_cart),
                            color = ValyrianGold,
                        )
                    },
                    selected = currentTab == TargaryenTab.CART,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            pagerState.animateScrollToPage(TargaryenTab.CART.ordinal)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = ValyrianGold,
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BloodRed.copy(alpha = 0.3f),
                            unselectedContainerColor = Color.Transparent,
                        ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(DesignSystemR.string.drawer_menu_profile),
                            color = ValyrianGold,
                        )
                    },
                    selected = currentTab == TargaryenTab.PROFILE,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            pagerState.animateScrollToPage(TargaryenTab.PROFILE.ordinal)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = ValyrianGold,
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BloodRed.copy(alpha = 0.3f),
                            unselectedContainerColor = Color.Transparent,
                        ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(DesignSystemR.string.drawer_menu_logout),
                            color = BloodRed,
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showLogoutConfirmation.value = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = BloodRed,
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Transparent,
                            unselectedContainerColor = Color.Transparent,
                        ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            }
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
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
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

                    TargaryenTab.FAVORITES.ordinal -> {
                        if (uiState.favoriteProducts.isEmpty()) {
                            FavoritesScreenStub()
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
                                Text(
                                    text = stringResource(DesignSystemR.string.favorites_tab_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ValyrianGold,
                                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceNormal),
                                )
                                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                                ProductGrid(
                                    products = uiState.favoriteProducts,
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

                    TargaryenTab.CART.ordinal ->
                        CartScreen(
                            products = uiState.products.filter { product -> product.quantityInCart > 0 },
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
        }
    }
}

@Composable
fun FavoritesScreenStub() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Obsidian)
                .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = BloodRed,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = stringResource(DesignSystemR.string.favorites_stub_title),
            style = MaterialTheme.typography.titleLarge,
            color = ValyrianGold,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = stringResource(DesignSystemR.string.favorites_stub_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = TextAlign.Center,
        )
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

private const val BRAND_LOGO_SCALE = 1.3f
