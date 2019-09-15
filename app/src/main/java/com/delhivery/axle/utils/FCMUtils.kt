package com.delhivery.axle.utils

import android.util.Log
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
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
    FirebaseInstanceId.getInstance()
        .instanceId
        .addOnCompleteListener(OnCompleteListener { task ->
          if (!task.isSuccessful) {
            Log.w("FCMUtils", "getInstanceId failed", task.exception)
            userPrefs.fcmTokenGenerated = false
            return@OnCompleteListener
          }

          // Get new Instance ID token
          completedAction(task.result?.token ?: "")
        })
  }

}