package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.core_designsystem.R as DesignSystemR

@Composable
internal fun FavoritesScreenStub(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Obsidian)
                .padding(TargaryenTheme.dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(TargaryenTheme.dimens.iconSizeExtraLarge),
            tint = BloodRed,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
        Text(
            text = stringResource(DesignSystemR.string.favorites_stub_title),
            style = MaterialTheme.typography.titleLarge,
            color = ValyrianGold,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
        Text(
            text = stringResource(DesignSystemR.string.favorites_stub_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = SilverHair,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Favorites Screen Stub - Dark Mode")
@Composable
fun FavoritesScreenStubPreview() {
    TargaryenTheme {
        FavoritesScreenStub()
    }
}
