@file:Suppress("MatchingDeclarationName")

package com.targaryen.cafeteria.feature_catalog.profile.presentation.view.components

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.targaryen.cafeteria.core_designsystem.theme.CharcoalBlack
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.profile.presentation.util.ProfileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface CropImageState {
    data object Loading : CropImageState

    data class Success(
        val bitmap: Bitmap,
    ) : CropImageState

    data object Error : CropImageState
}

@Composable
fun ProfileCropImageDialog(
    imageUri: Uri,
    onConfirm: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val cropImageState by produceState<CropImageState>(
        initialValue = CropImageState.Loading,
        key1 = imageUri,
        key2 = context,
    ) {
        value =
            withContext(Dispatchers.IO) {
                try {
                    val loadedBitmap = ProfileUtils.loadBitmapFromUri(context, imageUri)
                    if (loadedBitmap != null) {
                        CropImageState.Success(loadedBitmap)
                    } else {
                        CropImageState.Error
                    }
                } catch (e: java.io.IOException) {
                    Log.e("ProfileCropImage", "IOException loading bitmap from URI", e)
                    CropImageState.Error
                } catch (e: SecurityException) {
                    Log.e("ProfileCropImage", "SecurityException loading bitmap from URI", e)
                    CropImageState.Error
                }
            }
    }

    when (val state = cropImageState) {
        is CropImageState.Loading -> {
            Dialog(onDismissRequest = onDismiss) {
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .background(CharcoalBlack, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ValyrianGold)
                }
            }
        }
        is CropImageState.Error -> {
            LaunchedEffect(Unit) {
                onDismiss()
            }
        }
        is CropImageState.Success -> {
            val bitmap = state.bitmap
            val scale = remember { mutableFloatStateOf(1f) }
            val offsetX = remember { mutableFloatStateOf(0f) }
            val offsetY = remember { mutableFloatStateOf(0f) }

            val containerSizeDp = 280.dp
            val containerSizePx = with(density) { containerSizeDp.toPx() }

            Dialog(onDismissRequest = onDismiss) {
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
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCropImageDialogPreview() {
    TargaryenTheme {
        ProfileCropImageDialog(
            imageUri = Uri.EMPTY,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
