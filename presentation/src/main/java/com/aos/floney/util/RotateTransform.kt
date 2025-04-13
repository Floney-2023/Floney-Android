package com.letspl.oceankeeper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.IOException


object RotateTransform {
    // 이미지 파일로부터 Exif 정보를 읽어서 회전 각도를 얻는 함수
    fun getRotationAngle(imagePath: String): Int {
        var rotationAngle = 0
        try {
            val exif = ExifInterface(imagePath)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotationAngle = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotationAngle = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotationAngle = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotationAngle
    }

    // 이미지 회전
    fun rotateImage(context: Context, source: Bitmap, angle: Float, imageUri: Uri? = null): Bitmap? {
        val matrix = Matrix()
        matrix.setRotate(angle, source.width.toFloat(), source.height.toFloat())
        val bitmap =
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    fun getRotationAngleFromExif(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

}