package com.delhivery.axle.utils

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Helper class to log app analytics events
 *
 **
 */
@ActivityScope
class AnalyticsUtil @Inject constructor(
  private val activity: DaggerAppCompatActivity,
  private val userPrefs: UserPrefs
) {

  val TAG = "Firebase events"

  private fun getAnalyticsObject(): FirebaseAnalytics? {
    try {
      return FirebaseAnalytics.getInstance(activity.applicationContext)
    } catch (e: Exception) {
      return null
    }
  }

  fun setUserPhone(phone: String) {
    val analytics = getAnalyticsObject()
    if (analytics != null) {
      analytics.setUserProperty(CUSTOM_PHONE_NUMBER, phone)
    }
  }

  fun trackEvent(
    event: String,
    properties: List<String>,
    values: List<String>
  ) {
    val analytics = getAnalyticsObject()
    if (analytics != null) {
      val bundle = Bundle()
      var index = 0
      for (property in properties) {
        bundle.putString(property, values[index])
        index++
      }
      Log.i(TAG, event)
      if (!TextUtils.isEmpty(userPrefs.phoneNumber)) {
        analytics.setUserProperty(CUSTOM_PHONE_NUMBER, userPrefs.phoneNumber)
      }
      analytics.logEvent(event, bundle)
    }
  }
}

val EVENT_PLACE_BID = "bid_place"
val EVENT_ACCEPT_BID = "bid_accept"
val EVENT_EDIT_BID = "bid_edit"
val EVENT_SEARCH_LOAD = "search_load"
val EVENT_SEARCH_SAVED_LOAD = "search_saved_load"
val EVENT_SEARCH_LOCAL = "search_local"
val EVENT_SEARCH_ERROR = "search_error"
val EVENT_LIST_HEADER = "list_header"
val EVENT_LIST_ITEM = "list_item"
val EVENT_EDIT_ROUTE = "edit_route"
val EVENT_TRIP_STATUS_HISTORY = "trip_status_history"
val EVENT_PAYMENT_SUMMARY = "trip_payment_summary"
val EVENT_OTP_SEND = "otp_send"
val EVENT_OTP_RESEND = "otp_resend"
val EVENT_OTP_VERIFIED = "otp_verify"

val PROPERTY_TRANSACTION_ID = "transaction_id"
val PROPERTY_ORIGIN = "origin"
val PROPERTY_DESTINATION = "destination"
val PROPERTY_TRUCK_TYPE = "truck_type"
val PROPERTY_NUM_RESULTS = "num_result"
val PROPERTY_TRANSACTION_TYPE = "type"
val PROPERTY_SOURCE = "source"
val PROPERTY_ITEM = "item"

val VALUE_BID = "bid"
val VALUE_TRIP = "trip"
val VALUE_LOAD = "load"
val VALUE_ACTIVE = "active"
val VALUE_CONFIRMED = "confirmed"
val VALUE_LOST = "lost"
val VALUE_LOAD_INFO = "info_load"
val VALUE_NO_RESULTS = "no_results"
val VALUE_PROFILE = "profile"
val VALUE_ADVANCE_PENDING = "advance pending"
val VALUE_BALANCE_PENDING = "balance pending"
val VALUE_INTRANSIT = "intransit"
val VALUE_COMPLETED = "completed"

val CUSTOM_PHONE_NUMBER = "phone_number"