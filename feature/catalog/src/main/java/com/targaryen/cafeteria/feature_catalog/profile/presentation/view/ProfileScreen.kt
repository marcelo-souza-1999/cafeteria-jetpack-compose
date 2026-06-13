package com.targaryen.cafeteria.feature_catalog.profile.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.presentation.intent.ProfileIntent
import com.targaryen.cafeteria.feature_catalog.profile.presentation.state.ProfileState
import com.targaryen.cafeteria.feature_catalog.profile.presentation.util.ProfileUtils
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileAvatarImageDialogs
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileAvatarSection
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileDeleteWarningFancyDialog
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileErrorFancyDialog
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileInfoForm
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileSecurityBottomSheet
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.ProfileSecuritySection
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.PurchaseDetailBottomSheet
import com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components.PurchaseHistoryCard
import com.targaryen.cafeteria.feature_catalog.profile.presentation.viewmodel.ProfileViewModel

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
    var showAvatarOptions by remember { mutableStateOf(false) }
    var croppingImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    croppingImageUri = uri
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
                        croppingImageUri = tempUri
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
                            showAvatarOptions = true
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

    ProfileAvatarImageDialogs(
        showAvatarOptions = showAvatarOptions,
        croppingImageUri = croppingImageUri,
        onDismissAvatarOptions = { showAvatarOptions = false },
        onDismissCropping = { croppingImageUri = null },
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
