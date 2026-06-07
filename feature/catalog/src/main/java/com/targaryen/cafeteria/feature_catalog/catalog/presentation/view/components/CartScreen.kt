package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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

private const val SHIPPING_FREE_THRESHOLD = 50.0
private const val SHIPPING_COST = 5.90

data class CartScreenActions(
    val onIncreaseQuantity: (ProductUiModel) -> Unit,
    val onDecreaseQuantity: (ProductUiModel) -> Unit,
    val onRemoveProduct: (ProductUiModel) -> Unit,
    val onProductClick: (ProductUiModel) -> Unit,
    val onCheckoutClick: () -> Unit,
)

@Composable
fun CartScreen(
    products: List<ProductUiModel>,
    actions: CartScreenActions,
    modifier: Modifier = Modifier,
) {
    val subtotal = products.sumOf { product -> product.price * product.quantityInCart }
    val shippingTribute = if (subtotal > SHIPPING_FREE_THRESHOLD || subtotal == 0.0) 0.0 else SHIPPING_COST
    val total = subtotal + shippingTribute

    if (products.isEmpty()) {
        EmptyCartState()
    } else {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(Obsidian)
                    .padding(TargaryenTheme.dimens.spaceNormal),
        ) {
            Text(
                text = stringResource(DesignSystemR.string.cart_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ValyrianGold,
                modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceExtraSmall),
            )

            Text(
                text = stringResource(DesignSystemR.string.cart_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SilverHair.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceNormal),
            )

            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceNormal),
                contentPadding = PaddingValues(bottom = TargaryenTheme.dimens.spaceNormal),
            ) {
                items(
                    items = products,
                    key = { product -> product.id },
                ) { product ->
                    CartItemCard(
                        product = product,
                        onIncrease = { actions.onIncreaseQuantity(product) },
                        onDecrease = { actions.onDecreaseQuantity(product) },
                        onRemove = { actions.onRemoveProduct(product) },
                        onProductClick = { actions.onProductClick(product) },
                    )
                }
            }

            HorizontalDivider(
                color = SilverHair.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = TargaryenTheme.dimens.spaceNormal),
            )

            // Resumo de Banquetes
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = TargaryenTheme.dimens.spaceNormal),
                verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceSmall),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(DesignSystemR.string.cart_label_subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverHair,
                    )
                    Text(
                        text = String.format(Locale.US, "R$ %.2f", subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TargaryenWhite,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(DesignSystemR.string.cart_label_shipping),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverHair,
                    )
                    Text(
                        text =
                            if (shippingTribute == 0.0) {
                                stringResource(DesignSystemR.string.cart_label_shipping_free)
                            } else {
                                String.format(Locale.US, "R$ %.2f", shippingTribute)
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (shippingTribute == 0.0) ValyrianGold else TargaryenWhite,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(DesignSystemR.string.cart_label_total),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ValyrianGold,
                    )
                    Text(
                        text = String.format(Locale.US, "R$ %.2f", total),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ValyrianGold,
                    )
                }
            }

            Button(
                onClick = actions.onCheckoutClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = BloodRed,
                        contentColor = TargaryenWhite,
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(DesignSystemR.string.cart_button_checkout),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TargaryenWhite,
                )
            }
        }
    }
}

@Composable
fun CartItemCard(
    product: ProductUiModel,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onProductClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = DragonScale,
                contentColor = TargaryenWhite,
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { onProductClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = (product.imageUrl ?: "").replace(" ", "%20"),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Obsidian),
                )

                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceMedium))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ValyrianGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            product.category.replaceFirstChar { char ->
                                if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverHair.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                    Text(
                        text = String.format(Locale.US, "R$ %.2f", product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TargaryenWhite,
                    )
                }
            }

            Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .background(BloodRed.copy(alpha = 0.2f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Diminuir quantidade",
                        tint = BloodRed,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Text(
                    text = product.quantityInCart.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TargaryenWhite,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center,
                )

                IconButton(
                    onClick = onIncrease,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .background(BloodRed.copy(alpha = 0.2f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar quantidade",
                        tint = BloodRed,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir item",
                        tint = BloodRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCartState() {
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
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = ValyrianGold,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = stringResource(DesignSystemR.string.cart_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ValyrianGold,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = stringResource(DesignSystemR.string.cart_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Cart Screen - Empty State")
@Composable
fun CartScreenEmptyPreview() {
    TargaryenTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Obsidian) {
            CartScreen(
                products = emptyList(),
                actions =
                    CartScreenActions(
                        onIncreaseQuantity = {},
                        onDecreaseQuantity = {},
                        onRemoveProduct = {},
                        onProductClick = {},
                        onCheckoutClick = {},
                    ),
            )
        }
    }
}

@Preview(name = "Cart Screen - With Items")
@Composable
fun CartScreenItemsPreview() {
    val mockProducts =
        listOf(
            ProductUiModel(
                id = "1",
                name = "Café Expresso Valíria",
                description = "Café expresso ultra forte, escuro e denso como obsidian.",
                price = 8.50,
                imageUrl = "${B2_PRODUCTS_BASE}img_cafe_valiria.png",
                category = "bebidas",
                quantityInCart = 2,
            ),
            ProductUiModel(
                id = "2",
                name = "Mocha Fogo de Dragão",
                description = "Café mocha premium com um toque picante de pimenta caiena e canela.",
                price = 14.90,
                imageUrl = "${B2_PRODUCTS_BASE}img_mocha_dragao.png",
                category = "bebidas",
                quantityInCart = 1,
            ),
            ProductUiModel(
                id = "3",
                name = "Croissant de Obsidiana",
                description = "Croissant folhado feito com carvão ativado e recheio de chocolate belga.",
                price = 10.50,
                imageUrl = "${B2_PRODUCTS_BASE}img_croissant_obsidiana.png",
                category = "comidas",
                quantityInCart = 3,
            ),
        )

    TargaryenTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Obsidian) {
            CartScreen(
                products = mockProducts,
                actions =
                    CartScreenActions(
                        onIncreaseQuantity = {},
                        onDecreaseQuantity = {},
                        onRemoveProduct = {},
                        onProductClick = {},
                        onCheckoutClick = {},
                    ),
            )
        }
    }
}

private const val B2_PRODUCTS_BASE =
    "https://f005.backblazeb2.com/file/cafeteria-targaryen-assets/products/"
