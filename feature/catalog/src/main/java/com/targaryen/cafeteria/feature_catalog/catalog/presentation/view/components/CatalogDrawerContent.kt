package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR
import com.targaryen.cafeteria.feature_catalog.R as CatalogR

private const val BRAND_LOGO_SCALE = 1.3f

@Composable
internal fun CatalogDrawerContent(
    currentTab: TargaryenTab,
    onTabClick: (TargaryenTab) -> Unit,
    onLogoutClick: () -> Unit,
    onDebugSimulatorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        modifier = modifier,
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
                        id = DesignSystemR.drawable.ic_logo_login_screen,
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
            onClick = { onTabClick(TargaryenTab.CATALOG) },
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
            onClick = { onTabClick(TargaryenTab.FAVORITES) },
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
            onClick = { onTabClick(TargaryenTab.CART) },
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
            onClick = { onTabClick(TargaryenTab.PROFILE) },
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

        if (com.targaryen.cafeteria.feature_catalog.BuildConfig.DEBUG) {
            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(CatalogR.string.voice_command_drawer_title),
                        color = ValyrianGold,
                    )
                },
                selected = false,
                onClick = onDebugSimulatorClick,
                icon = {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = ValyrianGold,
                    )
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent,
                    ),
                modifier = Modifier.padding(horizontal = TargaryenTheme.dimens.spaceSmall),
            )
        }

        NavigationDrawerItem(
            label = {
                Text(
                    stringResource(DesignSystemR.string.drawer_menu_logout),
                    color = BloodRed,
                )
            },
            selected = false,
            onClick = onLogoutClick,
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
}

@Preview(name = "Catalog Drawer - Dark Mode")
@Composable
fun CatalogDrawerContentPreview() {
    TargaryenTheme {
        CatalogDrawerContent(
            currentTab = TargaryenTab.CATALOG,
            onTabClick = {},
            onLogoutClick = {},
            onDebugSimulatorClick = {},
        )
    }
}
