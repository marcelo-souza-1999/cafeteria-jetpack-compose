package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.presentation.util.ProfileUtils

@Composable
fun ProfileAvatarSection(
    photoUrl: String?,
    name: String,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUpdatingPhoto: Boolean = false,
) {
    Box(
        modifier = modifier.size(TargaryenTheme.dimens.avatarLarge + TargaryenTheme.dimens.spaceMedium),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(TargaryenTheme.dimens.avatarLarge)
                    .border(
                        BorderStroke(
                            TargaryenTheme.dimens.borderThick,
                            Brush.sweepGradient(listOf(BloodRed, ValyrianGold, BloodRed)),
                        ),
                        CircleShape,
                    ).padding(TargaryenTheme.dimens.spaceExtraSmall),
            contentAlignment = Alignment.Center,
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ProfileUtils.getAvatarModel(photoUrl),
                    contentDescription = stringResource(R.string.profile_desc_royal_effigy),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(CharcoalBlack, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = ValyrianGold,
                    )
                }
            }

            if (isUpdatingPhoto) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Color.Black
                                    .copy(alpha = 0.5f),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(TargaryenTheme.dimens.spaceLarge),
                        color = ValyrianGold,
                        strokeWidth = TargaryenTheme.dimens.borderSmall,
                    )
                }
            }
        }

        IconButton(
            onClick = { if (!isUpdatingPhoto) onPhotoClick() },
            enabled = !isUpdatingPhoto,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .background(CharcoalBlack, CircleShape)
                    .size(TargaryenTheme.dimens.avatarSelectorIcon)
                    .border(TargaryenTheme.dimens.borderSmall, ValyrianGold, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = stringResource(R.string.profile_desc_change_photo),
                tint = ValyrianGold,
                modifier = Modifier.size(TargaryenTheme.dimens.iconSizeMedium),
            )
        }
    }
}

@Preview(name = "Avatar - With Image")
@Composable
fun ProfileAvatarSectionPreviewWithImage() {
    TargaryenTheme {
        ProfileAvatarSection(
            photoUrl = "preset_dragon",
            name = "Rhaenyra Targaryen",
            onPhotoClick = {},
        )
    }
}

@Preview(name = "Avatar - Without Image")
@Composable
fun ProfileAvatarSectionPreviewWithoutImage() {
    TargaryenTheme {
        ProfileAvatarSection(
            photoUrl = null,
            name = "Daemon Targaryen",
            onPhotoClick = {},
        )
    }
}
