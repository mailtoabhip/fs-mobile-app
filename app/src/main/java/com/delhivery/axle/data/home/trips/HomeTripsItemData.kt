package com.delhivery.axle.data.home.trips

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.api.response.ExpenseData
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.ui.bids.TripType.BalancePending
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns.OrionDateFormat
import com.delhivery.axle.utils.DatePatterns.SimpleDateFormat
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class HomeTripsItemData(
  @SerializedName("LR") val lr: String,
  @SerializedName("arrival_time") val arrivalTime: String?,
  @SerializedName("action_time") val actionTime: String,
  @SerializedName("auto_advance_transfer") val autoAdvanceTransfer: Boolean? = false,
  @SerializedName("client_id") val clientId: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("trip_status") val tripStatus: String,
  @SerializedName("vehicle") val vehicleDetails: TripVehicleDetails,
  @SerializedName("driver") val driverDetails: TripDriverDetails?,
  @SerializedName("bid_details") val bidDetails: TripBidDetails?,
  @SerializedName("loading_location") val loadingLocation: String?,
  @SerializedName("reached_time") val reachedTime: String?,
  @SerializedName("unloaded_time") val unloadingTime: String?,
  @SerializedName("required_on") val requiredOn: String,
  @SerializedName("unloading_location") val unloadingLocation: String?,
  @SerializedName("payment_mode") val paymentMode: String? = null,
  @SerializedName("truck_display_name") val truckDisplayName: String? = "",
  @SerializedName("pod_url") val podUrl: String? = "",
  @SerializedName("promise_date") val promiseDate: String? = "",
  @SerializedName("actual_load") val load: Double? = 0.0,
  @SerializedName("dead_weight") val deadWeight: Double? = 0.0,
  @SerializedName("volumetric_weight") val volumetricWeight: Double? = 0.0,
  @SerializedName("weight_used") val weightUsed: String? = "",
  @SerializedName("vendor_pmt_rate") val vendorPmtRate: Double? = 0.0,
  @SerializedName("truck_specifications") val truckSpecification: TruckSpecification?,
  @SerializedName("pod_dispatch_awb_number") val podDispatchAwbNumber: String?,
  @SerializedName("pod_dispatch_docket_image") val podDispatchDocketImage: String?,
  @SerializedName("pod_dispatch_date") val podDispatchDate: String?,
  @SerializedName("status_update_info") val updateInfo: StatusUpdateInfo?,
  @SerializedName("charges_updated") val chargesUpdated: Boolean? = false,
  @SerializedName("damage_pending") val damagePending: Boolean? = false,
  @SerializedName("detention_pending") val detentionPending: Boolean? = false,
  var payment: ExpenseData? = null,
  var fuelCard: FuelCardData? = null,
  var selected: Boolean = false,
  var selectable: Boolean = false
) : BaseKeyTypeModel<String>(), Serializable {
  override fun key() = transactionId

  /**
   * Formatted driver details as per UI
   */
  fun formattedDriverDetails() = "Driver: ${driverDetails?.driverPhoneNo}"

  /**
   * Trip Status [TripStatus]
   */
  fun tripStatus() = TripType.byStatus(tripStatus)

  /**
   * @return formatted origin city
   */
  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  /**
   * @return formatted destination city
   */
  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  /**
   * @return formatted origin state
   */
  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  /**
   * @return formatted destination state
   */
  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  /**
   * @return formatted display time
   */
  private fun displayTime() = when (tripStatus) {
    TripStatus.TruckConfirmed.statusKey -> requiredOn
    else -> arrivalTime ?: requiredOn
  }

  /**
   * @return advance deduction flag
   */
  fun advanceDeduction() = when (tripStatus()) {
    AdvancePending -> {
      autoAdvanceTransfer ?: false
    }
    else -> {
      when (paymentMode) {
        "automatic" -> true
        null -> false
        else -> false
      }
    }
  }

  /**
   * @return formatted load
   */
  fun load() = when (weightUsed?.toLowerCase()) {
    "bill_on_max_weight" -> volumetricWeight?.let {
      "EQW Weight: ${StringUtils.formatAmount(volumetricWeight)}MT"
    }
    else -> load?.let { "Loaded Weight: ${StringUtils.formatAmount(load)}MT" }
  }

  /**
   * @return formatted pmt rate
   */
  fun pmtRate() = vendorPmtRate?.let { "Rate: ₹ ${StringUtils.formatAmount(vendorPmtRate)} PMT" }

  /**
   * @return promise date
   */
  fun promiseDate() = promiseDate?.let {
    "PD: " + DateUtils.formatDate(
        DateUtils.parseDate(it, OrionDateFormat), "dd-MMM-yyyy"
    )
  }

  /**
   * Formatted required at
   */
  fun requiredAt() =
    DateUtils.formatDate(DateUtils.parseDate(displayTime(), OrionDateFormat), "dd MMM")

  /**
   * Formatted required at
   */
  fun loadedAt() =
    updateInfo?.loadedInfo?.let {
      "Loaded: " + DateUtils.formatDate(
          DateUtils.parseDate(it.time, OrionDateFormat), SimpleDateFormat
      )
    } ?: ""

  /**
   * Formatted required at
   */
  fun unloadedAt() =
    unloadingTime?.let {
      "Unloaded: " + DateUtils.formatDate(
          DateUtils.parseDate(it, OrionDateFormat), SimpleDateFormat
      )
    } ?: ""

  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    DrawableProviderUtils.daysDiffBgDrawableRes(displayTime(), OrionDateFormat)

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun requiredTextColor() =
    ColorProviderUtils.getTripStatusColor(tripStatus().typeText.toLowerCase())

  /**
   * Required at text color as per promise date
   */
  @ColorRes
  fun requiredPromiseDateColor() = promiseDate?.let {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, calendar.getActualMinimum(Calendar.HOUR))
    calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE))
    ColorProviderUtils.getPromiseDateColor(
        (calendar.timeInMillis - DateUtils.parseDate(it, OrionDateFormat).time)
    )
  } ?: R.color.sub_heading_black

  /**
   * Check if fuel balance is available or not
   */
  fun isFuelBalanceAvailable(): Boolean {
    if (fuelCard != null) {
      try {
        val activeAmount = fuelCard?.amount?.toDouble() ?: 0.0
        val allowedAmount = bidDetails?.bidPrice?.times(60)?.div(100) ?: 0.0
        if (activeAmount < allowedAmount) {
          return true
        }
        return false
      } catch (e: Exception) {
        return false
      }
    } else {
      return true
    }
  }

  /**
   * @return true if indent type(pmt/ftl)
   */
  fun isPMTIndent() = truckSpecification?.sourcedAs?.toLowerCase() == "pmt"

  /**
   * @return formatted lr number
   */
  fun lr() = lr.let { "LR: $it" }

  /**
   * @return pod action text
   */
  fun podActionText() = if (podUrl.isNullOrEmpty()) "UPLOAD EPOD" else "VIEW EPOD"

  /**
   * @return pod action text
   */
  fun docketActionText() =
    if (!hasPODTracking()) "ADD COURIER DETAILS" else "UPDATE COURIER DETAILS"

  private fun getDiff(
    arrived: Date,
    prefix: String
  ): String {
    val today = Calendar.getInstance()
    val diffInMillisec = today.timeInMillis - arrived.time
    val daysDiff = TimeUnit.MILLISECONDS.toDays(diffInMillisec)
        .toInt()
    return if (daysDiff >= 1) "$prefix $daysDiff days"
    else "$prefix${TimeUnit.MILLISECONDS.toHours(diffInMillisec).toInt()} hrs"
  }

  /**
   * Get time difference
   */
  fun timeDiff() = when (tripStatus) {
    TruckUnloaded.statusKey, EPodUploaded.statusKey -> {
      if (podDispatchDate.isNotNullOrEmpty()) "Courier Date: $podDispatchDate"
      else unloadingTime?.let {
        getDiff(DateUtils.parseDate(it, OrionDateFormat), "Ageing: ")
      } ?: ""
    }
    else -> {
      ""
    }
  }

  /**
   * Return pod dispatch status
   */
  fun hasPODTracking() = podDispatchAwbNumber.isNotNullOrEmpty()

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun podTimeColor() = when (tripStatus) {
    TruckUnloaded.statusKey, EPodUploaded.statusKey -> {
      if (podDispatchDate.isNotNullOrEmpty()) {
        ColorProviderUtils.getPODDateColor(
            DateUtils.parseDate(podDispatchDate!!, OrionDateFormat)
        )
      } else unloadingTime?.let {
        ColorProviderUtils.getPODDateColor(
            DateUtils.parseDate(it, OrionDateFormat)
        )
      } ?: R.color.sub_heading_black
    }
    else -> {
      R.color.sub_heading_black
    }
  }

  /**
   * Trip route
   */
  fun route(): String {
    return originCityName() + " - " + destinationCityName()
  }

  /**
   * @return payment advance/pending
   */
  fun tripPayment() = when (tripStatus()) {
    AdvancePending -> {
      if (bidDetails != null && bidDetails.advancePayout ?: 0.0 > 0.0) {
        "₹ ${StringUtils.formatAmount(bidDetails.advancePayout ?: 0.0)}"
      } else {
        ""
      }
    }
    BalancePending -> {
      if (payment != null) {
        val advancePayment = payment?.payments?.find { it.head == "cash_advance" }
        val loadingChargePayment = payment?.payments?.find { it.head == "loading_charge" }
        var amount = bidDetails?.bidPrice ?: 0.0
        if (advancePayment != null) {
          amount -= advancePayment.amount
          if (loadingChargePayment != null) {
            amount -= loadingChargePayment.amount
          }
        }
        "₹ ${StringUtils.formatAmount(amount)}"
      } else {
        ""
      }
    }
    else -> ""
  }

  /**
   * Advance payment status, payment date and utr triplet
   */
  fun advance(): Triple<String, String, String> {
    var status = "Advance Pending"
    var date = ""
    var amount = bidDetails?.advancePayout ?: 0.0
    val advancePayment = payment?.payments?.find { it.head == "cash_advance" }
    val loadingChargePayment = payment?.payments?.find { it.head == "loading_charge" }
    if (advancePayment != null) {
      status = "Advance Paid"
      date = advancePayment.dateTime()
      amount = advancePayment.amount
      if (loadingChargePayment != null) {
        amount += loadingChargePayment.amount
      }
    }
    return Triple(status, date, "₹ ${StringUtils.formatAmount(amount)}")
  }

  /**
   * Balance payment status, payment date and utr triplet
   */
  fun balance(): Triple<String, String, String> {
    var status = "Balance Pending"
    var date = ""
    var amount = bidDetails?.bidPrice?.minus(bidDetails.advancePayout ?: 0.0) ?: 0.0
    val balancePayment = payment?.payments?.find { it.head == "balance_payment" }

    if (balancePayment != null) {
      status = "Balance Paid"
      date = balancePayment.dateTime()
      amount = balancePayment.amount
    }
    return Triple(status, date, "₹ ${StringUtils.formatAmount(amount)}")
  }

  /**
   * Pending text
   */
  fun pending() = if (detentionPending == true) "Detention Pending"
  else {
    if (damagePending == true) "Damage Pending" else ""
  }

  override fun filter(query: String) =
    vehicleDetails.vehicleNo.contains(query, true)
        || destination.contains(query, true)
        || (lr.isNotNullOrEmpty() && lr.contains(query, true))

}

/* actions */
const val HomeTripsRequestAction_ViewDetails = "trip_details"
const val HomeTripsRequestAction_UploadEpod = "upload_epod"
const val HomeTripsRequestAction_UploadTracking = "upload_tracking"

/**
 * Trip Driver details
 */
data class TripDriverDetails(@SerializedName("phone_number") val driverPhoneNo: String?) :
    Serializable {

  /**
   * @return formatted [driverPhoneNo]
   */
  fun driverPhoneNo() = "Driver($driverPhoneNo)"
}

/**
 * Trip Vehicle details
 */
data class TripVehicleDetails(@SerializedName("vehicle_number") val vehicleNo: String) :
    Serializable

/**
 * Truck specifications
 */
data class TruckSpecification(
  @SerializedName("default_MG") val advancePayout: Double?,
  @SerializedName("max_capacity") val maxCapacity: Double?,
  @SerializedName("min_capacity") val minCapacity: Double?,
  @SerializedName("sourced_as") val sourcedAs: String?
) : Serializable

/**
 * Trip bid detail
 */
data class TripBidDetails(
  @SerializedName("advance_payout") val advancePayout: Double?,
  @SerializedName("bid_price") val bidPrice: Double?,
  @SerializedName("effective_price") val effectivePrice: Double?,
  @SerializedName("fuel_payout") val fuelPayout: Double?
) : Serializable {

  /**
   * @return formatted [bidPrice]
   */
  fun bidPrice() = "₹ ${StringUtils.formatAmount(bidPrice ?: 0.0)}"
}

/**
 * Status update info
 */
data class StatusUpdateInfo(
  @SerializedName("truck_loaded") val loadedInfo: ByUser? = null,
  @SerializedName("epod_uploaded") val epodUploadInfo: ByUser? = null
) : Serializable

/**
 * By userinfo
 */
data class ByUser(
  @SerializedName("at") val time: String,
  @SerializedName("by") val by: String
) : Serializable

/**
 * Trip status enum
 */
enum class TripStatus(
  val statusKey: String,
  val status: String
) {
  In_Transit("in_transit", "In-Transit"),
  TripCancelled("trip_cancelled", "Trip Cancelled"),
  TripCompleted("trip_completed", "Trip Completed"),
  TruckArrived("truck_arrived", "Truck Arrived"),
  TruckConfirmed("truck_confirmed", "Truck Placed"),
  TruckLoaded("truck_loaded", "Loading Completed"),
  TruckReached("truck_reached", "Reached Destination"),
  TruckUnloaded("truck_unloaded", "Truck Unloaded"),
  EPodUploaded("epod_uploaded", "EPod Uploaded"),
  Unknown("unknown", "Unknown");

  companion object {

    /**
     * Get [TripStatus] from response key
     */
    fun byKey(statusKey: String) =
      values().firstOrNull { it.statusKey.equals(statusKey, true) } ?: Unknown
  }
}