package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold

@Composable
fun TargaryenBottomBar(
    currentTab: TargaryenTab,
    onTabSelected: (TargaryenTab) -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 3
) {
    NavigationBar(
        containerColor = Obsidian,
        modifier = modifier
    ) {
        TargaryenTab.entries.forEach { tab ->
            TargaryenBottomBarItem(
                tab = tab,
                isSelected = currentTab == tab,
                badgeCount = badgeCount,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun RowScope.TargaryenBottomBarItem(
    tab: TargaryenTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val label = when (tab) {
        TargaryenTab.CATALOG -> "Catálogo"
        TargaryenTab.FAVORITES -> "Favoritos"
        TargaryenTab.CART -> "Carrinho"
        TargaryenTab.PROFILE -> "Perfil"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "DragonEmbers")
    val animatedScale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )
    val animatedAlpha = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val glowScale = if (isSelected) animatedScale.value else 0f
    val glowAlpha = if (isSelected) animatedAlpha.value else 0f

    val iconModifier = if (isSelected) {
        Modifier.drawBehind {
            val radius = size.minDimension / 2f * glowScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BloodRed.copy(alpha = glowAlpha),
                        ValyrianGold.copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    } else {
        Modifier
    }

    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Box(
                modifier = iconModifier,
                contentAlignment = Alignment.Center
            ) {
                if (tab == TargaryenTab.CART) {
                    BadgedBox(
                        badge = {
                            if (badgeCount > 0) {
                                Badge(
                                    containerColor = BloodRed,
                                    contentColor = TargaryenWhite
                                ) {
                                    Text(text = badgeCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = label
                        )
                    }
                } else {
                    val icon = when (tab) {
                        TargaryenTab.CATALOG -> Icons.Default.Home
                        TargaryenTab.FAVORITES -> Icons.Default.Favorite
                        TargaryenTab.PROFILE -> Icons.Default.Person
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                }
            }
        },
        label = {
            Text(text = label)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = ValyrianGold,
            unselectedIconColor = SilverHair,
            selectedTextColor = ValyrianGold,
            unselectedTextColor = SilverHair,
            indicatorColor = Color.Transparent
        )
    )
}

@Preview(name = "Targaryen Bottom Bar - Dark Mode", showBackground = true)
@Composable
fun TargaryenBottomBarPreview() {
    TargaryenTheme {
        TargaryenBottomBar(
            currentTab = TargaryenTab.CART,
            onTabSelected = {}
        )
    }
}
