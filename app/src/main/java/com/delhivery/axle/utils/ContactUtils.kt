package com.delhivery.axle.utils

import android.content.Intent
import android.net.Uri
import com.delhivery.axle.injection.scope.ActivityScope
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

@ActivityScope
class ContactUtils @Inject constructor(private val activity: DaggerAppCompatActivity) {

  fun openGmail(
    subject: String = "",
    body: String = "",
    receiver: String
  ): Boolean {
    try {
      val emailIntent = Intent(
          Intent.ACTION_SENDTO, Uri.fromParts(
          "mailto", receiver, null
      )
      )
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
      activity.startActivity(Intent.createChooser(emailIntent, null))
      return true
    } catch (e: Exception) {
      return false
    }
  }
}