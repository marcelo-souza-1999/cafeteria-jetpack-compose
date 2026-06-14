package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R

@Suppress("LongParameterList")
@Composable
fun ProfileAvatarImageDialogs(
    showAvatarOptions: Boolean,
    croppingImageUri: Uri?,
    onDismissAvatarOptions: () -> Unit,
    onDismissCropping: () -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onUploadCroppedPhoto: (Uri) -> Unit,
) {
    if (showAvatarOptions) {
        AlertDialog(
            onDismissRequest = onDismissAvatarOptions,
            title = {
                Text(
                    text = stringResource(R.string.profile_photo_source_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.profile_photo_source_desc),
                    color = SilverHair,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissAvatarOptions()
                    onLaunchGallery()
                }) {
                    Text(
                        stringResource(R.string.profile_photo_source_gallery),
                        color = ValyrianGold,
                    )
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onDismissAvatarOptions()
                        onLaunchCamera()
                    }) {
                        Text(
                            stringResource(R.string.profile_photo_source_camera),
                            color = ValyrianGold,
                        )
                    }
                    Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                    TextButton(onClick = onDismissAvatarOptions) {
                        Text(stringResource(R.string.profile_btn_cancel), color = SilverHair)
                    }
                }
            },
            containerColor = CharcoalBlack,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = TargaryenTheme.dimens.spaceExtraSmall,
        )
    }

    croppingImageUri?.let { uri ->
        ProfileCropImageDialog(
            imageUri = uri,
            onConfirm = { croppedUri ->
                onUploadCroppedPhoto(croppedUri)
                onDismissCropping()
            },
            onDismiss = onDismissCropping,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileAvatarImageDialogsPreview() {
    TargaryenTheme {
        ProfileAvatarImageDialogs(
            showAvatarOptions = true,
            croppingImageUri = null,
            onDismissAvatarOptions = {},
            onDismissCropping = {},
            onLaunchCamera = {},
            onLaunchGallery = {},
            onUploadCroppedPhoto = {},
        )
    }
}
