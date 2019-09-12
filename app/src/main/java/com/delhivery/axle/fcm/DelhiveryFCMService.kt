package com.delhivery.axle.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat.Builder
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

  private lateinit var notificationManager: NotificationManager

  override fun onNewToken(fcmToken: String) {
    super.onNewToken(fcmToken)
    Log.d("DelhiveryFCMService", fcmToken)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    buildNotificationChannel()
    remoteMessage.notification?.let { sendNotification(it) }
  }

  private fun buildNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = NotificationChannel(
          DEFAULT_NOTIFICATION_CHANNEL, "My Default Notifications",
          NotificationManager.IMPORTANCE_HIGH
      )
      // Configure the notification channel.
      notificationChannel.description = "Default Channel"
      notificationChannel.enableVibration(true)
      notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
      notificationManager.createNotificationChannel(notificationChannel)
    }
  }

  private fun sendNotification(notification: RemoteMessage.Notification) {
    val notificationBuilder: Builder
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val intent = Intent(this, HomeActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationBuilder = Builder(this, DEFAULT_NOTIFICATION_CHANNEL)
      notificationBuilder.setAutoCancel(true)
          .setDefaults(Notification.DEFAULT_ALL)
          .setWhen(System.currentTimeMillis())
          .setSmallIcon(R.drawable.ic_launcher)
          .setContentText(notification.body)
          .setSound(soundUri)
    } else {
      notificationBuilder = Builder(this)
      notificationBuilder.setAutoCancel(true)
          .setDefaults(Notification.DEFAULT_ALL)
          .setWhen(System.currentTimeMillis())
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentText(notification.body)
          .setSound(soundUri)
          .setContentIntent(pendingIntent)
    }
    notificationManager.notify(1, notificationBuilder.build());
  }

}

private const val DEFAULT_NOTIFICATION_CHANNEL = "default_notification_channel"