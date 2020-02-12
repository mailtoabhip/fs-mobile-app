package com.delhivery.axle.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

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

  @Inject lateinit var userPrefs: UserPrefs

  override fun onCreate() {
    AndroidInjection.inject(this)
    super.onCreate()
  }

  override fun onNewToken(fcmToken: String) {
    super.onNewToken(fcmToken)
    Log.d("DelhiveryFCMService", fcmToken)
    userPrefs.fcmTokenGenerated = true
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    remoteMessage.let { sendNotification(it) }
  }

  private fun sendNotification(remoteMessage: RemoteMessage) {
    val notificationBuilder: Builder = if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
      buildNotificationChannel()
      Builder(this, DEFAULT_NOTIFICATION_CHANNEL)
    } else {
      Builder(this)
    }
    val notificationId = remoteMessage.data["notification_service_notification_id"] ?: ""
    val notificationType = remoteMessage.data["notification_type"] ?: ""
    val transactions = remoteMessage.data["transaction_ids"] ?: ""
    val preferredTransactionId = remoteMessage.data["transaction_id"] ?: ""

    remoteMessage.notification?.let {
      val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val intent = Intent(this, HomeActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(ARGS_NOTIFICATION_ID, notificationId)
        putExtra(ARGS_NOTIFICATION_TYPE, notificationType)
        putExtra(ARGS_TRANSACTION_IDS, transactions)
        putExtra(ARGS_PREFERRED_TRANSACTION_ID, preferredTransactionId)
      }
      val pendingIntent = PendingIntent.getActivity(
          this, 0, intent, PendingIntent.FLAG_ONE_SHOT
      )
      val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
      notificationBuilder.setAutoCancel(true)
          .setDefaults(Notification.DEFAULT_ALL)
          .setWhen(System.currentTimeMillis())
          .setLargeIcon(largeIcon)
          .setSmallIcon(R.drawable.ic_notification)
          .setContentTitle(it.title)
          .setContentText(it.body)
          .setStyle(NotificationCompat.BigTextStyle().bigText(it.body))
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)
          .setSound(soundUri)

      with(NotificationManagerCompat.from(this)) {
        notify(notificationId.hashCode(), notificationBuilder.build())
      }
    }
  }

  @RequiresApi(VERSION_CODES.O)
  private fun buildNotificationChannel() {
    val notificationChannel = NotificationChannel(
        DEFAULT_NOTIFICATION_CHANNEL, "Axle Notifications",
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationChannel.description = "Default Channel"
    notificationChannel.enableVibration(true)
    notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
    val notificationManager: NotificationManager? =
      getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
    notificationManager?.createNotificationChannel(notificationChannel)
  }

}

private const val DEFAULT_NOTIFICATION_CHANNEL = "axle_notification_channel"
const val ARGS_NOTIFICATION_ID = "args_notification_id"
const val ARGS_TRANSACTION_IDS = "transaction_ids"
const val ARGS_PREFERRED_TRANSACTION_ID = "preferred_transaction_id"
const val ARGS_NOTIFICATION_TYPE = "notification_type"
const val ARGS_NOTIFICATION_KEY = "notification_service_notification_id"