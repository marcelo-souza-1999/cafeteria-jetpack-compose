package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import android.net.Uri

data class ProfileAvatarDialogsActions(
    val onDismissAvatarOptions: () -> Unit,
    val onDismissCropping: () -> Unit,
    val onLaunchCamera: () -> Unit,
    val onLaunchGallery: () -> Unit,
    val onUploadCroppedPhoto: (Uri) -> Unit,
)
