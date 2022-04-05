package com.delhivery.axle.utils

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Property
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
const val EVENT_POD_UPLOAD = "pod_upload"
const val EVENT_CALL_HELPLINE = "helpline"
const val EVENT_CALL_VENDOR_DESK ="call_vendorDesk"
const val EVENT_BID_INLINE_PROMPT = "Bid_in_line_prompt"
const val EVENT_BID_REVISE_PROMPT = "Bid_revise_prompt"
const val EVENT_REVISE_BID_INTENT  ="revise_bid_intent"
const val EVENT_SKIP_TUTORIAL = "skip_app_tutorial"
const val EVENT_VIEW_TUTORIAL = "view_app_tutorial"
const val EVENT_AUTO_LOGOUT = "auto_logout"
const val EVENT_USER_LOGOUT = "user_logout"
const val EVENT_SHOW_ADDITIONAL_LOADS = "show_additional_loads"
const val EVENT_VIEW_BIDS_SCREEN = "view_bids_screen"
const val EVENT_VIEW_LOST_BIDS = "view_lost_bids"
const val EVENT_VIEW_CONFIRMED_BIDS = "view_confirmed_bids"
const val EVENT_VIEW_ACTIVE_BIDS = "view_active_bids"
const val EVENT_VIEW_TRIPS = "view_trips"
const val EVENT_VIEW_ALL_TRIPS = "view_all_trips"
const val EVENT_VIEW_TRIPS_AWAITING_ARRIVAL = "view_trips_awaiting_arrival"
const val EVENT_VIEW_TRIPS_INTRANSIT = "view_trips_intransit"
const val EVENT_VIEW_TRIPS_AWAITING_POD = "view_trips_awaitingPOD"
const val EVENT_VIEW_TRIPS_AWAITING_LOADING = "view_trips_awaiting_loading"
const val EVENT_VIEW_TRIPS_AWAITING_UNLOADING = "view_trips_awaiting_unloading"
const val EVENT_VIEW_PAYMENT_SUMMARY = "view_payment_summary"
const val EVENT_VIEW_ADVANCE_PENDING = "view_advance_pending"
const val EVENT_VIEW_BALANCE_PENDING = "view_balance_pending"
const val EVENT_VIEW_RECOVERY_PENDING = "view_recovery_pending"
const val EVENT_FILTER_BALANCE_PENDING = "filter_balance_pending"
const val EVENT_DOWNLOAD_LEDGER = "download_ledger"
const val EVENT_SEARCH_TRIPS = "search_trips"
const val EVENT_VIEW_CHARGES = "view_charges"
const val EVENT_VIEW_DEDUCTIONS = "view_deductions"
const val EVENT_VIEW_PAYMENTS = "view_payments"
const val EVENT_VIEW_RECOVERIES_ADJUSTED = "view_recoveries_adjusted"
const val EVENT_FILTER_ALL_TRIPS = "filter_all_trips"
const val EVENT_FILTER_VEHICLE_TYPE = "filter_vehicle_type"
const val EVENT_FILTER_EXPRESS_LOADS = "filter_express_loads"
const val EVENT_UPDATE_APP = "update_app"
const val EVENT_UPDATE_CANCEL = "update_cancel"
const val EVENT_EDIT_PREFERENCES ="edit_preferences"
const val EVENT_ENTER_FIRST_OC = "enter_first_oc"
const val EVENT_CONFIRM_FIRST_ROUTE = "confirm_first_route"
const val EVENT_LOAD_SCROLL = "load_scroll"
const val EVENT_NOTIFICATION_OPEN = "notification_open_event"
const val EVENT_NOTIFICATION_RECEIVE = "notification_receive"
const val EVENT_NOTIFICATION_DISMISS = "notification_dismiss"
const val EVENT_APP_OPEN = "app_open"
const val EVENT_VIEW_TRIP_RECOVERY_ADJUSTMENT = "view_trip_recovery_adjustment"
const val EVENT_VIEW_TRIP_FUTURE_ADJUSTMENT = "view_trip_future_adjustment"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIPS = "view_change_payment_mode_trips"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIP_DETAILS = "view_change_payment_mode_trip_details"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_DONE = "view_change_payment_mode_done"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_CANCEL = "view_change_payment_mode_trip_cancel"
const val EVENT_DEEP_LINK_ADD_FUEL_PAYMENT = "deep_link_add_fuel_payment"
const val EVENT_ACTIVATE_TRUCK = "activate_truck"
const val EVENT_DEACTIVATE_TRUCK = "deactivate_truck"
const val EVENT_EDIT_TRUCK = "edit_truck"
const val EVENT_DELETE_TRUCK = "delete_truck"
const val EVENT_ADD_TRUCK = "add_truck"
const val EVENT_BANNER_CLICK_TOP = "click_add_truck_top_banner"
const val EVENT_BANNER_CLICK_SCROLL = "click_add_truck_scroll_banner"
const val EVENT_VIEW_MY_TRUCK = "view_my_truck"




const val PROPERTY_TRANSACTION_ID = "transaction_id"
const val PROPERTY_ORIGIN = "origin"
const val PROPERTY_DESTINATION = "destination"
const val PROPERTY_TRUCK_TYPE = "truck_type"
const val PROPERTY_NUM_RESULTS = "num_result"
const val PROPERTY_TRANSACTION_TYPE = "type"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_ITEM = "item"
const val PROPERTY_STATUS = "status"
const val PROPERTY_USER_ID = "user_id"
const val PROPERTY_ACTIVE_BIDS = "active_bids"
const val PROPERTY_CONFIRMED_BIDS = "confirmed_bids"
const val PROPERTY_LOST_BIDS = "lost_bids"
const val PROPERTY_ALL_TRIPS_COUNT = "all_trips_count"
const val PROPERTY_TRIPS_AWAITING_ARRIVAL_COUNT = "awaiting_arrival_count"
const val PROPERTY_TRIPS_INTRANSIT_COUNT = "intransit_count"
const val PROPERTY_TRIPS_AWAITING_POD_COUNT = "awaiting_pod_count"
const val PROPERTY_TRIPS_AWAITING_LOADING_COUNT = "awaiting_loading_count"
const val PROPERTY_TRIPS_AWAITING_UNLOADING_COUNT = "awaiting_unloading_count"
const val PROPERTY_TRIPS_PAYMENT_SUMMARY_COUNT = "payment_summary_count"
const val PROPERTY_ADVANCE_PENDING_COUNT = "advance_pending_count"
const val PROPERTY_BALANCE_PENDING_COUNT = "balance_pending_count"
const val PROPERTY_RECOVERY_PENDING_COUNT = "recovery_pending_count"
const val PROPERTY_LOADING_TIME = "loading_time"
const val PROPERTY_FILTER_SELECTED= "filter_selected"
const val PROPERTY_OPTION_SELECTED= "option_selected"
const val PROPERTY_DURATION_SELECTED = "duration_selected"
const val PROPERTY_DOWNLOADED_EMAILED_SELECTED = "downloaded_emailed_selected"
const val PROPERTY_EMAIL_ENTERED = "email_entered"
const val PROPERTY_MOBILE_NUMBER_ENTERED = "mobile_number_entered"
const val PROPERTY_ENTERED_VALUE = "entered_value"
const val PROPERTY_TRIP_ID = "trip_id"
const val PROPERTY_CHARGES_AMOUNT = "charges_amount"
const val PROPERTY_DEDUCTIONS_AMOUNT = "deductions_amount"
const val PROPERTY_PAYMENTS_AMOUNT = "payments_amount"
const val PROPERTY_RECOVERIES_ADJUSTED_AMOUNT= "recoveries_adjusted_amount"
const val PROPERTY_AMOUNT_TYPE = "pending_or_recovery_amount"
const val PROPERTY_PAGE = "page_from_which_called"
const val PROPERTY_LOADED_AFTER = "loaded_after_time"
const val PROPERTY_ONLY_SETTLED = "only_settled"
const val PROPERTY_CURRENT_VERSION = "current_app_version"
const val PROPERTY_LATEST_VERSION = "latest_app_version"
const val PROPERTY_ATTRIBUTE_CHANGED = "attribute_changed"
const val PROPERTY_ORIGIN_CITY_CAPTURED = "origin_city_captured"
const val PROPERTY_ROUTE_PREFERENCES = "route_preferences_selected"
const val PROPERTY_DEMAND_TYPE = "demand_type"
const val PROPERTY_OVERALL_PERFORMANCE = "overall_performance"
const val PROPERTY_LOAD_SCROLL = "load_scroll"
const val PROPERTY_TIME_LAPSE = "time_lapse"
const val PROPERTY_NOTIFICATION_TYPE = "notification_type"
const val PROPERTY_PAGE_NAME = "page_name"
const val PROPERTY_OTP_SEND_COUNT = "otp_sent_count"
const val PROPERTY_HOUR_OF_DAY = "hour_of_day"
const val PROPERTY_NO_OF_SCROLLS = "no_of_scrolls"
const val PROPERTY_AMOUNT_OF_RECOVERY_ADJUSTED = "amt_of_recovery_adjusted"
const val PROPERTY_TRIP_AGAINST_RECOVERY_ADJUSTED = "trip_against_recovery_adjusted"
const val PROPERTY_RECD_TRIP_IDS = "recd_trip_ids"
const val PROPERTY_TIME_SINCE_LAST_LOGIN = "time_since_last_login"
const val PROPERTY_CHANGE_PAYMENT_DIESEL_PAYOUT = "change_payment_diesel_payout"
const val PROPERTY_CHANGE_PAYMENT_DIESEL_CARD_NUMBER = "change_payment_diesel_card_number"
const val PROPERTY_INVENTORY_ID = "inventory_id"
const val PROPERTY_REASON = "reason_for_deactivating"



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
const val VALUE_DOWNLOADED = "downloaded"
const val VALUE_EMAILED = "emailed"
const val VALUE_PENDING_AMOUNT = "pending_amount"
const val VALUE_RECOVERY_AMOUNT = "recovery_amount"
const val VALUE_REFRESH = "load"
const val VALUE_SCROLL = "scroll"
const val VALUE_DESTINATION_STATE = "destination_state"
const val VALUE_COMPLETE_ROUTE = "complete_route"
const val CUSTOM_PHONE_NUMBER = "phone_number"
const val VALUE_FUTURE_ADJUSTMENT = "future_adjustment"
const val VALUE_RECOVERY_ADJUSTMENT = "recovery_adjustment"
const val VALUE_NOTIFICATION = "notification"
const val VALUE_DEEP_LINKING = "deep_linking"
const val VALUE_ADD_TRUCK_TOP_BANNER = "add_truck_top_banner"
const val VALUE_ADD_TRUCK_SCROLL_BANNER= "add_truck_scroll_banner"
const val VALUE_ADD_TRUCK_PAGE = "add_truck_page"
const val VALUE_ADD_TRUCK_ONBOARDING_PAGE = "add_truck_onboarding_page"