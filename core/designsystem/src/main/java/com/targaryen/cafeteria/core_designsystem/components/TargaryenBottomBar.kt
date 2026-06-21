package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.model.TargaryenTab
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold

private const val CHAT_GLOW_SCALE_FACTOR = 0.8f
private const val CHAT_GLOW_ALPHA_FACTOR = 0.4f
private const val GLOW_EDGE_ALPHA_FACTOR = 0.3f

@Composable
fun TargaryenBottomBar(
    currentTab: TargaryenTab,
    onTabSelected: (TargaryenTab) -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 3,
) {
    NavigationBar(
        containerColor = Obsidian,
        modifier = modifier,
    ) {
        TargaryenTab.entries.forEach { tab ->
            TargaryenBottomBarItem(
                tab = tab,
                isSelected = currentTab == tab,
                badgeCount = badgeCount,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

private fun getTabLabel(tab: TargaryenTab): String =
    when (tab) {
        TargaryenTab.CATALOG -> "Catálogo"
        TargaryenTab.FAVORITES -> "Favoritos"
        TargaryenTab.CHAT -> "Chat"
        TargaryenTab.CART -> "Carrinho"
        TargaryenTab.PROFILE -> "Perfil"
    }

private fun getTabIcon(tab: TargaryenTab): ImageVector =
    when (tab) {
        TargaryenTab.CATALOG -> Icons.Default.Home
        TargaryenTab.FAVORITES -> Icons.Default.Favorite
        TargaryenTab.CHAT -> Icons.Default.AutoAwesome
        TargaryenTab.PROFILE -> Icons.Default.Person
        TargaryenTab.CART -> Icons.Default.ShoppingCart
    }

@Composable
private fun TargaryenTabIcon(
    tab: TargaryenTab,
    isSelected: Boolean,
    glowScale: Float,
    glowAlpha: Float,
    badgeCount: Int,
) {
    val label = getTabLabel(tab)
    val isChat = tab == TargaryenTab.CHAT

    val chatModifier =
        if (isChat) {
            Modifier
                .size(TargaryenTheme.dimens.chatSendButtonSize)
                .background(
                    color = Obsidian,
                    shape = CircleShape,
                ).border(
                    width = TargaryenTheme.dimens.borderSmall,
                    color = if (isSelected) ValyrianGold else ValyrianGold.copy(alpha = 0.5f),
                    shape = CircleShape,
                ).padding(TargaryenTheme.dimens.spaceSmall)
        } else {
            Modifier
        }

    val iconModifier =
        if (isSelected || isChat) {
            Modifier.drawBehind {
                val radius = size.minDimension / 2f * glowScale
                val centerColor =
                    if (isChat && !isSelected) {
                        ValyrianGold.copy(alpha = glowAlpha)
                    } else {
                        BloodRed.copy(alpha = glowAlpha)
                    }
                val edgeColor =
                    if (isChat && !isSelected) {
                        BloodRed.copy(alpha = glowAlpha * GLOW_EDGE_ALPHA_FACTOR)
                    } else {
                        ValyrianGold.copy(alpha = glowAlpha * GLOW_EDGE_ALPHA_FACTOR)
                    }
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(centerColor, edgeColor, Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                    radius = radius,
                    center = center,
                )
            }
        } else {
            Modifier
        }

    Box(
        modifier = iconModifier.then(chatModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (tab == TargaryenTab.CART) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = BloodRed,
                            contentColor = TargaryenWhite,
                        ) {
                            Text(text = badgeCount.toString())
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = label,
                )
            }
        } else {
            Icon(
                imageVector = getTabIcon(tab),
                contentDescription = label,
                tint =
                    if (isChat) {
                        ValyrianGold
                    } else if (isSelected) {
                        ValyrianGold
                    } else {
                        SilverHair
                    },
            )
        }
    }
}

@Composable
private fun RowScope.TargaryenBottomBarItem(
    tab: TargaryenTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
) {
    val label = getTabLabel(tab)
    val infiniteTransition = rememberInfiniteTransition(label = "DragonEmbers")
    val animatedScale =
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "GlowScale",
        )
    val animatedAlpha =
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "GlowAlpha",
        )

    val isChat = tab == TargaryenTab.CHAT
    val glowScale =
        if (isSelected) {
            animatedScale.value
        } else if (isChat) {
            animatedScale.value * CHAT_GLOW_SCALE_FACTOR
        } else {
            0f
        }
    val glowAlpha =
        if (isSelected) {
            animatedAlpha.value
        } else if (isChat) {
            animatedAlpha.value * CHAT_GLOW_ALPHA_FACTOR
        } else {
            0f
        }

    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            TargaryenTabIcon(
                tab = tab,
                isSelected = isSelected,
                glowScale = glowScale,
                glowAlpha = glowAlpha,
                badgeCount = badgeCount,
            )
        },
        label = {
            Text(text = label)
        },
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = ValyrianGold,
                unselectedIconColor = SilverHair,
                selectedTextColor = ValyrianGold,
                unselectedTextColor = SilverHair,
                indicatorColor = Color.Transparent,
            ),
    )
}

@Preview(name = "Targaryen Bottom Bar - Dark Mode", showBackground = true)
@Composable
fun TargaryenBottomBarPreview() {
    TargaryenTheme {
        TargaryenBottomBar(
            currentTab = TargaryenTab.CART,
            onTabSelected = {},
        )
    }
}
