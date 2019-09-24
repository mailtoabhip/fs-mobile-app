package com.delhivery.axle.data.home.trips

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.api.response.BulkPaymentItem
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.ui.bids.TripType.BalancePending
import com.delhivery.axle.ui.bids.TripType.Completed
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HomeTripsItemData(
  @SerializedName("LR") val lr: String,
  @SerializedName("arrival_time") val arrivalTime: String?,
  @SerializedName("auto_advance_transfer") val autoAdvanceTransfer: Boolean? = false,
  @SerializedName("client_id") val clientId: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("trip_status") private val _tripStatus: String,
  @SerializedName("vehicle") val vehicleDetails: TripVehicleDetails,
  @SerializedName("driver") val driverDetails: TripDriverDetails?,
  @SerializedName("bid_details") val bidDetails: TripBidDetails?,
  @SerializedName("loading_location") val loadingLocation: String?,
  @SerializedName("reached_time") val reachedTime: String?,
  @SerializedName("required_on") val requiredOn: String,
  @SerializedName("unloading_location") val unloadingLocation: String?,
  @SerializedName("payment_mode") val paymentMode: String? = null,
  @SerializedName("truck_display_name") val truckDisplayName: String? = "",
  var payment: BulkPaymentItem? = null
) : BaseKeyTypeModel<String>(), Serializable {
  override fun key() = transactionId

  /**
   * Formatted driver details as per UI
   */
  fun formattedDriverDetails() = "Driver: ${driverDetails?.driverPhoneNo}"

  /**
   * Trip Status [TripStatus]
   */
  fun tripStatus() = TripType.byStatus(_tripStatus)

  fun tripPayment() = when (tripStatus()) {
    AdvancePending -> {
      if (bidDetails != null && bidDetails.advancePayout ?: 0.0 > 0.0) {
        "₹ ${StringUtils.formatAmount(bidDetails.advancePayout ?: 0.0)}"
      } else {
        ""
      }
    }
    BalancePending -> {
      if (payment != null && payment!!.bidPrice > 0.0 && payment!!.advancePayout > 0.0) {
        val balance = payment!!.bidPrice.minus(payment!!.advancePayout)
        "₹ ${StringUtils.formatAmount(balance)}"
      } else {
        ""
      }
    }
    Completed -> {
      if (payment != null && payment!!.bidPrice > 0) {
        "₹ ${StringUtils.formatAmount(payment!!.bidPrice)}"
      } else {
        ""
      }
    }
    else -> ""

  }

  override fun filter(query: String) =
    vehicleDetails.vehicleNo.contains(query, true)
        || destination.contains(query, true)
        || (lr.isNotNullOrEmpty() && lr.contains(query, true))

  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  private fun displayTime() = when (_tripStatus) {
    TripStatus.TruckConfirmed.statusKey -> requiredOn
    else -> arrivalTime ?: requiredOn
  }

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
   * Formatted required at
   */
  fun requiredAt() =
    DateUtils.formatDate(DateUtils.parseDate(displayTime(), DatePatterns.OrionDateFormat), "dd MMM")

  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    DrawableProviderUtils.daysDiffBgDrawableRes(displayTime(), DatePatterns.OrionDateFormat)

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun requiredTextColor() =
    ColorProviderUtils.getTripStatusColor(tripStatus().typeText.toLowerCase())

}

/* actions */
const val HomeTripsRequestAction_ViewDetails = "trip_details"

data class TripDriverDetails(
  @SerializedName("phone_number") val driverPhoneNo: String?
) {

  fun driverPhoneNo() = "Driver($driverPhoneNo)"
}

data class TripVehicleDetails(
  @SerializedName("vehicle_number") val vehicleNo: String
)

data class TripBidDetails(
  @SerializedName("advance_payout") val advancePayout: Double?,
  @SerializedName("bid_price") val bidPrice: Int?,
  @SerializedName("effective_price") val effectivePrice: Int?,
  @SerializedName("fuel_payout") val fuelPayout: Double?
) {

  fun bidPrice() = "₹ " + StringUtils.formatAmount(bidPrice?.toDouble() ?: 0.0)
}

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