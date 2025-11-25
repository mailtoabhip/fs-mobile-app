package com.delhivery.axle.fcm

import android.Manifest.permission
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import dagger.android.DaggerActivity
import javax.inject.Inject
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.tokenExpiryHandling.RefreshTokenWorker
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import java.util.concurrent.TimeUnit


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
  //@Inject lateinit var analyticsUtil : AnalyticsUtil

  private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val notificationType = intent?.extras?.get(ARGS_NOTIFICATION_TYPE)
//      analyticsUtil.trackEvent(
//              EVENT_NOTIFICATION_DISMISS,
//              mutableListOf(PROPERTY_USER_ID, PROPERTY_NOTIFICATION_TYPE, PROPERTY_OVERALL_PERFORMANCE),
//              mutableListOf(userPrefs.userId(), notificationType.toString() , userPrefs.userPerformance)
//      )
      unregisterReceiver(this)
    }
  }

  override fun onCreate() {
    AndroidInjection.inject(this)
    super.onCreate()
  }

  override fun onNewToken(fcmToken: String) {
    super.onNewToken(fcmToken)
    MoEFireBaseHelper.getInstance().passPushToken(applicationContext,fcmToken)
    userPrefs.fcmTokenGenerated = true
    userPrefs.moengageFcmTokenGenerated =true
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d("prefs","started")
    if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data)){
      if(!userPrefs.requestedDeletion)
          MoEFireBaseHelper.getInstance().passPushPayload(applicationContext, remoteMessage.data)
      }else{
      remoteMessage.let { sendNotification(it) }
      }
  }

  private fun sendNotification(remoteMessage: RemoteMessage) {
    Log.d("prefs","send notifi. started $remoteMessage")
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

    //For inventory use
    val vehicleNumber = remoteMessage.data["vehicle_number"] ?: ""

    //For pricing
    val pricingId = remoteMessage.data["pricing_id"] ?: ""
    val pricingSortKey = remoteMessage.data["pricing_sort_key"] ?: ""
    val notificationFrom =  remoteMessage.data["notification_from"] ?: ""
    val offerId =  remoteMessage.data["offer_id"] ?: ""



    val n= remoteMessage.notification
    if(n==null){
      val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(false)
        .setRequiresCharging(false)
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .apply {
          if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            setRequiresDeviceIdle(false)
        }
        .build()

      val repeatingRequest
              = PeriodicWorkRequestBuilder<RefreshTokenWorker>(16, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            RefreshTokenWorker.INITIAL_BACKOFF_DELAY_MINUTES,
            TimeUnit.MINUTES
        )
        .build()

      WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        RefreshTokenWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.REPLACE,
        repeatingRequest)
    }

    if(!userPrefs.requestedDeletion)
        remoteMessage.notification?.let {
          val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
          val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ARGS_NOTIFICATION_ID, notificationId)
            putExtra(ARGS_NOTIFICATION_TYPE, notificationType)
            putExtra(ARGS_TRANSACTION_IDS, transactions)
            putExtra(ARGS_PREFERRED_TRANSACTION_ID, preferredTransactionId)
            putExtra(ARGS_VEHICLE_NUMBER, vehicleNumber)
            putExtra(ARGS_PRICING_ID, pricingId)
            putExtra(ARGS_PRICING_SORT_KEY, pricingSortKey)
            putExtra(ARGS_NOTIFICATION_FROM, notificationFrom)
            putExtra(ARGS_OFFER_ID, offerId)


          }

          val pendingIntent =
            PendingIntent.getActivity(
              this, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
              else PendingIntent.FLAG_ONE_SHOT
            )


          /*Dismiss Notification*/
          val intentDismissNotification = Intent("NOTIFICATION_DELETED_ACTION").apply {
            putExtra(ARGS_NOTIFICATION_TYPE, notificationType)
          }
          val pendingIntentDismiss  =
            PendingIntent.getBroadcast(this , 0, intentDismissNotification , if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            else PendingIntent.FLAG_ONE_SHOT)

          ContextCompat.registerReceiver(this,receiver, IntentFilter("NOTIFICATION_DELETED_ACTION"),ContextCompat.RECEIVER_NOT_EXPORTED)

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
              .setDeleteIntent(pendingIntentDismiss)
              .setAutoCancel(true)
              .setSound(soundUri)

          with(NotificationManagerCompat.from(this)) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this@DelhiveryFCMService,
                permission.POST_NOTIFICATIONS
              ) == PackageManager.PERMISSION_GRANTED)
              ||Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            )
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
const val ARGS_PREFERRED_TRANSACTION_ID = "transaction_id"
const val ARGS_NOTIFICATION_TYPE = "notification_type"
const val ARGS_NOTIFICATION_KEY = "notification_service_notification_id"
const val ARGS_DEEPLINK_TYPE = "deeplink_type"
const val ARGS_DEEPLINK_ID = "deeplink_id"
const val ARGS_VEHICLE_NUMBER = "vehicle_number"
const val ARGS_PRICING_ID= "pricing_id"
const val ARGS_PRICING_SORT_KEY= "pricing_sort_key"
const val ARGS_NOTIFICATION_FROM= "notification_from"
const val ARGS_OFFER_ID= "offer_id"