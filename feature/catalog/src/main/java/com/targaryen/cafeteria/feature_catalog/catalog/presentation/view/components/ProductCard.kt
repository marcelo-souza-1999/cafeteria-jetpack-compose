package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@Composable
fun ProductCard(
    product: ProductUiModel,
    onProductClick: (ProductUiModel) -> Unit,
    onAddToCart: (ProductUiModel) -> Unit,
    onIncreaseQuantity: (ProductUiModel) -> Unit,
    onDecreaseQuantity: (ProductUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onProductClick(product) },
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusLarge),
        border = BorderStroke(TargaryenTheme.dimens.borderSmall, ValyrianGold.copy(alpha = 0.6f)),
        colors =
            CardDefaults.cardColors(
                containerColor = DragonScale,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = TargaryenTheme.dimens.spaceSmall,
            ),
    ) {
        Column(
            modifier = Modifier.padding(TargaryenTheme.dimens.spaceMedium),
        ) {
            AsyncImage(
                model = product.imageUrl?.replace(" ", "%20"),
                contentDescription = product.name,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TargaryenTheme.dimens.cardImageHeight)
                        .clip(RoundedCornerShape(TargaryenTheme.dimens.radiusMedium))
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors = listOf(Obsidian, BloodRed.copy(alpha = 0.5f)),
                                ),
                        ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                color = ValyrianGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraSmall))

            Text(
                text = product.description,
                style = MaterialTheme.typography.labelMedium,
                color = SilverHair,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(TargaryenTheme.dimens.descriptionHeight),
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

            Text(
                text =
                    String.format(
                        locale,
                        "%s %.2f",
                        stringResource(DesignSystemR.string.currency_symbol),
                        product.price,
                    ),
                style = MaterialTheme.typography.titleMedium,
                color = ValyrianGold,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            if (product.quantityInCart == 0) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(TargaryenTheme.dimens.buttonHeightMedium)
                            .clip(RoundedCornerShape(TargaryenTheme.dimens.radiusSmall))
                            .background(BloodRed)
                            .clickable { onAddToCart(product) }
                            .border(
                                width = TargaryenTheme.dimens.borderSmall,
                                color = ValyrianGold.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(TargaryenTheme.dimens.radiusSmall),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(DesignSystemR.string.add_to_cart_label),
                        color = ValyrianGold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(TargaryenTheme.dimens.buttonHeightMedium)
                            .border(
                                width = TargaryenTheme.dimens.borderSmall,
                                color = ValyrianGold.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(TargaryenTheme.dimens.radiusSmall),
                            ).background(Obsidian),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onDecreaseQuantity(product) },
                        modifier = Modifier.size(TargaryenTheme.dimens.buttonHeightMedium),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(DesignSystemR.string.content_desc_decrease),
                            tint = ValyrianGold,
                            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeSmall),
                        )
                    }

                    Text(
                        text = product.quantityInCart.toString(),
                        color = TargaryenWhite,
                        style = MaterialTheme.typography.titleSmall,
                    )

                    IconButton(
                        onClick = { onIncreaseQuantity(product) },
                        modifier = Modifier.size(TargaryenTheme.dimens.buttonHeightMedium),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(DesignSystemR.string.content_desc_increase),
                            tint = ValyrianGold,
                            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeSmall),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Product Card - Dark Mode", showBackground = true)
@Composable
fun ProductCardPreview() {
    TargaryenTheme {
        ProductCard(
            product =
                ProductUiModel(
                    id = "1",
                    name = "Targaryen Blood Blend",
                    description = "Café extraído sob o fogo do dragão, encorpado e com notas intensas.",
                    price = 12.5,
                    category = "Bebidas Quentes",
                    quantityInCart = 2,
                ),
            onProductClick = {},
            onAddToCart = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
        )
    }
}
