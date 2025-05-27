package com.aos.floney.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import timber.log.Timber
import java.io.InputStream

object ImgFileMaker {
    // Uri 로부터 실제 파일 경로를 얻어옴
    fun getFullPathFromUri(ctx: Context, fileUri: Uri?): String? {
        if (fileUri == null) return null

        // PhotoPicker URIs need special handling
        if (fileUri.toString().startsWith("content://media/picker")) {
            // For PhotoPicker URIs, we need to create a temporary file
            try {
                val inputStream = ctx.contentResolver.openInputStream(fileUri)
                if (inputStream != null) {
                    // Create a temporary file to save the image
                    val tempFile = File.createTempFile(
                        "photo_picker_",
                        ".jpg",
                        ctx.cacheDir
                    )

                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    inputStream.close()

                    return tempFile.absolutePath
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling PhotoPicker URI")
                return null
            }
        }

        // Fall back to the old method for other URI types
        var fullPath: String? = null
        val column = "_data"
        var cursor = ctx.contentResolver.query(fileUri, null, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    var document_id = cursor.getString(0)
                    if (document_id == null) {
                        for (i in 0 until cursor.columnCount) {
                            if (column.equals(cursor.getColumnName(i), ignoreCase = true)) {
                                fullPath = cursor.getString(i)
                                break
                            }
                        }
                    } else {
                        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
                        cursor.close()
                        val projection = arrayOf(column)
                        try {
                            cursor = ctx.contentResolver.query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                projection,
                                MediaStore.Images.Media._ID + " = ? ",
                                arrayOf(document_id),
                                null
                            )
                            if (cursor != null && cursor.moveToFirst()) {
                                fullPath = cursor.getString(cursor.getColumnIndexOrThrow(column))
                            }
                        } finally {
                            cursor?.close()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error processing cursor")
                }
            } else {
                // If the cursor is empty but we still have a valid URI
                // try to get the content directly as a bitmap and save to a file
                return createTempFileFromUri(ctx, fileUri)
            }
            cursor?.close()
        } else {
            // If cursor is null, try to get the content directly
            return createTempFileFromUri(ctx, fileUri)
        }

        return fullPath
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): String? {
        try {
            val bitmap = createBitmapFromUri(context, uri) ?: return null

            val tempFile = File.createTempFile(
                "image_",
                ".jpg",
                context.cacheDir
            )

            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            return tempFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error creating temp file from URI")
            return null
        }
    }

    fun createBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
