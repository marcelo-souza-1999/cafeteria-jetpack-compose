package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold

@Composable
fun TargaryenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = BloodRed,
            contentColor = ValyrianGold,
            disabledContainerColor = BloodRed.copy(alpha = 0.5f),
            disabledContentColor = ValyrianGold.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ValyrianGold,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TargaryenButtonPreviewLight() {
    TargaryenTheme() {
        TargaryenButton(
            text = "Reivindicar",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TargaryenButtonPreviewDark() {
    TargaryenTheme() {
        TargaryenButton(
            text = "Reivindicar",
            onClick = {}
        )
    }
}
