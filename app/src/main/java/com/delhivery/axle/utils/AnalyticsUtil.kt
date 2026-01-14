package com.delhivery.axle.utils
import android.util.Log
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.analytics.FirebaseAnalytics
import com.moengage.core.Properties
import com.moengage.core.analytics.MoEAnalyticsHelper
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

  val TAG = "Analytics events"
  val MOENGAGE_TAG = "Moengage events"


  private fun getAnalyticsObject(): FirebaseAnalytics? {
    return try {
      FirebaseAnalytics.getInstance(activity.applicationContext)
    } catch (e: Exception) {
      null
    }
  }

  fun moEngageTrackEvent(
    event: String,
    attributes: List<String> = mutableListOf(),
    values: List<String> = mutableListOf()
  ) {
    Log.i(MOENGAGE_TAG, event)
      val properties = Properties()
      for ((index, attribute) in attributes.withIndex()) {
        Log.i(MOENGAGE_TAG, attribute + ":" + values[index])
        properties.addAttribute(attribute, values[index])
      }
      properties.addAttribute(PROPERTY_PREVIOUS_SOURCE,userPrefs.userPreviousScreen)
      Log.i(MOENGAGE_TAG, PROPERTY_PREVIOUS_SOURCE + ":" + userPrefs.userPreviousScreen)
    MoEAnalyticsHelper.trackEvent(activity.applicationContext, event, properties)
  }

  fun moEngageUserAttribute(
    userAttribute: String,
    value: String
  ) {
    Log.i(MOENGAGE_TAG, userAttribute)
      MoEAnalyticsHelper.setUserAttribute(activity.applicationContext,userAttribute, value)
  }
}

const val EVENT_SEARCH_LOAD = "search_load"
const val EVENT_SEARCH_SAVED_LOAD = "search_saved_load"
const val EVENT_SEARCH_LOCAL = "search_local"
const val EVENT_SEARCH_ERROR = "search_error"
const val EVENT_LIST_ITEM = "list_item"
const val EVENT_EDIT_ROUTE = "edit_route"
const val EVENT_OTP_SEND = "otp_send"
const val EVENT_OTP_RESEND = "otp_resend"
const val EVENT_OTP_VERIFIED = "otp_verify"
const val EVENT_POD_VIEWED = "pod_view"
const val EVENT_POD_UPLOAD = "pod_upload"
const val EVENT_CALL_VENDOR_DESK = "call_vendorDesk"
const val EVENT_SKIP_TUTORIAL = "skip_app_tutorial"
const val EVENT_VIEW_TUTORIAL = "view_app_tutorial"
const val EVENT_TOKEN_EXPIRED_403 = "token_expired_403"
const val EVENT_TOKEN_EXPIRED_401 = "token_expired_401"
const val EVENT_USER_LOGOUT = "user_logout"
const val EVENT_USER_DELETE_ACCOUNT = "user_delete_account"
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
const val EVENT_EDIT_PREFERENCES = "edit_preferences"
const val EVENT_ENTER_FIRST_OC = "enter_first_oc"
const val EVENT_CONFIRM_FIRST_ROUTE = "confirm_first_route"
const val EVENT_LOAD_SCROLL = "load_scroll"
const val EVENT_NOTIFICATION_OPEN = "notification_open_event"
const val EVENT_APP_OPEN = "app_open"
const val EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION = "doc_uploaded_with_wrong_extension"
const val EVENT_VIEW_TRIP_RECOVERY_ADJUSTMENT = "view_trip_recovery_adjustment"
const val EVENT_VIEW_TRIP_FUTURE_ADJUSTMENT = "view_trip_future_adjustment"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIPS = "view_change_payment_mode_trips"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIP_DETAILS = "view_change_payment_mode_trip_details"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_DONE = "view_change_payment_mode_done"
const val EVENT_VIEW_CHANGE_PAYMENT_MODE_CANCEL = "view_change_payment_mode_trip_cancel"
const val EVENT_DEEP_LINK_ADD_FUEL_PAYMENT = "deep_link_add_fuel_payment"
const val EVENT_ACTIVATE_TRUCK = "activate_truck"
const val EVENT_DEACTIVATE_TRUCK = "deactivate_truck"
const val EVENT_DELETE_TRUCK = "delete_truck"
const val EVENT_BANNER_CLICK_TOP = "click_add_truck_top_banner"
const val EVENT_BANNER_CLICK_SCROLL = "click_add_truck_scroll_banner"
const val EVENT_VIEW_MY_TRUCK = "view_my_truck"
const val EVENT_SUBMITTED_ABOUT_YOURSELF = "submitted_about_yourself"
const val EVENT_SUBMITTED_ROUTES_TRUCKS = "submitted_routes_trucks"
const val EVENT_SUBMIT_GST = "submit_gst"
const val EVENT_SUBMIT_AADHAR = "submit_aadhar"
const val EVENT_SUBMIT_IDENTITY = "submit_identity"
const val EVENT_VIEW_ABOUT_YOURSELF = "view_about_yourself"
const val EVENT_CONFIRM_PAN = "confirm_pan"
const val EVENT_SUBMIT_POPUP_OFFICE_ADDRESS = "submit_popup_office_address"
const val EVENT_SUBMIT_OFFICE_ADDRESS = "submit_office_address"
const val EVENT_GST_OFFICE_ADDRESS = "submit_gst_address"
const val EVENT_SUBMIT_BUSINESS_PROOF = "submit_business_proof"
const val EVENT_SUBMIT_PAYMENT_DETAILS = "submit_payment_details"
const val EVENT_ACCEPT_VENDOR_POLICY = "accept_vendor_policy"
const val EVENT_VIEW_PAYOUT = "view_payout"
const val EVENT_SUBMIT_OFFER = "submit_offer"
const val EVENT_CLICKED_OFFER = "clicked_offer"
const val EVENT_VIEW_BIDS_SCREEN_OFFERS = "view_bids_screen_offers"
const val EVENT_VIEW_SHARE_RATE_OFFERS = "view_share_rate_offers"
const val EVENT_VIEW_MY_TRUCK_OFFERS = "view_my_truck_offers"
const val EVENT_VIEW_MY_PROFILE = "view_my_profile"
const val EVENT_CLICKED_PRICE_BANNER = "clicked_price_banner"
const val EVENT_CLICKED_PRICE_NOTIFICATION = "clicked_price_notification"
const val EVENT_SUPPLIER_RECOMMENDATION = "recommendation_notif_open"
const val EVENT_EPOD_LIST_SHOWN = "epod_list_shown"
const val EVENT_HPOD_LIST_SHOWN = "hpod_list_shown"
const val EVENT_HPOD_SUBMIT_TAP = "hpod_submit_tap"
const val EVENT_POD_SEARCH_LIST_SHOWN = "pod_search_list_shown"



const val PROPERTY_TRANSACTION_ID = "transaction_id"
const val PROPERTY_ORIGIN = "origin"
const val PROPERTY_DESTINATION = "destination"
const val PROPERTY_TRUCK_TYPE = "truck_type"
const val PROPERTY_NUM_RESULTS = "num_result"
const val PROPERTY_TRANSACTION_TYPE = "type"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_SUB_SOURCE = "sub_source"
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
const val PROPERTY_FILTER_SELECTED = "filter_selected"
const val PROPERTY_OPTION_SELECTED = "option_selected"
const val PROPERTY_DURATION_SELECTED = "duration_selected"
const val PROPERTY_DOWNLOADED_EMAILED_SELECTED = "downloaded_emailed_selected"
const val PROPERTY_EMAIL_ENTERED = "email_entered"
const val PROPERTY_MOBILE_NUMBER_ENTERED = "mobile_number_entered"
const val PROPERTY_ENTERED_VALUE = "entered_value"
const val PROPERTY_TRIP_ID = "trip_id"
const val PROPERTY_CHARGES_AMOUNT = "charges_amount"
const val PROPERTY_DEDUCTIONS_AMOUNT = "deductions_amount"
const val PROPERTY_PAYMENTS_AMOUNT = "payments_amount"
const val PROPERTY_RECOVERIES_ADJUSTED_AMOUNT = "recoveries_adjusted_amount"
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
const val PROPERTY_NUMBER_OF_OFFERS = "number_of_offers"
const val PROPERTY_LOAD_SCROLL = "load_scroll"
const val PROPERTY_TIME_LAPSE = "time_lapse"
const val PROPERTY_NOTIFICATION_TYPE = "notification_type"
const val PROPERTY_NOTIFICATION_DETAIL = "notification_detail"
const val PROPERTY_PAGE_NAME = "page_name"
const val PROPERTY_OTP_SEND_COUNT = "otp_sent_count"
const val PROPERTY_HOUR_OF_DAY = "hour_of_day"
const val PROPERTY_DATE = "date"
const val PROPERTY_NO_OF_SCROLLS = "no_of_scrolls"
const val PROPERTY_AMOUNT_OF_RECOVERY_ADJUSTED = "amt_of_recovery_adjusted"
const val PROPERTY_TRIP_AGAINST_RECOVERY_ADJUSTED = "trip_against_recovery_adjusted"
const val PROPERTY_RECD_TRIP_IDS = "recd_trip_ids"
const val PROPERTY_TIME_SINCE_LAST_LOGIN = "time_since_last_login"
const val PROPERTY_CHANGE_PAYMENT_DIESEL_PAYOUT = "change_payment_diesel_payout"
const val PROPERTY_CHANGE_PAYMENT_DIESEL_CARD_NUMBER = "change_payment_diesel_card_number"
const val PROPERTY_INVENTORY_ID = "inventory_id"
const val PROPERTY_REASON = "reason_for_deactivating"
const val PROPERTY_PHONE_NO = "phone_no"
const val PROPERTY_TYPE_OF_DOC = "type_of_doc"
const val PROPERTY_SOURCE_PAGE = "source_page"
const val PROPERTY_USERNAME = "user_name"
const val PROPERTY_BUSINESS_NAME = "business_name"
const val PROPERTY_TTL = "time_taken_to_complete"
const val PROPERTY_TNC = "terms_and_conditions"
const val PROPERTY_IDENTITY_SELECTED = "identity_selected"
const val PROPERTY_ADD_PROOF_TYPE = "address_proof_type"
const val PROPERTY_BUSINESS_PROOF_TYPE = "business_proof_type"
const val PROPERTY_ERROR_MESSAGE = "error_message"
const val PROPERTY_OFFER_ID = "offer_id"
const val PROPERTY_OFFER_SOURCE = "source"
const val PROPERTY_SP_PHONE_NUMBER = "sp_phone_number"
const val PROPERTY_FTL_ADHOC_COUNT = "ftlAdhocMissingCount"
const val PROPERTY_FTL_CONTRACT_COUNT = "ftlContractMissingCount"
const val PROPERTY_INTRACITY_ADHOC_COUNT = "intracityAdhocMissingCount"
const val PROPERTY_INTRACITY_CONTRACT_COUNT = "intracityContractMissingCount"
const val PROPERTY_MISSING_TOTAL_COUNT = "missing_count"
const val PROPERTY_DELAYED_TOTAL_COUNT = "delayed_count"
const val PROPERTY_EXPECTED_TOTAL_COUNT = "expected_count"








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
const val VALUE_ADD_TRUCK_SCROLL_BANNER = "add_truck_scroll_banner"
const val VALUE_ADD_TRUCK_PAGE = "add_truck_page"
const val VALUE_ADD_TRUCK_ONBOARDING_PAGE = "add_truck_onboarding_page"
const val VALUE_ADD_TRUCK_PLACEMENT = "add_truck_from_placement"


//MoEngage Attributes and Events
const val EVENT_LOGIN = "login"
const val EVENT_HOME_ORDER_CARD_CLICK = "home_order_card_click"
const val EVENT_HOME_LOADS_TAB_CLICK = "home_loads_tab_click"
const val EVENT_HOME_MY_TRUCKS_TAB_CLICK = "home_my_trucks_tab_click"
const val EVENT_HOME_CONTRACT_TAB_CLICK = "view_contract_tab"
const val EVENT_HOME_CONTRACT_CARD_CLICK = "view_contract"
const val EVENT_SUBMIT_CONTRACT_BID = "submit_contract_bid"
const val EVENT_REVISE_CONTRACT_BID = "revise_contract_bid"
const val EVENT_VIEW_CONTRACT_STATUS = "view_contract_status"


const val EVENT_HOME_SEARCH_INITIATE = "home_search_initiate"
const val EVENT_NAVIGATION_MY_BIDS = "navigation_my_bids"
const val EVENT_NAVIGATION_MY_TRIPS = "navigation_my_trips"
const val EVENT_NAVIGATION_MY_PROFILE = "navigation_my_profile"
const val EVENT_NAVIGATION_PODS = "navigation_pods"
const val EVENT_NAVIGATION_HOME = "navigation_home"
const val EVENT_SEARCH_RESULTS_ORDER_CARD_CLICK = "search_results_order_card_click"
const val EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID =
  "page_load_order_details_without_existing_bid"
const val EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID =
  "page_load_order_details_with_existing_bid"
const val EVENT_LOADFEED_BID_INITIATE = "loadfeed_bid_initiate"
const val EVENT_SEARCH_RESULT_BID_INITIATE = "search_result_bid_initiate"
const val EVENT_ORDER_DETAILS_BID_INITIATE = "order_details_bid_initiate"
const val EVENT_LOADFEED_BID_SUBMIT = "loadfeed_bid_submit"
const val EVENT_SEARCH_RESULT_BID_SUBMIT = "search_result_bid_submit"
const val EVENT_ORDER_DETAILS_BID_SUBMIT = "order_details_bid_submit"
const val EVENT_LOWEST_BID_CTA = "lowest_bid_CTA"
const val EVENT_NOT_LOWEST_BID_CTA = "not_lowest_bid_CTA"
const val EVENT_BID_REVISE_INITIATED = "bid_revise_initiated"
const val EVENT_BID_REVISE_SUBMITTED = "bid_revise_submitted"
const val EVENT_LOADFEED_BID_REVISE_INITIATED = "loadfeed_bid_revise_initiated"
const val EVENT_LOADFEED_BID_REVISE_SUBMITTED = "loadfeed_bid_revise_submitted"
const val EVENT_SEARCH_RESULT_BID_REVISE_INITIATED = "search_result_bid_revise_initiated"
const val EVENT_SEARCH_RESULT_BID_REVISE_SUBMITTED = "search_result_bid_revise_submitted"
const val EVENT_SEARCH_DETAILS_SUBMIT = "search_details_submit"
const val EVENT_PAGE_LOAD_SEARCH_RESULTS_WITH_ORDERS = "page_load_search_results_with_orders"
const val EVENT_PAGE_LOAD_SEARCH_RESULTS_NO_ORDERS = "page_load_search_results_no_orders"
const val EVENT_PAGE_CONTRACT_SEARCH_RESULTS_WITH_ORDERS = "page_contract_search_results_with_orders"
const val EVENT_PAGE_CONTRACT_SEARCH_RESULTS_NO_ORDERS = "page_contract_search_results_no_orders"
const val EVENT_ADD_TRUCK_INITIATE = "add_truck_initiate"
const val EVENT_EDIT_TRUCK_INITIATE = "edit_truck_initiate"
const val EVENT_ADD_TRUCK_SUBMIT = "add_truck_submit"
const val EVENT_EDIT_TRUCK_SUBMIT = "edit_truck_submit"
const val EVENT_REQUEST_FOR_LOAD_SUBMIT = "request_for_load_submit"
const val EVENT_HOME_CLICKED = "home_click"
const val EVENT_LOAD_INTRACITY_CLICKED= "Load_Intra_click"
const val EVENT_LOAD_INTERCITY_CLICKED = "Load_Inter_click"
const val EVENT_NON_DELHIVERY_LOAD_CLICKED = "Load_nonDLV_click"
const val EVENT_LOAD_SEARCH_CLICKED = "Load_search"
const val EVENT_LOAD_VEHICLE_TYPE_CLICKED = "load_VT"
const val EVENT_LOAD_INTRACITY_NAVIGATE_CLICKED = "intra_navigate"
const val EVENT_LOAD_INTRACITY_ACCEPT_CLICKED = "intra_accept"
const val EVENT_LOAD_INTRACITY_VEHICLE_NUMBER = "load_intra_VNum"
const val EVENT_LOAD_INTRACITY_DRIVER_NAME = "load_intra_DrName"
const val EVENT_LOAD_INTRACITY_DRIVER_NUMBER = "load_intra_DrNum"
const val EVENT_LOAD_INTRACITY_SUBMIT= "load_intra_submit"
const val EVENT_HOME_PLACEMENT_TAB= "placement_tab_landing"
const val EVENT_HOME_PLACEMENT_DELAYED_TAB= "delayed_tab"
const val EVENT_HOME_PLACEMENT_DETAIL_MISSING= "details_missing"
const val EVENT_HOME_PLACEMENT_EXPECTED_TAB= "expected_tab"
const val EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED= "demand_card_click"
const val EVENT_HOME_PLACEMENT_ADD_DETAILS_ATTEMPTED= "add_details_attempted"
const val EVENT_HOME_PLACEMENT_EDIT_DETAILS_ATTEMPTED= "edit_details_attempted"
const val EVENT_HOME_PLACEMENT_ADD_DETAILS_SUCCESS= "add_details_success"
const val EVENT_HOME_PLACEMENT_EDIT_DETAILS_SUCCESS= "edit_details_success"
const val EVENT_HOME_PLACEMENT_FILTER= "placement_filter"
const val EVENT_HOME_PLACEMENT_MISSING_DETAILS_LISTING= "missing_details_listing"
const val EVENT_HOME_TOTAL_PLACEMENT= "total_placements"



const val PROPERTY_ORDER_ID = "order_id"
const val PROPERTY_ORDER_RANK = "order_rank"
const val PROPERTY_ORDER_COUNT = "order_count"
const val PROPERTY_BID_COUNT = "bid_count"
const val PROPERTY_INVENTORY_COUNT = "inventory_count"
const val PROPERTY_TOTAL_BIDS_COUNT = "total_bids_count"
const val PROPERTY_ACTIVE_BIDS_COUNT = "active_bids_count"
const val PROPERTY_CONFIRMED_BIDS_COUNT = "confirmed_bids_count"
const val PROPERTY_LOST_BIDS_COUNT = "lost_bids_count"
const val PROPERTY_AWAITING_ARRIVAL_COUNT = "awaiting_arrival_count"
const val PROPERTY_USER_BID_VALUE = "user_bid_value"
const val PROPERTY_VEHICLE_REPORTING_DATE_TIME = "vehicle_reporting_date_time"
const val PROPERTY_ORDER_LOWEST_BID_VALUE = "order_lowest_bid_value"
const val PROPERTY_USER_BID_VALUE_OLD = "user_bid_value_old"
const val PROPERTY_USER_BID_VALUE_NEW = "user_bid_value_new"
const val PROPERTY_SEARCH_ORIGIN_CITY = "search_origin_city"
const val PROPERTY_SEARCH_DESTINATION_CITY = "search_destination_city"
const val PROPERTY_SEARCH_BODY_TYPE = "search_body_type"
const val PROPERTY_INVENTORY_UUID = "inventory_uuid"
const val PROPERTY_VENDOR_ID = "vendor_id"
const val PROPERTY_FIELD_EDITED = "field_edited"
const val PROPERTY_PREVIOUS_SOURCE = "previous_source"
const val PROPERTY_CONTRACT_TYPE = "contract_type"
const val PROPERTY_IS_FLEXIBLE= "is_flexible"
const val PROPERTY_BID_AMOUNT_DIFF = "bid_amount_diff"
const val PROPERTY_VEHICLE_TYPE = "vehicle_type"
const val PROPERTY_DRIVER_NAME = "driver_name"
const val PROPERTY_DRIVER_NUMBER = "driver_number"
const val PROPERTY_VEHICLE_NUMBER = "vehicle_number"
const val PROPERTY_MISSING_FLAG = "missing_details"
const val PROPERTY_EXPECTED_TIME = "expected_time"
const val PROPERTY_VEHICLE_NO = "vehicle_no"
const val PROPERTY_DRIVER_PHONE = "driver_phone"
const val PROPERTY_TIMESTAMP = "timestamp"




const val VALUE_BANNER = "banner"
const val VALUE_MY_TRUCKS = "my_trucks"
const val VALUE_ORIGIN = "origin"
const val VALUE_DESTINATION = "destination"
const val VALUE_PRICE = "price"
const val VALUE_OWNERSHIP = "ownership"
const val VALUE_DEEPLINK = "deeplinking"
const val VALUE_APP_FLOW = "app_flow"
const val VALUE_SEARCH_LISITING = "search_listing"
const val VALUE_BID_LISTING = "bid_listing"
const val VALUE_PUSH_NOTIFICATION = "push_notification"
const val VALUE_ORDER_LISTING = "order_listing"
const val VALUE_VENDOR_SUBSOURCE = "new_order_vendor_match"
const val VALUE_INVENTORY_SUBSOURCE = "new_order_inventory_match"






const val USER_PROPERTY_ANDROID_ID = "android_id"
const val USER_PROPERTY_ANDROID_VERSION = "android_version"
const val USER_PROPERTY_BASE_CITY = "base_city"
const val USER_PROPERTY_CREATION_DATE = "creation_date"
const val USER_PROPERTY_NAME = "name"
const val USER_PROPERTY_UUID = "uuid"
const val USER_PROPERTY_PHONE_NO = "phone_no"
const val USER_PROPERTY_COMPANY_NAME = "company_name"
const val USER_PROPERTY_DEMAND_TYPE = "demand_type"
const val USER_PROPERTY_OWNS_TRUCKS = "owns_trucks"
const val USER_PROPERTY_STATUS = "status"
const val USER_PROPERTY_SUB_STATUS = "sub_status"
const val USER_PROPERTY_IS_KYC_VERIFIED = "is_kyc_verified"
const val USER_PROPERTY_RECEIVE_WHATSAPP_NOTIFICATIONS = "receive_whatsapp_notifications"
