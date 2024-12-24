package com.delhivery.axle.utils

import android.util.Log
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.gms.tasks.Task
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
      FirebaseMessaging.getInstance().deleteToken() // Clears the current token
          .addOnCompleteListener { task: Task<Void?> ->
              if (task.isSuccessful) {
                  FirebaseMessaging.getInstance().token // Retrieves a new token
                      .addOnCompleteListener { newTask: Task<String?> ->
                          if (!newTask.isSuccessful) {
                              Log.w("FCMUtils", "FCM getInstanceId failed", task.exception)
                              userPrefs.fcmTokenGenerated = false
                          }else{
                              completedAction(newTask.result ?: "")
                          }
                      }
              }else{
                  userPrefs.fcmTokenGenerated = false
              }
          }

  }

}