package com.delhivery.axle.utils

import android.util.Log
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * FCM utility
 *
 **
 */
@ActivityScope
class FCMUtils @Inject constructor(
  private val userPrefs: UserPrefs
) {

  /**
   * Generate FCM token
   */
  fun generateToken(completedAction: (token: String) -> Unit) {
      FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
          if (!task.isSuccessful) {
              Log.w("FCMUtils", "FCM getInstanceId failed", task.exception)
              userPrefs.fcmTokenGenerated = false
          } else {
              completedAction(task.result ?: "")
          }
      }
  }

}