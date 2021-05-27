package com.delhivery.axle.utils

import android.content.Intent
import android.net.Uri
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.Config.AxleCallSupport
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

@ActivityScope
class ContactUtils @Inject constructor(private val activity: DaggerAppCompatActivity) {

  fun openGmail(
    subject: String = "",
    body: String = "",
    receiver: String
  ): Boolean {
    return try {
      val emailIntent = Intent(
          Intent.ACTION_SENDTO, Uri.fromParts(
          "mailto", receiver, null
      )
      )
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
      activity.startActivity(Intent.createChooser(emailIntent, null))
      true
    } catch (e: Exception) {
      false
    }
  }

  fun callDriver(phoneNumber:String = "") = try {
    val callIntent = Intent(Intent.ACTION_CALL).apply {
      data = Uri.parse("tel:$phoneNumber")
    }
    activity.startActivity(callIntent)
    true
  } catch (e: Exception) {
    false
  }

  fun callHelpline() = try {
    val callIntent = Intent(Intent.ACTION_CALL).apply {
      data = Uri.parse("tel:$AxleCallSupport")
    }
    activity.startActivity(callIntent)
    true
  } catch (e: Exception) {
    false
  }

  fun openURL(url: String): Boolean {
    return try {
      val intent = Intent(Intent.ACTION_VIEW)
      var modifiedUrl = url
      if (!url.startsWith("https://") && !url.startsWith("http://")) modifiedUrl = "http://$url"
      intent.data = Uri.parse(modifiedUrl)
      activity.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

}