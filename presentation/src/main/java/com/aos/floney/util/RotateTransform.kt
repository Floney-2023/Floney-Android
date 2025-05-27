package com.letspl.oceankeeper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        if (angle == 0f) {
            return source
        }

        try {
            val matrix = Matrix()
            matrix.postRotate(angle)

            return Bitmap.createBitmap(
                source,
                0,
                0,
                source.width,
                source.height,
                matrix,
                true
            )
        } catch (e: Exception) {
            Timber.e(e, "Error rotating image")
            return source
        }
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

    // 새로운 메소드: URI에서 직접 회전 처리된 비트맵을 생성
    fun createRotatedBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // URI에서 직접 회전 각도 얻기
            val angle = getRotationAngleFromExif(context, uri).toFloat()

            // 회전 적용
            return if (angle != 0f) {
                val matrix = Matrix()
                matrix.postRotate(angle)
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating rotated bitmap from URI")
            return null
        }
    }
}
