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
    return try {
      FirebaseAnalytics.getInstance(activity.applicationContext)
    } catch (e: Exception) {
      null
    }
  }
  
  fun trackEvent(
    event: String,
    properties: List<String> = mutableListOf(),
    values: List<String> = mutableListOf()
  ) {
    val analytics = getAnalyticsObject()
    if (analytics != null) {
      val bundle = Bundle()
      for ((index, property) in properties.withIndex()) {
        bundle.putString(property, values[index])
      }
      Log.i(TAG, event)
      if (!TextUtils.isEmpty(userPrefs.phoneNumber)) {
        analytics.setUserProperty(CUSTOM_PHONE_NUMBER, userPrefs.phoneNumber)
      }
      analytics.logEvent(event, bundle)
    }
  }
}

const val EVENT_PLACE_BID = "bid_place"
const val EVENT_ACCEPT_BID = "bid_accept"
const val EVENT_EDIT_BID = "bid_edit"
const val EVENT_SEARCH_LOAD = "search_load"
const val EVENT_SEARCH_SAVED_LOAD = "search_saved_load"
const val EVENT_SEARCH_LOCAL = "search_local"
const val EVENT_SEARCH_ERROR = "search_error"
const val EVENT_LIST_HEADER = "list_header"
const val EVENT_LIST_ITEM = "list_item"
const val EVENT_EDIT_ROUTE = "edit_route"
const val EVENT_TRIP_STATUS_HISTORY = "trip_status_history"
const val EVENT_PAYMENT_SUMMARY = "trip_payment_summary"
const val EVENT_OTP_SEND = "otp_send"
const val EVENT_OTP_RESEND = "otp_resend"
const val EVENT_OTP_VERIFIED = "otp_verify"
const val EVENT_POD_VIEWED = "pod_view"
const val EVENT_POD_UPLOAD= "pod_upload"
const val EVENT_CALL_HELPLINE= "helpline"
const val EVENT_BID_INLINE_PROMPT="Bid_in_line_prompt"
const val EVENT_BID_REVISE_PROMPT="Bid_revise_prompt"
const val EVENT_REVISE_BID_INTENT="revise_bid_intent"

const val PROPERTY_TRANSACTION_ID = "transaction_id"
const val PROPERTY_ORIGIN = "origin"
const val PROPERTY_DESTINATION = "destination"
const val PROPERTY_TRUCK_TYPE = "truck_type"
const val PROPERTY_NUM_RESULTS = "num_result"
const val PROPERTY_TRANSACTION_TYPE = "type"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_ITEM = "item"
const val PROPERTY_STATUS = "status"

const val VALUE_BID = "bid"
const val VALUE_TRIP = "trip"
const val VALUE_LOAD = "load"
const val VALUE_ACTIVE = "active"
const val VALUE_CONFIRMED = "confirmed"
const val VALUE_LOST = "lost"
const val VALUE_LOAD_INFO = "info_load"
const val VALUE_NO_RESULTS = "no_results"
const val VALUE_PROFILE = "profile"
const val VALUE_ADVANCE_PENDING = "advance pending"
const val VALUE_BALANCE_PENDING = "balance pending"
const val VALUE_INTRANSIT = "intransit"
const val VALUE_COMPLETED = "completed"
const val VALUE_SUCCESS = "success"
const val VALUE_FAILURE = "failure"

const val CUSTOM_PHONE_NUMBER = "phone_number"