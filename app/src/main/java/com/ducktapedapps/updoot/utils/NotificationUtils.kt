package com.ducktapedapps.updoot.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ducktapedapps.updoot.R
import kotlin.random.Random


fun createNotification(title: String, message: String, context: Context,id:Int=Random.nextInt()) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context)
    }
    buildNotificationAndShow(title, message, context,id)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        notificationManager!!.createNotificationChannel(this)
    }
}

private fun buildNotificationAndShow(title: String, message: String, context: Context,id:Int) {
    val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
    NotificationManagerCompat.from(context).notify(id, builder.build())
}