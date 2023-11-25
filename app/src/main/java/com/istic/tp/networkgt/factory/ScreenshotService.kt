package com.istic.tp.networkgt.factory

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScreenshotService: Service() {

    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("ScreenshotService", "Je suis bien ici 1")
        if (intent == null) {
            return Service.START_STICKY
        }

        if (mediaProjection == null) {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK)
            val data = intent.getParcelableExtra<Intent>("data")

            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
            takeScreenshot()
        }

        return Service.START_STICKY
    }
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaProjection?.stop()
        super.onDestroy()
    }

    private fun takeScreenshot() {
        val imageReader = ImageReader.newInstance(
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels,
            ImageFormat.RGB_565,
            2
        )

        val virtualDisplay = mediaProjection?.createVirtualDisplay(
            "Screenshot",
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels,
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader?.acquireLatestImage()
            if (image != null) {
                saveScreenshot(image)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveScreenshot(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.RGB_565
        )

        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)

        try {
            // Save the bitmap to a file
            val screenshotsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val screenshotFile = File(screenshotsDir, "screenshot.png")
            val fos = FileOutputStream(screenshotFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            image.close()

            Toast.makeText(this, "Capture d'écran sauvegardée : ${screenshotFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}