package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailBottomSheet(
    item: PurchaseHistoryItem,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", LocalLocale.current.platformLocale)
    val formattedDate = formatter.format(Date(item.dateMillis))

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CharcoalBlack,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceLarge),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = ValyrianGold,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceMedium))
                Text(
                    text = "Recibo Real ${item.id}",
                    style = MaterialTheme.typography.titleLarge,
                    color = ValyrianGold,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DimmedGold,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                Text(
                    text = "Status: ${item.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimmedGold,
                )
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            Text(
                text = "Itens do Banquete:",
                style = MaterialTheme.typography.titleMedium,
                color = SilverHair,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
            Text(
                text = item.itemsSummary,
                style = MaterialTheme.typography.bodyLarge,
                color = TargaryenWhite,
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            HorizontalDivider(color = SilverHair.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverHair,
                )
                Text(
                    text = "Total: R$ ${
                        String.format(
                            LocalLocale.current.platformLocale,
                            "%.2f",
                            item.totalPrice,
                        )
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = ValyrianGold,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
        }
    }
}

@Preview(name = "Purchase Detail Sheet")
@Composable
fun PurchaseDetailBottomSheetPreview() {
    TargaryenTheme {
        PurchaseDetailBottomSheet(
            item =
                PurchaseHistoryItem(
                    id = "ORD-001",
                    dateMillis = System.currentTimeMillis(),
                    totalPrice = 125.50,
                    itemsSummary = "2x Dragonstone Brew, 1x Banquete de Aegon",
                    status = "Entregue nas Chamas",
                ),
            onDismissRequest = {},
        )
    }
}
