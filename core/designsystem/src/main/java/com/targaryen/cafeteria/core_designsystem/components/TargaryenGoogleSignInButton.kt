package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.R
import com.targaryen.cafeteria.core_designsystem.theme.GoogleDarkButtonBorder
import com.targaryen.cafeteria.core_designsystem.theme.GoogleDarkButtonContainer
import com.targaryen.cafeteria.core_designsystem.theme.GoogleDarkButtonContent
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme

@Composable
fun TargaryenGoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerColor = GoogleDarkButtonContainer
    val borderColor = GoogleDarkButtonBorder
    val contentColor = GoogleDarkButtonContent

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        border = BorderStroke(TargaryenTheme.dimens.borderSmall, borderColor),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.5f),
            ),
        shape = RoundedCornerShape(TargaryenTheme.dimens.radiusMedium),
        contentPadding =
            PaddingValues(
                horizontal = TargaryenTheme.dimens.buttonContentHorizontal,
                vertical = TargaryenTheme.dimens.buttonContentVertical,
            ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = null,
            modifier = Modifier,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceMedium))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun GoogleSignInButtonPreviewContent() {
    TargaryenGoogleSignInButton(
        text = "Fazer login com o Google",
        onClick = {},
    )
}

@Preview(showBackground = false)
@Composable
private fun GoogleSignInButtonPreviewLight() {
    TargaryenTheme {
        GoogleSignInButtonPreviewContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleSignInButtonPreviewDark() {
    TargaryenTheme {
        GoogleSignInButtonPreviewContent()
    }
}
