package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PurchaseHistoryCard(
    item: PurchaseHistoryItem,
    onClick: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate =
        remember(item.dateMillis) {
            Instant
                .ofEpochMilli(item.dateMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = BorderStroke(TargaryenTheme.dimens.borderSmall, ValyrianGold.copy(alpha = 0.15f)),
    ) {
        Column(modifier = Modifier.padding(TargaryenTheme.dimens.spaceMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_banquet_title_format, formattedDate),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                )
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (item.status) {
                            stringResource(R.string.status_approved) -> ValyrianGold
                            stringResource(R.string.status_pending) -> SilverHair.copy(alpha = 0.7f)
                            else -> BloodRed
                        },
                )
            }
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
            Text(
                text = item.itemsSummary,
                color = TargaryenWhite,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchaseHistoryCardPreview() {
    TargaryenTheme {
        PurchaseHistoryCard(
            item =
                PurchaseHistoryItem(
                    "ORD-001",
                    System.currentTimeMillis(),
                    MOCK_PRICE,
                    "2x Dragonstone Brew, 1x Banquete de Aegon",
                    "Entregue nas Chamas",
                ),
            onClick = {},
        )
    }
}

private const val MOCK_PRICE = 125.50
