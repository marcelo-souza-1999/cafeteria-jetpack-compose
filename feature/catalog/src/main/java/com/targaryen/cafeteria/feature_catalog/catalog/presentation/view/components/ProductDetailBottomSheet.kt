package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.targaryen.cafeteria.core_designsystem.components.TargaryenButton
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.catalog.presentation.model.ProductUiModel
import java.util.Locale
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

private const val LABEL_UNIT_PRICE = "Preço Unitário:"
private const val LABEL_TOTAL_CART = "Total no Carrinho:"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    product: ProductUiModel,
    onDismiss: () -> Unit,
    onIncreaseQuantity: (ProductUiModel) -> Unit,
    onDecreaseQuantity: (ProductUiModel) -> Unit,
    onToggleFavorite: (ProductUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DragonScale,
        contentColor = TargaryenWhite,
        scrimColor = Obsidian.copy(alpha = 0.8f),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(TargaryenTheme.dimens.spaceLarge)
                    .padding(bottom = TargaryenTheme.dimens.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Imagem ampliada
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TargaryenTheme.dimens.bottomSheetImageHeight)
                        .clip(RoundedCornerShape(TargaryenTheme.dimens.radiusLarge))
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors = listOf(Obsidian, BloodRed.copy(alpha = 0.6f)),
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                coil3.compose.AsyncImage(
                    model = product.imageUrl?.replace(" ", "%20"),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )

                // Botão de Favorito Flutuante
                IconButton(
                    onClick = { onToggleFavorite(product) },
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(TargaryenTheme.dimens.spaceNormal),
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (product.isFavorite) BloodRed else SilverHair,
                        modifier = Modifier.size(TargaryenTheme.dimens.iconSizeMedium * FAVORITE_ICON_SCALE),
                    )
                }
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

            // Nome do Produto (Cinzel)
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                color = ValyrianGold,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraSmall))

            // Categoria
            Text(
                text = product.category.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = SilverHair.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            // Descrição (Montserrat)
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = SilverHair,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))

            // Preço unitário e total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = LABEL_UNIT_PRICE,
                    style = MaterialTheme.typography.labelLarge,
                    color = SilverHair,
                )
                Text(
                    text =
                        String.format(
                            Locale.forLanguageTag("pt-BR"),
                            "%s %.2f",
                            stringResource(DesignSystemR.string.currency_symbol),
                            product.price,
                        ),
                    style = MaterialTheme.typography.titleMedium,
                    color = ValyrianGold,
                )
            }

            if (product.quantityInCart > 0) {
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = LABEL_TOTAL_CART,
                        style = MaterialTheme.typography.labelLarge,
                        color = SilverHair,
                    )
                    Text(
                        text =
                            String.format(
                                Locale.forLanguageTag("pt-BR"),
                                "%s %.2f",
                                stringResource(DesignSystemR.string.currency_symbol),
                                product.price * product.quantityInCart,
                            ),
                        style = MaterialTheme.typography.titleLarge,
                        color = ValyrianGold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

            // Controles de quantidade e Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier =
                        Modifier
                            .width(TargaryenTheme.dimens.quantitySelectorWidth)
                            .height(TargaryenTheme.dimens.quantitySelectorHeight)
                            .border(
                                TargaryenTheme.dimens.borderSmall,
                                ValyrianGold,
                                RoundedCornerShape(TargaryenTheme.dimens.radiusMedium),
                            ).background(Obsidian),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onDecreaseQuantity(product) },
                        enabled = product.quantityInCart > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(DesignSystemR.string.content_desc_decrease),
                            tint =
                                if (product.quantityInCart > 0) {
                                    ValyrianGold
                                } else {
                                    SilverHair.copy(alpha = 0.5f)
                                },
                        )
                    }

                    Text(
                        text = product.quantityInCart.toString(),
                        color = TargaryenWhite,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    IconButton(
                        onClick = { onIncreaseQuantity(product) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(DesignSystemR.string.content_desc_increase),
                            tint = ValyrianGold,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceNormal))

                TargaryenButton(
                    text = stringResource(DesignSystemR.string.button_close),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "Catalog Detail Bottom Sheet - Dark Mode", showBackground = true)
@Composable
fun ProductDetailBottomSheetPreview() {
    val mockProduct =
        ProductUiModel(
            id = "5",
            name = "Banquete de Aegon",
            description =
                "Uma seleção rústica e farta de pães artesanais de Westeros " +
                    "servidos quentes com geleia de frutas silvestres e manteiga trufada.",
            price = 35.0,
            category = CatalogCategories.ROYAL_FEAST,
            quantityInCart = 2,
        )
    TargaryenTheme {
        ProductDetailBottomSheet(
            product = mockProduct,
            onDismiss = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onToggleFavorite = {},
        )
    }
}

private const val FAVORITE_ICON_SCALE = 1.5f
