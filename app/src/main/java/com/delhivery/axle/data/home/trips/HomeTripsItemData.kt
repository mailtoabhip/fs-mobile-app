package com.delhivery.axle.data.home.trips

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.api.response.BulkPaymentItem
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.ui.bids.TripType.BalancePending
import com.delhivery.axle.ui.bids.TripType.Completed
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns.OrionDateFormat
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Calendar

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
  var payment: BulkPaymentItem? = null,
  var fuelCard: FuelCardData? = null
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
    "bill_on_max_weight" -> "EQW Weight: ${volumetricWeight}MT"
    else -> "Loaded Weight: ${load}MT"
  }

  /**
   * @return formatted pmt rate
   */
  fun pmtRate() = "Rate: ₹ $vendorPmtRate PMT"

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

  override fun filter(query: String) =
    vehicleDetails.vehicleNo.contains(query, true)
        || destination.contains(query, true)
        || (lr.isNotNullOrEmpty() && lr.contains(query, true))

}

/* actions */
const val HomeTripsRequestAction_ViewDetails = "trip_details"

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
  fun bidPrice() = "₹ " + StringUtils.formatAmount(bidPrice ?: 0.0)
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