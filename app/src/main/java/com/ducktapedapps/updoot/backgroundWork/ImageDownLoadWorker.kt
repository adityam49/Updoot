package com.ducktapedapps.updoot.backgroundWork

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Images.Media
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.createNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

class ImageDownLoadWorker(private val context: Context, workParas: WorkerParameters) : CoroutineWorker(context, workParas) {
    companion object {
        const val URL_KEY = "url_key"
        private const val TAG = "ImageWorker"
    }

    override suspend fun doWork(): Result {
        val url = inputData.getString(URL_KEY)
        return if (url.isNullOrBlank()) {
            buildNotificationAndShow("No url found", context, finished = true)
            Result.failure()
        } else fetchFromUrlAndSave(url)
    }

    private suspend fun fetchFromUrlAndSave(url: String): Result {
        val notificationId = Random.nextInt()
        val request = Request.Builder()
                .url(url)
                .build()
        return try {
            buildNotificationAndShow("Downloading image...", context, notificationId, false)
            withContext(Dispatchers.IO) {
                val response = OkHttpClient().newCall(request).execute()
                val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                val savedImageUri = saveBitmapToStorage(bitmap, context.getString(R.string.app_name))
                buildNotificationAndShow("Download complete", context, notificationId, true, bitmap = bitmap, imageUri = savedImageUri)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            buildNotificationAndShow("Downloading failed! : ${e.message}", context, notificationId, true)
            Result.failure()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap, folderName: String): Uri? {
        val contentResolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folderName")
                put(Media.IS_PENDING, true)
                put(Media.MIME_TYPE, "image/png")
                put(Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(Media.DATE_TAKEN, System.currentTimeMillis())
            }
            val uri: Uri? = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, contentResolver.openOutputStream(uri))
                values.put(Media.IS_PENDING, false)
                contentResolver.update(uri, values, null, null)
            }
            uri
        } else {
            val directory = File(context.getExternalFilesDir(null).toString() + "${Environment.DIRECTORY_PICTURES}/$folderName")
            if (!directory.exists()) directory.mkdirs()
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = ContentValues().apply {
                put(Media.MIME_TYPE, "image/png")
                put(Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    put(Media.DATA, file.absolutePath)
            }
            contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream?.close()
    }

    private fun buildNotificationAndShow(
            title: String,
            context: Context,
            id: Int = Random.nextInt(),
            finished: Boolean,
            bitmap: Bitmap? = null,
            imageUri: Uri? = null
    ) {
        context.createNotificationChannel()
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
            setContentTitle(title)
            priority = NotificationCompat.PRIORITY_DEFAULT
            if (!finished) setProgress(0, 0, true)
            else {
                if (!imageUri?.toString().isNullOrBlank()) {
                    setAutoCancel(true)
                    val openImageIntent = Intent(Intent.ACTION_VIEW, imageUri)
                    val pendingIntent = PendingIntent.getActivities(context, 1, arrayOf(openImageIntent), PendingIntent.FLAG_ONE_SHOT)
                    setContentIntent(pendingIntent)
                    setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                }
            }
            NotificationManagerCompat.from(context).notify(id, this.build())
        }
    }
}