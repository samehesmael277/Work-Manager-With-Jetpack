package com.sameh.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class CustomWorkerWithNotification @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        try {
            setForeground(getForegroundInfo(applicationContext))
        } catch (e:Exception) {
            return Result.failure()
        }
        delay(10000)
        "CustomWorkerWithNotification success".toLogD()
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return getForegroundInfo(applicationContext)
    }
}

private fun getForegroundInfo(context: Context): ForegroundInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        ForegroundInfo(
            1,
            createNotification(context = context),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
        )
    } else {
        ForegroundInfo(
            1,
            createNotification(context = context)
        )
    }
}

private fun createNotification(context: Context): Notification {
    val channelId = "main_channel_id"
    val channelName = "Main Channel"

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Notification Title")
        .setContentText("This is my first notification.")
        .setOngoing(true)
        .setAutoCancel(true)

    return builder.build()
}