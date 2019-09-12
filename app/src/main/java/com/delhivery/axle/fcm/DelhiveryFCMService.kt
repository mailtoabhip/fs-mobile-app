package com.delhivery.axle.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Implementation of [FirebaseMessagingService] to handle notifications
 *
 **
 */
class DelhiveryFCMService : FirebaseMessagingService() {

  override fun onNewToken(fcmToken: String) {
    super.onNewToken(fcmToken)
    Log.d("DelhiveryFCMService", fcmToken)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    buildNotificationChannel()
    remoteMessage.notification?.let { sendNotification(it) }
  }

  /**
   *
   * Configure the notification channel
   *
   */
  private fun buildNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = NotificationChannel(
          DEFAULT_NOTIFICATION_CHANNEL, "My Default Notifications",
          NotificationManager.IMPORTANCE_HIGH
      )
      notificationChannel.description = "Default Channel"
      notificationChannel.enableVibration(true)
      notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
      // Register the channel with the system
      val notificationManager: NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(notificationChannel)
    }
  }

  /**
   *
   * Configure the notification
   *
   */
  private fun sendNotification(notification: RemoteMessage.Notification) {
    val notificationBuilder: Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Builder(this, DEFAULT_NOTIFICATION_CHANNEL)
    } else {
      Builder(this)
    }
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val intent = Intent(this, HomeActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    val largeIcon = BitmapFactory.decodeResource(
        resources, R.mipmap.ic_launcher
    )

    notificationBuilder.setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.ic_notificaiton)
        .setLargeIcon(largeIcon)
        .setContentTitle(notification.title)
        .setContentText(notification.body)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setSound(soundUri)

    with(NotificationManagerCompat.from(this)) {
      notify(1, notificationBuilder.build())
    }
  }

}

private const val DEFAULT_NOTIFICATION_CHANNEL = "delhivery_notification_channel"