package com.targaryen.cafeteria.feature_catalog.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_catalog.presentation.model.ProductUiModel

private const val EMPTY_STATE_TEXT = "Nenhuma infusão encontrada nos Sete Reinos."

@Composable
fun ProductGrid(
    products: List<ProductUiModel>,
    onProductClick: (ProductUiModel) -> Unit,
    onAddToCart: (ProductUiModel) -> Unit,
    onIncreaseQuantity: (ProductUiModel) -> Unit,
    onDecreaseQuantity: (ProductUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(TargaryenTheme.dimens.spaceLarge),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = EMPTY_STATE_TEXT,
                color = SilverHair,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = TargaryenTheme.dimens.spaceNormal,
                vertical = TargaryenTheme.dimens.spaceSmall
            ),
            horizontalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceMedium)
        ) {
            items(products, key = { product -> product.id }) { product ->
                ProductCard(
                    product = product,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity
                )
            }
        }
    }
}

@Preview(name = "Product Grid - Dark Mode")
@Composable
fun ProductGridPreview() {
    val mockProducts = listOf(
        ProductUiModel(
            id = "1",
            name = "Targaryen Blood Blend",
            description = "Café extraído sob o fogo do dragão, encorpado e com notas intensas.",
            price = 12.5,
            category = "Bebidas Quentes",
            quantityInCart = 1
        ),
        ProductUiModel(
            id = "2",
            name = "Valyrian Velvet Latte",
            description = "Uma combinação sedosa e mística de café espresso robusto.",
            price = 15.0,
            category = "Bebidas Quentes",
            quantityInCart = 0
        )
    )
    TargaryenTheme {
        ProductGrid(
            products = mockProducts,
            onProductClick = {},
            onAddToCart = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
