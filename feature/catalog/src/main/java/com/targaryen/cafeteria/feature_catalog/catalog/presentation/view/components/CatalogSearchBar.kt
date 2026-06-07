package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@Composable
fun CatalogSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = { text -> onQueryChange(text) },
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    width = TargaryenTheme.dimens.borderSmall,
                    color = ValyrianGold,
                    shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium),
                ),
        placeholder = {
            Text(
                text = stringResource(DesignSystemR.string.search_placeholder),
                color = SilverHair,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(DesignSystemR.string.search_icon_desc),
                tint = ValyrianGold,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(DesignSystemR.string.clear_icon_desc),
                        tint = ValyrianGold,
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TargaryenWhite),
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Obsidian,
                unfocusedContainerColor = Obsidian,
                focusedBorderColor = ValyrianGold,
                unfocusedBorderColor = ValyrianGold,
                cursorColor = ValyrianGold,
            ),
    )
}

@Preview(name = "Catalog Search Bar - Dark Mode", showBackground = true)
@Composable
fun CatalogSearchBarPreview() {
    TargaryenTheme {
        CatalogSearchBar(
            query = "Café",
            onQueryChange = {},
            onClearQuery = {},
        )
    }
}
