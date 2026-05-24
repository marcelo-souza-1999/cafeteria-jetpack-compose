package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold

private const val OPACITY_BORDER = 0.3f
private const val GLOW_RADIUS_FACTOR = 0.5f
private const val GRADIENT_STOP_START = 0.0f
private const val GRADIENT_STOP_CENTER = 0.5f
private const val GRADIENT_STOP_END = 1.0f
private const val GLOW_ALPHA_FACTOR = 0.3f

private const val ANIMATION_DURATION_MS = 1200
private const val GLOW_SCALE_MIN = 0.95f
private const val GLOW_SCALE_MAX = 1.15f
private const val GLOW_ALPHA_MIN = 0.15f
private const val GLOW_ALPHA_MAX = 0.45f

@Composable
fun CategoryFilters(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = GLOW_SCALE_MIN,
        targetValue = GLOW_SCALE_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = GLOW_ALPHA_MIN,
        targetValue = GLOW_ALPHA_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceSmall),
        contentPadding = PaddingValues(horizontal = TargaryenTheme.dimens.spaceNormal)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val containerColor = if (isSelected) BloodRed else Obsidian
            val borderColor = if (isSelected) ValyrianGold else SilverHair.copy(alpha = OPACITY_BORDER)
            val textColor = if (isSelected) ValyrianGold else TargaryenWhite

            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier.drawBehind {
                                val radius = size.minDimension * GLOW_RADIUS_FACTOR * glowScale
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colorStops = arrayOf(
                                            GRADIENT_STOP_START to BloodRed.copy(alpha = glowAlpha),
                                            GRADIENT_STOP_CENTER to ValyrianGold.copy(alpha = glowAlpha * GLOW_ALPHA_FACTOR),
                                            GRADIENT_STOP_END to Color.Transparent
                                        ),
                                        center = center,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = center
                                )
                            }
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(TargaryenTheme.dimens.radiusLarge))
                    .background(containerColor)
                    .border(
                        width = TargaryenTheme.dimens.borderSmall,
                        color = borderColor,
                        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusLarge)
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(
                        horizontal = TargaryenTheme.dimens.spaceNormal,
                        vertical = TargaryenTheme.dimens.spaceSmall
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(name = "Category Filters - Dark Mode", showBackground = true)
@Composable
fun CategoryFiltersPreview() {
    TargaryenTheme {
        CategoryFilters(
            categories = listOf("Todos", "Bebidas Quentes", "Bebidas Frias", "Banquete Real"),
            selectedCategory = "Bebidas Quentes",
            onCategorySelected = {}
        )
    }
}
