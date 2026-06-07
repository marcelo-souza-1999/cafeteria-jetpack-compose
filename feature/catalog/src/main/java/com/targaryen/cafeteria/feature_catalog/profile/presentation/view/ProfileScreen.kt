package com.targaryen.cafeteria.feature_catalog.profile.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.presentation.intent.ProfileIntent
import com.targaryen.cafeteria.feature_catalog.profile.presentation.state.ProfileState
import com.targaryen.cafeteria.feature_catalog.profile.presentation.util.ProfileUtils
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileAvatarSection
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileDeleteWarningFancyDialog
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileErrorFancyDialog
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileInfoForm
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileSecurityBottomSheet
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileSecuritySection
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.PurchaseDetailBottomSheet
import com.targaryen.cafeteria.feature_catalog.profile.presentation.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onLogout()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onIntent = { intent -> viewModel.onIntent(intent) },
        modifier = modifier,
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackBarHostState.showSnackbar(msg)
            onIntent(ProfileIntent.ClearMessages)
        }
    }

    val context = LocalContext.current
    val showAvatarOptions = remember { mutableStateOf(false) }
    val croppingImageUri = remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    croppingImageUri.value = uri
                }
            },
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            onResult = { bitmap ->
                if (bitmap != null) {
                    val tempUri = ProfileUtils.saveBitmapToTempFile(context, bitmap)
                    if (tempUri != null) {
                        croppingImageUri.value = tempUri
                    }
                }
            },
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Obsidian),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = TargaryenTheme.dimens.spaceLarge,
                    vertical = TargaryenTheme.dimens.spaceLarge,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ProfileAvatarSection(
                    photoUrl = uiState.photoUrl,
                    name = uiState.name,
                    isUpdatingPhoto = uiState.isUpdatingPhoto,
                    onPhotoClick = {
                        if (!uiState.isUpdatingPhoto) {
                            showAvatarOptions.value = true
                        }
                    },
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

                ProfileInfoForm(
                    name = uiState.name,
                    email = uiState.email,
                    onChangeName = { name -> onIntent(ProfileIntent.ChangeName(name)) },
                    onChangeEmail = { email -> onIntent(ProfileIntent.ChangeEmail(email)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.profile_choose_heraldry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverHair,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceSmall),
                ) {
                    ProfileUtils.AVATAR_PRESETS.forEach { presetUrl ->
                        Box(
                            modifier =
                                Modifier
                                    .size(TargaryenTheme.dimens.avatarPreset)
                                    .border(
                                        BorderStroke(
                                            if (uiState.photoUrl ==
                                                presetUrl
                                            ) {
                                                TargaryenTheme.dimens.borderMedium
                                            } else {
                                                TargaryenTheme.dimens.borderSmall
                                            },
                                            if (uiState.photoUrl == presetUrl) {
                                                ValyrianGold
                                            } else {
                                                SilverHair.copy(
                                                    alpha = 0.3f,
                                                )
                                            },
                                        ),
                                        CircleShape,
                                    ).clickable(enabled = !uiState.isUpdatingPhoto) {
                                        onIntent(ProfileIntent.UpdatePhoto(presetUrl))
                                    },
                        ) {
                            AsyncImage(
                                model = ProfileUtils.getAvatarModel(presetUrl),
                                contentDescription = stringResource(R.string.profile_desc_preset),
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            }

            item {
                Text(
                    text = stringResource(R.string.profile_banquet_chronicles_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = TargaryenTheme.dimens.spaceSmall),
                    textAlign = TextAlign.Start,
                )
            }

            if (uiState.purchaseHistory.isEmpty()) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = TargaryenTheme.dimens.spaceMedium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = SilverHair.copy(alpha = 0.4f),
                            modifier = Modifier.size(TargaryenTheme.dimens.spaceMassive),
                        )
                        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                        Text(
                            text = stringResource(R.string.profile_empty_history_desc),
                            color = SilverHair,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                val itemsToShow =
                    if (uiState.isHistoryExpanded) {
                        uiState.purchaseHistory
                    } else {
                        uiState.purchaseHistory.take(
                            2,
                        )
                    }

                items(itemsToShow) { item ->
                    PurchaseHistoryCard(item) {
                        onIntent(ProfileIntent.ShowPurchaseDetail(item))
                    }
                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceNormal))
                }

                if (uiState.purchaseHistory.size > 2) {
                    item {
                        TextButton(onClick = { onIntent(ProfileIntent.ToggleHistoryExpansion) }) {
                            Text(
                                text =
                                    if (uiState.isHistoryExpanded) {
                                        stringResource(R.string.profile_history_collapse)
                                    } else {
                                        "${stringResource(
                                            R.string.profile_btn_see_all_records,
                                        )} (${uiState.purchaseHistory.size})"
                                    },
                                color = DimmedGold,
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
                ProfileSecuritySection(
                    onResetPasswordClick = { onIntent(ProfileIntent.ShowSecurityModal) },
                    onDeleteAccountClick = { onIntent(ProfileIntent.ShowDeleteDialog) },
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ValyrianGold)
            }
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    AvatarImageDialogs(
        showAvatarOptions = showAvatarOptions,
        croppingImageUri = croppingImageUri,
        onLaunchCamera = { cameraLauncher.launch(null) },
        onLaunchGallery = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onUploadCroppedPhoto = { croppedUri ->
            onIntent(ProfileIntent.UploadPhoto(croppedUri))
        },
    )

    ProfileSecurityAndErrorDialogs(
        uiState = uiState,
        onIntent = onIntent,
    )
}

@Composable
private fun ProfileSecurityAndErrorDialogs(
    uiState: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
) {
    uiState.error?.let { error ->
        ProfileErrorFancyDialog(
            message = error,
            onDismiss = { onIntent(ProfileIntent.ClearMessages) },
        )
    }

    if (uiState.showSecurityModal) {
        ProfileSecurityBottomSheet(
            onDismissRequest = { onIntent(ProfileIntent.DismissSecurityModal) },
            onChangePassword = { old, new -> onIntent(ProfileIntent.ChangePassword(old, new)) },
        )
    }

    if (uiState.showDeleteConfirmation) {
        ProfileDeleteWarningFancyDialog(
            onConfirm = { onIntent(ProfileIntent.ConfirmDeleteAccount) },
            onDismiss = { onIntent(ProfileIntent.DismissDeleteDialog) },
        )
    }

    uiState.selectedPurchase?.let { purchase ->
        PurchaseDetailBottomSheet(
            item = purchase,
            onDismissRequest = { onIntent(ProfileIntent.DismissPurchaseDetail) },
        )
    }
}

@Composable
private fun PurchaseHistoryCard(
    item: PurchaseHistoryItem,
    onClick: () -> Unit,
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(item.dateMillis))

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
                    text = "Banquete de $formattedDate",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                )
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (item.status) {
                            "Aprovado" -> ValyrianGold
                            "Pendente" -> SilverHair.copy(alpha = 0.7f)
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

@Composable
fun CropImageDialog(
    imageUri: Uri,
    onConfirm: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val bitmap = remember(imageUri) { ProfileUtils.loadBitmapFromUri(context, imageUri) }

    if (bitmap == null) {
        onDismiss()
        return
    }

    val scale = remember { mutableFloatStateOf(1f) }
    val offsetX = remember { mutableFloatStateOf(0f) }
    val offsetY = remember { mutableFloatStateOf(0f) }

    val containerSizeDp = 280.dp
    val containerSizePx = with(density) { containerSizeDp.toPx() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
            border =
                BorderStroke(
                    TargaryenTheme.dimens.borderSmall,
                    ValyrianGold.copy(alpha = 0.2f),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceMedium),
        ) {
            Column(
                modifier = Modifier.padding(TargaryenTheme.dimens.spaceMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.profile_crop_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ValyrianGold,
                )
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

                Box(
                    modifier =
                        Modifier
                            .size(containerSizeDp)
                            .clip(CircleShape)
                            .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, _, _ ->
                                        offsetX.floatValue += pan.x
                                        offsetY.floatValue += pan.y
                                    }
                                }.graphicsLayer(
                                    scaleX = scale.floatValue,
                                    scaleY = scale.floatValue,
                                    translationX = offsetX.floatValue,
                                    translationY = offsetY.floatValue,
                                ),
                    )
                }

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))
                Text(
                    text = stringResource(R.string.profile_crop_zoom),
                    color = SilverHair,
                    style = MaterialTheme.typography.labelSmall,
                )
                Slider(
                    value = scale.floatValue,
                    onValueChange = { newScale -> scale.floatValue = newScale },
                    valueRange = 1f..3f,
                    colors =
                        androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = ValyrianGold,
                            activeTrackColor = ValyrianGold,
                            inactiveTrackColor = SilverHair.copy(alpha = 0.2f),
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.profile_btn_cancel), color = SilverHair)
                    }
                    Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                    TextButton(onClick = {
                        val croppedBitmap =
                            ProfileUtils.cropBitmap(
                                originalBitmap = bitmap,
                                scale = scale.floatValue,
                                offsetX = offsetX.floatValue,
                                offsetY = offsetY.floatValue,
                                containerSizePx = containerSizePx,
                                cropSize = 500,
                            )
                        val croppedUri = ProfileUtils.saveBitmapToTempFile(context, croppedBitmap)
                        if (croppedUri != null) {
                            onConfirm(croppedUri)
                        }
                    }) {
                        Text(stringResource(R.string.profile_btn_confirm), color = ValyrianGold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarImageDialogs(
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
        CropImageDialog(
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

private const val ONE_DAY_MILLIS = 86400000L
private const val TWO_DAYS_MILLIS = 186400000L
private const val MOCK_PRICE_HIGH = 125.50
private const val MOCK_PRICE_MID = 45.00
private const val MOCK_PRICE_LOW = 10.00

@Preview(name = "Profile Screen - Default State")
@Composable
fun ProfileScreenPreview() {
    val mockState =
        ProfileState(
            name = "Aegon Targaryen",
            email = "aegon.conqueror@valyria.com",
            photoUrl = "preset_dragon",
            purchaseHistory =
                listOf(
                    PurchaseHistoryItem(
                        "ORD-001",
                        System.currentTimeMillis(),
                        MOCK_PRICE_HIGH,
                        "2x Dragonstone Brew, 1x Banquete de Aegon",
                        "Entregue nas Chamas",
                    ),
                    PurchaseHistoryItem(
                        "ORD-002",
                        System.currentTimeMillis() - ONE_DAY_MILLIS,
                        MOCK_PRICE_MID,
                        "3x Valyrian Velvet Latte",
                        "Preparando nos Fornos",
                    ),
                    PurchaseHistoryItem(
                        "ORD-003",
                        System.currentTimeMillis() - TWO_DAYS_MILLIS,
                        MOCK_PRICE_LOW,
                        "1x Café Negro",
                        "Entregue nas Chamas",
                    ),
                ),
        )

    TargaryenTheme {
        ProfileScreenContent(
            uiState = mockState,
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
