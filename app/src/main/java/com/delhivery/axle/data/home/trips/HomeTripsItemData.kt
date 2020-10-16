package com.delhivery.axle.data.home.trips

import android.view.View
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
  @SerializedName("is_epod_verified") val isEpodVerified: Boolean? = false,
  @SerializedName("is_multi_drop") val isMultidrop: Boolean? = false,
  @SerializedName("epod_rejection_remarks") val epodRejectionRemark: String? = "",
  @SerializedName("lr_details") val lrDetails: MutableList<LRDetails> = mutableListOf(),
  @SerializedName("speed") var speed: String?,
  @SerializedName("tat_minutes") var tatMinutes: String?,
  @SerializedName("origin_district") val originDistrict: String?,
  @SerializedName("destination_district") val destinationDistrict: String?,
  var payment: ExpenseData? = null,
  var fuelCard: FuelCardData? = null,
  var selected: Boolean = false,
  var selectable: Boolean = false,
  var tds: Int,
  var updatedTds: Double
) : BaseKeyTypeModel<String>(), Serializable {
  override fun key() = transactionId

  /**
   * if trip is Multidrop
   */
  fun setMutidrop() = if (isMultidrop != null && isMultidrop == true) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * Formatted driver details as per UI
   */
  fun formattedDriverDetails() = "Driver: ${driverDetails?.driverPhoneNo}"

  /**
   * Trip Status [TripStatus]
   */
  fun tripStatus() = TripType.byStatus(tripStatus)

  /**
   * Trip status visibility
   */
  fun tripStatusVisibility() = if (tripStatus == "truck_unloaded" || tripStatus == "epod_uploaded") {
    View.GONE
  } else {
    View.VISIBLE
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
   * @return formatted origin district
   */
  fun originDistrictName() = originDistrict?.let { StringUtils.capitalize(it) } ?: ""

  /**
   * @return formatted destination district
   */
  fun destinationDistrictName() = destinationDistrict?.let { StringUtils.capitalize(it) } ?: ""

  /**
   * @return origin district and state name
   */
  fun originDistrictState() = if (originDistrictName().isNotNullOrEmpty()) {
    originDistrictName() + ", " + originStateName()
  } else {
    originStateName()
  }

  /**
   * @return destination district and state name
   */
  fun destinationDistrictState() = if (destinationDistrictName().isNotNullOrEmpty()) {
    destinationDistrictName() + ", " + destinationStateName()
  } else {
    destinationStateName()
  }

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
  fun promiseDate() =
    promiseDate?.let {
      val format = when {
        isExpress() -> "dd-MMM-yyyy hh:mm"
        else -> "dd-MMM-yyyy"
      }
      "PD: " + DateUtils.formatDate(
          DateUtils.parseDate(it, OrionDateFormat), format
      )
    } ?: ""

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
  fun lr() = if (lr.isNotNullOrEmpty()) {
    "LR: $lr"
  } else if (!lrDetails.isNullOrEmpty()) {
    val lrString = StringBuilder()
    lrDetails.forEach {
      lrString.append(it.lr)
          .append(", ")
    }
    "LR: ${lrString.substring(0, lrString.length - 2)}"
  } else {
    ""
  }

  /**
   * @return pod action text
   */
  fun podAction() = when (tripStatus) {
    TruckUnloaded.statusKey -> {
      if (epodRejectionRemark.isNotNullOrEmpty()) PODStatus.REJECT else if (podUrl.isNullOrEmpty()) PODStatus.UPLOAD else PODStatus.VIEWPOD
    }
    EPodUploaded.statusKey -> {
      if (isEpodVerified == null || isEpodVerified == false) PODStatus.REVIEW else PODStatus.VIEWPOD
    }
    else -> if (podUrl.isNullOrEmpty()) PODStatus.UPLOAD else PODStatus.VIEWPOD
  }

  /**
   * Required at pod background
   */
  fun podDrawable() = when (podAction()) {
    PODStatus.REJECT -> {
      DrawableProviderUtils.podDrawableRes(PODStatus.REJECT)
    }
    PODStatus.UPLOAD -> {
      DrawableProviderUtils.podDrawableRes(PODStatus.UPLOAD)
    }
    PODStatus.REVIEW -> {
      DrawableProviderUtils.podDrawableRes(PODStatus.REVIEW)
    }
    else -> {
      DrawableProviderUtils.podDrawableRes(PODStatus.VIEWPOD)
    }
  }

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
//    BalancePending -> {
//      if (payment != null) {
//        val advancePayment = payment?.payments?.find { it.head == "cash_advance" }
//        val loadingChargePayment = payment?.payments?.find { it.head == "loading_charge" }
//        val intermittentPayment = payment?.payments?.filter { it.head == "intermittent" }
//        var amount = bidDetails?.bidPrice ?: 0.0
//        if (!intermittentPayment.isNullOrEmpty()) {
//          intermittentPayment.forEach {
//            amount -= it.amount
//          }
//        }
//        if (advancePayment != null) {
//          amount -= advancePayment.amount
//          if (loadingChargePayment != null) {
//            amount -= loadingChargePayment.amount
//          }
//        }
//        "₹ ${StringUtils.formatAmount(amount)}"
//      } else {
//        ""
//      }
//    }
    else -> ""
  }

  /**
   * Advance payment status, payment date and utr triplet
   */
  fun advance(): Triple<String, String, String> {
    var status = "Advance Pending"
    var date = ""
    var totaltds = 0.0
    var amount = bidDetails?.advancePayout ?: 0.0
    val advancePayment = payment?.payments?.find { it.head == "cash_advance" }
    val loadingChargePayment = payment?.payments?.find { it.head == "loading_charge" }
    if (advancePayment != null) {
      status = "Advance Paid"
      date = advancePayment.dateTime()
      amount = advancePayment.amount
      totaltds += advancePayment.getTDS(tds, updatedTds)
      if (loadingChargePayment != null) {
        amount += loadingChargePayment.amount
        totaltds += loadingChargePayment.getTDS(tds, updatedTds)
      }
    }
    amount -= totaltds
    return Triple(status, date, "₹ ${StringUtils.formatAmount(amount)}")
  }

  /**
   * Balance payment status, payment date and utr triplet
   */
  fun balance(): Triple<String, String, String> {
    var status = "Balance Pending"
    var date = ""
    var amount = bidDetails?.bidPrice ?: 0.0
    var advance = 0.0

    val advancePayment = payment?.payments?.find { it.head == "cash_advance" }
    val loadingChargePayment = payment?.payments?.find { it.head == "loading_charge" }
    if (advancePayment != null) {
      advance += advancePayment.amount
      if (loadingChargePayment != null) {
        advance += loadingChargePayment.amount
      }
    } else {
      advance = bidDetails?.advancePayout ?: 0.0
    }

    val intermittentPayment = payment?.payments?.filter { it.head == "intermittent" }
    val partialBalancePayment = payment?.payments?.find { it.head == "partial_balance_payment" }
    val balancePayment = payment?.payments?.find { it.head == "balance_payment" }
    var charges = 0.0
    payment?.charges?.forEach { charge ->
      charge.payVendor?.let {
        if (charge.payVendor < 0) {
          charges += charge.payVendor
        } else {
          charges -= charge.payVendor
        }
      }
      charge.deductVendor?.let {
        charges += charge.deductVendor
      }
    }
    amount -= (advance + charges)

    var interPayments = 0.0
    if (!intermittentPayment.isNullOrEmpty()) {
      intermittentPayment.forEach {
        interPayments += (it.amount)
      }
    }
    partialBalancePayment?.let {
      interPayments += (it.amount)
    }
    amount -= (interPayments)
    amount = amount * (updatedTds) / 100

    balancePayment?.let {
      status = "Balance Paid"
      date = it.dateTime()
      amount = it.amount - it.getTDS(tds, updatedTds)
    }

    return Triple(status, date, "₹ ${StringUtils.formatAmount(amount)}")
  }

  /**
   * Pending text
   */
  fun pending() = if (detentionPending == true) "Detention Issue"
  else {
    if (damagePending == true) "Damage Issue" else ""
  }

  /**
   * pod remarks or partial payment status
   */
  fun podRemarksOrPayment() = if (chargesUpdated != null && chargesUpdated == true) {
    "Partial Balance Approved"
  } else if (tripStatus == TruckUnloaded.statusKey && epodRejectionRemark.isNotNullOrEmpty()) {
    epodRejectionRemark
  } else {
    ""
  }

  /**
   * @return true if speed is express
   */
  fun isExpress() = speed?.compareTo("EXP") == 0

  override fun filter(query: String) =
    vehicleDetails.vehicleNo.contains(query, true)
        || destination.contains(query, true)
        || (lr.isNotNullOrEmpty() && lr.contains(query, true))

}

enum class PODStatus(
  val status: String,
  val key: Int
) {
  REJECT("REJECTED", 1),
  UPLOAD("UPLOAD EPOD", 2),
  REVIEW("UNDER REVIEW", 3),
  VIEWPOD("VIEW EPOD", 4);
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
 * LR details
 */
data class LRDetails(
  @SerializedName("lr") val lr: String,
  @SerializedName("lr_date") val lrDate: String,
  @SerializedName("invoice_details") var invoice: List<InvoiceDetails>
)

/**
 * Invoice details
 */
data class InvoiceDetails(
  @SerializedName("invoice_number") var invoiceNumber: String? = null,
  @SerializedName("ewaybill_number") var ewaybillNumber: String? = null,
  @SerializedName("invoice_amount") var invoiceAmount: Double? = null,
  @SerializedName("weight_in_mt") var actualLoad: Double? = null,
  @SerializedName("volumetric_weight") var volumetricWeight: Double? = null
)

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
  InvoiceInProgress("invoice_inprogress", "Invoice Progress"),
  Invoiced("invoiced", "Invoiced"),
  InvoicFailed("invoice_failed", "Invoice Failed"),
  Unknown("unknown", "Unknown");

  companion object {

    /**
     * Get [TripStatus] from response key
     */
    fun byKey(statusKey: String) =
      values().firstOrNull { it.statusKey.equals(statusKey, true) } ?: Unknown
  }
}