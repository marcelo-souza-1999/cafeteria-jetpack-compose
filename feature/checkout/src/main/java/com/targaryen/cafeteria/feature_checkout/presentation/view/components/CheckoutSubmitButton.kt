package com.targaryen.cafeteria.feature_checkout.presentation.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.feature_checkout.R
import com.targaryen.cafeteria.feature_checkout.presentation.intent.CheckoutIntent
import com.targaryen.cafeteria.feature_checkout.presentation.state.CheckoutState

@Composable
fun CheckoutSubmitButton(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onIntent(CheckoutIntent.OnSubmitPayment) },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = TargaryenTheme.dimens.spaceNormal)
                .height(TargaryenTheme.dimens.buttonHeightLarge),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = BloodRed,
                contentColor = Color.White,
            ),
        enabled = !state.isCreatingPreference,
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium),
    ) {
        if (state.isCreatingPreference) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(TargaryenTheme.dimens.iconSizeMedium),
            )
        } else {
            Text(
                text = stringResource(R.string.checkout_action_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutSubmitButtonPreview() {
    TargaryenTheme {
        CheckoutSubmitButton(
            state = CheckoutState(isCreatingPreference = false),
            onIntent = {},
        )
    }
}
