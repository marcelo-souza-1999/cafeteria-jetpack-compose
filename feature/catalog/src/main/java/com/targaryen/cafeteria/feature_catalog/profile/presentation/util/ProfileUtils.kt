package com.targaryen.cafeteria.feature_catalog.profile.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import java.io.File
import java.io.FileOutputStream

object ProfileUtils {
    private const val JPEG_QUALITY = 90

    val AVATAR_PRESETS =
        listOf(
            "preset_dragon",
            "preset_crown",
            "preset_sword",
            "preset_emblem",
        )

    @Composable
    fun getAvatarModel(photoUrl: String?): Any? {
        if (photoUrl == null) return null
        return when (photoUrl) {
            "preset_dragon" -> com.targaryen.cafeteria.core_designsystem.R.drawable.img_avatar_dragon
            "preset_crown" -> com.targaryen.cafeteria.core_designsystem.R.drawable.img_avatar_crown
            "preset_sword" -> com.targaryen.cafeteria.core_designsystem.R.drawable.img_avatar_sword
            "preset_emblem" -> com.targaryen.cafeteria.core_designsystem.R.drawable.img_avatar_emblem
            else -> photoUrl
        }
    }

    fun saveBitmapToTempFile(
        context: Context,
        bitmap: Bitmap,
    ): Uri? {
        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
        return try {
            val out = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.flush()
            out.close()
            Uri.fromFile(tempFile)
        } catch (e: java.io.FileNotFoundException) {
            Log.e("ProfileUtils", "Falha ao salvar bitmap: arquivo nao encontrado", e)
            null
        } catch (e: java.io.IOException) {
            Log.e("ProfileUtils", "Erro de I/O ao salvar bitmap no cache", e)
            null
        }
    }

    fun loadBitmapFromUri(
        context: Context,
        uri: Uri,
    ): Bitmap? =
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val result = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            result
        } catch (e: java.io.IOException) {
            Log.e("ProfileUtils", "Erro ao carregar bitmap da Uri", e)
            null
        }

    fun cropBitmap(
        originalBitmap: Bitmap,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        containerSizePx: Float,
        cropSize: Int = 500,
    ): Bitmap {
        val croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBitmap)

        canvas.drawColor(Color.BLACK)

        val matrix = Matrix()

        val originalWidth = originalBitmap.width.toFloat()
        val originalHeight = originalBitmap.height.toFloat()

        val baseScale = Math.max(cropSize / originalWidth, cropSize / originalHeight)
        val finalScale = baseScale * scale

        val dx = (cropSize - originalWidth * finalScale) / 2f + (offsetX / containerSizePx) * cropSize
        val dy = (cropSize - originalHeight * finalScale) / 2f + (offsetY / containerSizePx) * cropSize

        matrix.postScale(finalScale, finalScale)
        matrix.postTranslate(dx, dy)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(originalBitmap, matrix, paint)

        return croppedBitmap
    }
}
