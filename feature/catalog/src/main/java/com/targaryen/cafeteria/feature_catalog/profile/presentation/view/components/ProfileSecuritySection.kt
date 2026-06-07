package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R

@Composable
fun ProfileSecuritySection(
    onResetPasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = DragonScale),
        border = BorderStroke(TargaryenTheme.dimens.borderSmall, ValyrianGold.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceMedium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_section_security_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ValyrianGold,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ValyrianGold,
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = TargaryenTheme.dimens.spaceMedium),
                ) {
                    OutlinedButton(
                        onClick = onResetPasswordClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ValyrianGold),
                        border = BorderStroke(TargaryenTheme.dimens.borderSmall, ValyrianGold),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ValyrianGold,
                            modifier = Modifier.padding(end = TargaryenTheme.dimens.spaceSmall),
                        )
                        Text(stringResource(R.string.profile_btn_reset_password), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

                    OutlinedButton(
                        onClick = onDeleteAccountClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BloodRed),
                        border = BorderStroke(TargaryenTheme.dimens.borderSmall, BloodRed),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = BloodRed,
                            modifier = Modifier.padding(end = TargaryenTheme.dimens.spaceSmall),
                        )
                        Text(
                            stringResource(R.string.profile_btn_delete_account),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Profile Security Section")
@Composable
fun ProfileSecuritySectionPreview() {
    TargaryenTheme {
        ProfileSecuritySection(
            onResetPasswordClick = {},
            onDeleteAccountClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
