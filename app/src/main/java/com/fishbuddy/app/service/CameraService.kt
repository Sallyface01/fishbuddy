package com.fishbuddy.app.service

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor

/** Wraps CameraX for photo capture. */
class CameraService(private val context: Context) {

    fun takePhoto(
        outputFile: File,
        executor: Executor,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val imageCapture = ImageCapture.Builder().build()

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            imageCapture.takePicture(
                outputOptions, executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onSuccess(output.savedUri ?: Uri.fromFile(outputFile))
                    }
                    override fun onError(exc: ImageCaptureException) {
                        onError(exc.message ?: "Capture failed")
                    }
                }
            )
        }, executor)
    }

    companion object {
        /** Compress bitmap to JPEG under maxSize bytes. */
        fun compressImage(data: ByteArray, maxWidth: Int = 2048, quality: Int = 70): ByteArray {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
            val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxWidth.toFloat() / bitmap.height, 1f)
            val scaled = if (ratio < 1f)
                android.graphics.Bitmap.createScaledBitmap(
                    bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true
                ) else bitmap

            val out = java.io.ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
            if (scaled !== bitmap) scaled.recycle()
            return out.toByteArray()
        }

        fun makeThumbnail(data: ByteArray, size: Int = 200): ByteArray {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
            val ratio = minOf(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height, 1f)
            val scaled = android.graphics.Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true
            )
            val out = java.io.ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, out)
            scaled.recycle()
            return out.toByteArray()
        }
    }
}
