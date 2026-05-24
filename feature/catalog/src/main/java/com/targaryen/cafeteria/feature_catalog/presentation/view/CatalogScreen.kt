package com.targaryen.cafeteria.feature_catalog.presentation.view

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.components.CategoryFilters
import com.targaryen.cafeteria.core_designsystem.components.TargaryenBottomBar
import com.targaryen.cafeteria.core_designsystem.components.TargaryenTopBar
import com.targaryen.cafeteria.core_designsystem.model.CatalogConstants
import com.targaryen.cafeteria.core_designsystem.model.CatalogConstants.TOP_BAR_TITLE
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.presentation.intent.CatalogIntent
import com.targaryen.cafeteria.feature_catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.feature_catalog.presentation.state.CatalogUiState
import com.targaryen.cafeteria.feature_catalog.presentation.view.components.CatalogSearchBar
import com.targaryen.cafeteria.feature_catalog.presentation.view.components.ProductDetailBottomSheet
import com.targaryen.cafeteria.feature_catalog.presentation.view.components.ProductGrid
import com.targaryen.cafeteria.feature_catalog.presentation.viewmodel.CatalogViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CatalogScreen(
    onTabSelected: (TargaryenTab) -> Unit,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CatalogScreenContent(
        uiState = uiState,
        onIntent = { intent -> viewModel.onIntent(intent) },
        onTabSelected = onTabSelected,
        onMenuClick = onMenuClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreenContent(
    uiState: CatalogUiState,
    onIntent: (CatalogIntent) -> Unit,
    onTabSelected: (TargaryenTab) -> Unit,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CatalogConstants.CATEGORY_ALL,
        CatalogConstants.CATEGORY_DRAGON_FIRE,
        CatalogConstants.CATEGORY_ICE_BREATH,
        CatalogConstants.CATEGORY_ROYAL_FEAST,
        CatalogConstants.CATEGORY_CROWN_ELIXIRS
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Configurar o Pager de 4 páginas
    val pagerState = rememberPagerState(initialPage = 0) { 4 }

    // Sincronizar o estado do Pager com a BottomBar
    val currentTab = TargaryenTab.entries[pagerState.currentPage]

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = pagerState.currentPage == 0,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DragonScale,
                drawerContentColor = TargaryenWhite
            ) {
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

                // Cabeçalho Imperial
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TargaryenTheme.dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            id = com.targaryen.cafeteria.core_designsystem.R.drawable.ic_logo_login_screen
                        ),
                        contentDescription = "Brasão Targaryen",
                        modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge * 1.3f)
                    )
                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                    Text(
                        text = "CASA TARGARYEN",
                        style = MaterialTheme.typography.titleMedium,
                        color = ValyrianGold
                    )
                    Text(
                        text = "Fogo e Sangue",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverHair.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

                // Itens de Navegação do Drawer
                NavigationDrawerItem(
                    label = { Text("O Menu do Dragão", color = ValyrianGold) },
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
                            tint = ValyrianGold
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = BloodRed.copy(alpha = 0.3f),
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall)
                )

                NavigationDrawerItem(
                    label = { Text("Banquetes Agendados", color = ValyrianGold) },
                    selected = false,
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
                            tint = ValyrianGold
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall)
                )

                NavigationDrawerItem(
                    label = { Text("Tesouros da Coroa", color = ValyrianGold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall)
                )

                NavigationDrawerItem(
                    label = { Text("Mensagens do Corvo", color = ValyrianGold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = ValyrianGold
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("Abandonar o Trono", color = BloodRed) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogoutClick()
                        }
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = BloodRed
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall)
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TargaryenTopBar(
                    title = TOP_BAR_TITLE,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onLogoutClick = onLogoutClick
                )
            },
            bottomBar = {
                TargaryenBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        scope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                        onTabSelected(tab)
                    },
                    badgeCount = uiState.badgeCount
                )
            },
            containerColor = Obsidian
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Obsidian)
                    .padding(paddingValues)
            ) { page ->
                when (page) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                            // Campo de busca
                            CatalogSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = { text -> onIntent(CatalogIntent.Search(text)) },
                                onClearQuery = { onIntent(CatalogIntent.ClearSearch) },
                                modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceNormal)
                            )

                            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                            // Filtros por Categoria
                            CategoryFilters(
                                categories = categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = { category ->
                                    onIntent(
                                        CatalogIntent.SelectCategory(
                                            category
                                        )
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

                            // Grid de Produtos
                            ProductGrid(
                                products = uiState.products,
                                onProductClick = { item -> onIntent(CatalogIntent.SelectProduct(item)) },
                                onAddToCart = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                                onIncreaseQuantity = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                                onDecreaseQuantity = { item ->
                                    onIntent(
                                        CatalogIntent.RemoveFromCart(
                                            item
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )
                        }
                    }

                    1 -> {
                        if (uiState.favoriteProducts.isEmpty()) {
                            FavoritesScreenStub()
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
                                Text(
                                    text = "Seus Favoritos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ValyrianGold,
                                    modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceNormal)
                                )
                                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                                ProductGrid(
                                    products = uiState.favoriteProducts,
                                    onProductClick = { item ->
                                        onIntent(
                                            CatalogIntent.SelectProduct(
                                                item
                                            )
                                        )
                                    },
                                    onAddToCart = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                                    onIncreaseQuantity = { item ->
                                        onIntent(
                                            CatalogIntent.AddToCart(
                                                item
                                            )
                                        )
                                    },
                                    onDecreaseQuantity = { item ->
                                        onIntent(
                                            CatalogIntent.RemoveFromCart(
                                                item
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    2 -> CartScreenStub(badgeCount = uiState.badgeCount)
                    3 -> ProfileScreenStub()
                }
            }

            // ModalBottomSheet de Detalhe do Produto
            if (uiState.selectedProduct != null) {
                ProductDetailBottomSheet(
                    product = uiState.selectedProduct,
                    onDismiss = { onIntent(CatalogIntent.SelectProduct(null)) },
                    onIncreaseQuantity = { item -> onIntent(CatalogIntent.AddToCart(item)) },
                    onDecreaseQuantity = { item -> onIntent(CatalogIntent.RemoveFromCart(item)) },
                    onToggleFavorite = { item -> onIntent(CatalogIntent.ToggleFavorite(item)) }
                )
            }
        }
    }
}

@Composable
fun FavoritesScreenStub() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = BloodRed
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = "Favoritos do Trono",
            style = MaterialTheme.typography.titleLarge,
            color = ValyrianGold
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = "Suas iguarias preferidas do reino surgirão aqui sob o selo do dragão.",
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CartScreenStub(badgeCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = ValyrianGold
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = "Bússola de Banquetes",
            style = MaterialTheme.typography.titleLarge,
            color = ValyrianGold
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = if (badgeCount > 0) {
                "Você possui $badgeCount itens selecionados no baú. Aguardando suas moedas de ouro para selar o banquete."
            } else {
                "Seu baú de banquetes está vazio. Visite o catálogo e ordene suas provisões."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ProfileScreenStub() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = ValyrianGold
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = "Linhagem de Nobreza",
            style = MaterialTheme.typography.titleLarge,
            color = ValyrianGold
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = "Lorde de Westeros\nNível de Fidelidade: Herdeiro do Trono",
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(name = "Catalog Screen - Dark Mode")
@Composable
fun CatalogScreenPreview() {
    val mockProducts = listOf(
        ProductUiModel(
            id = "1",
            name = "Targaryen Blood Blend",
            description = "Café extraído sob o fogo do dragão, encorpado e com notas " +
                    "intensas de especiarias e frutas vermelhas da antiga Valíria.",
            price = 12.5,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE,
            quantityInCart = 1
        ),
        ProductUiModel(
            id = "2",
            name = "Valyrian Velvet Latte",
            description = "Uma combinação sedosa e mística de café espresso robusto, " +
                    "leite vaporizado cremoso e um toque sutil de cacau sagrado.",
            price = 15.0,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE,
            quantityInCart = 0
        ),
        ProductUiModel(
            id = "3",
            name = "Dragonstone Brew",
            description = "Cold brew maturado em rochas vulcânicas de Dragonstone, " +
                    "extremamente refrescante, infundido com notas cítricas de laranja e menta.",
            price = 10.0,
            category = CatalogConstants.CATEGORY_DRAGON_FIRE,
            quantityInCart = 0
        ),
        ProductUiModel(
            id = "4",
            name = "Winterfell Frost",
            description = "Café gelado batido com menta selvagem colhida além da " +
                    "Muralha e um xarope doce artesanal das terras do Norte.",
            price = 11.0,
            category = CatalogConstants.CATEGORY_ICE_BREATH,
            quantityInCart = 0
        ),
        ProductUiModel(
            id = "5",
            name = "Banquete de Aegon",
            description = "Uma seleção rústica e farta de pães artesanais de Westeros " +
                    "servidos quentes com geleia de frutas silvestres e manteiga trufada.",
            price = 35.0,
            category = CatalogConstants.CATEGORY_ROYAL_FEAST,
            quantityInCart = 0
        ),
        ProductUiModel(
            id = "6",
            name = "Torta de Limão de Sansa",
            description = "Fatia generosa de torta de limão siciliano selvagem, com " +
                    "massa folhada e merengue dourado perfeitamente tostado.",
            price = 18.0,
            category = CatalogConstants.CATEGORY_ROYAL_FEAST,
            quantityInCart = 0
        ),
        ProductUiModel(
            id = "7",
            name = "Lágrimas de Lys",
            description = "Elixir doce e perigoso com infusão de flores raras das ilhas " +
                    "de Lys, servido gelado para os que ousam desafiar o destino.",
            price = 22.0,
            category = CatalogConstants.CATEGORY_CROWN_ELIXIRS,
            quantityInCart = 0
        )
    )

    TargaryenTheme {
        CatalogScreenContent(
            uiState = CatalogUiState(
                searchQuery = "",
                selectedCategory = CatalogConstants.CATEGORY_ALL,
                products = mockProducts,
                selectedProduct = null,
                badgeCount = 1
            ),
            onIntent = {},
            onTabSelected = {},
            onMenuClick = {},
            onLogoutClick = {}
        )
    }
}
