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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R

@Composable
fun ProfileAvatarImageDialogs(
    showAvatarOptions: MutableState<Boolean>,
    croppingImageUri: MutableState<Uri?>,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onUploadCroppedPhoto: (Uri) -> Unit,
) {
    if (showAvatarOptions.value) {
        AlertDialog(
            onDismissRequest = { showAvatarOptions.value = false },
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
                    showAvatarOptions.value = false
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
                        showAvatarOptions.value = false
                        onLaunchCamera()
                    }) {
                        Text(
                            stringResource(R.string.profile_photo_source_camera),
                            color = ValyrianGold,
                        )
                    }
                    Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                    TextButton(onClick = { showAvatarOptions.value = false }) {
                        Text(stringResource(R.string.profile_btn_cancel), color = SilverHair)
                    }
                }
            },
            containerColor = CharcoalBlack,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = TargaryenTheme.dimens.spaceExtraSmall,
        )
    }

    croppingImageUri.value?.let { uri ->
        ProfileCropImageDialog(
            imageUri = uri,
            onConfirm = { croppedUri ->
                onUploadCroppedPhoto(croppedUri)
                croppingImageUri.value = null
            },
            onDismiss = {
                croppingImageUri.value = null
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileAvatarImageDialogsPreview() {
    val showAvatarOptions = remember { mutableStateOf(true) }
    val croppingImageUri = remember { mutableStateOf<Uri?>(null) }
    TargaryenTheme {
        ProfileAvatarImageDialogs(
            showAvatarOptions = showAvatarOptions,
            croppingImageUri = croppingImageUri,
            onLaunchCamera = {},
            onLaunchGallery = {},
            onUploadCroppedPhoto = {},
        )
    }
}
