package com.delhivery.axle.data.home.bids

import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName

/**
 *
 * Transaction details
 */
data class HomeBidsRequestItemData(
  @SerializedName("material_type") val materialType: String,
  @SerializedName("pickup_location") val pickupLocation: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("required_on") val _requiredOn: String,
  @SerializedName("target_price") val targetPrice: Double,
  @SerializedName("uuid") private val uuid: String?,
  @SerializedName("transaction_id") val transactionId: String?,
  @SerializedName("truck_type") val truckType: String?,
  @SerializedName("origin") val origin: String,
  @SerializedName("intermediary_stop1") val stop1City: String,
  @SerializedName("intermediary_stop1_state") val stop1State: String,
  @SerializedName("intermediary_stop2") val stop2City: String,
  @SerializedName("intermediary_stop2_state") val stop2State: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("truck_display_name") val truckDisplayName: String?,
  @SerializedName("commercial_type") val commercialType: String? = "",
  @SerializedName("load_price_percent") var loadPricePercent: Int,
  @SerializedName("requested_capacity_mg") var requestedCapacityMg: Double,
  @SerializedName("pmt_rate") var pmtRate: Double,
  @SerializedName("distance") var distance: Double,
  @SerializedName("truck_specifications") var truckSpecification: TruckSpecification,
  var lowestBid: Double? = 0.0,
  var numBids: Int = 0,
  var transactionBid: TransactionBid? = null,
  var showing: Boolean = false
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid ?: transactionId!!

  fun loadDetails() = "Load: ${StringUtils.capitalize(materialType) ?: "Not available"}"

  fun target() = if (targetPrice > 0 && loadPricePercent > 0) {
    targetPrice * loadPricePercent / 100
  } else {
    0.0
  }

  /**
   * @return formatted origin city name
   */
  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  /**
   * @return formatted destination city name
   */
  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  /**
   * @return formatted origin state name
   */
  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  /**
   * @return formatted destination state name
   */
  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  /**
   * @return formatted origin city, state
   */
  fun originCityState() = originCityName() + ", " + originStateName()

  /**
   * @return formatted destination city, state
   */
  fun destinationCityState() = destinationCityName() + ", " + destinationStateName()

  /**
   * @return intermediary stops
   */
  fun tripRoute(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
        .append(" - ")
    if (!TextUtils.isEmpty(stop1City)) {
      stopBuilder.append(StringUtils.capitalize(stop1City))
          .append(" - ")
    }
    if (!TextUtils.isEmpty(stop2City)) {
      stopBuilder.append(StringUtils.capitalize(stop2City))
          .append(" - ")
    }
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
  }

  /**
   * @return is trips is multi drop
   */
  fun isMultiDrop() = (stop1City.isNotNullOrEmpty() || stop2City.isNotNullOrEmpty())

  /**
   * @return requested capacity
   */
  fun requestedCapacityMg() = "Capacity: $requestedCapacityMg MG"

  /**
   * @return intermediary Stops string
   */
  fun intermediaryStops(): String {
    val stopBuilder = StringBuilder()
    if (!TextUtils.isEmpty(stop1City)) {
      stopBuilder.append(StringUtils.capitalize(stop1City))
          .append("(")
          .append(StringUtils.capitalize(stop1State))
          .append(")")
    }
    if (!TextUtils.isEmpty(stop2City)) {
      stopBuilder.append(", ")
          .append(StringUtils.capitalize(stop2City))
          .append("(")
          .append(StringUtils.capitalize(stop2State))
          .append(")")
    }
    return stopBuilder.toString()
  }

  /**
   * @return formatted bid amount
   */
  fun bidAmount() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted, Open, Rejected -> "₹ ${StringUtils.formatAmount(transactionBid!!.bidAmount)}"
      else -> ""
    }
  } else {
    ""
  }

  /**
   * @return bid label
   */
  fun amountLabel() = when (bidStatus()) {
    Open, Rejected -> "Your Bid"
    Accepted -> "Confirmed Price"
    else -> ""
  }

  /**
   * @return truckTypeDrawable basis [truckType]
   */
  @DrawableRes
  fun truckTypeDrawableRes() = DrawableProviderUtils.truckTypeDrawableRes(truckType)

  /**
   * Formatted required at
   */
  fun requiredAt() = DateUtils.daysDiffWithTimeStr(_requiredOn, DatePatterns.OrionDateFormat)

  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    DrawableProviderUtils.daysDiffBgDrawableRes(_requiredOn, DatePatterns.OrionDateFormat)

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun requiredTextColor() =
    ColorProviderUtils.getStatusColor(bidStatus().status.toLowerCase())

  /**
   * Get truck details/type
   */
  fun truckDetail() = truckSpecification.truckDispName + "(" +
      StringUtils.formatAmount(truckSpecification.defaultMG) + " MT)"

  /**
   * Trip display name for toolbar title
   */
  fun tripDisplayName(tripType: TripType? = null) =
    when (tripType) {
      AdvancePending -> "${StateModel.idFromName(originState)} - ${StateModel.idFromName(
          destinationState
      )} (${DateUtils.daysDiffStr(_requiredOn, DatePatterns.OrionDateFormat)})".toUpperCase()
      else -> "${StateModel.idFromName(originState)} - ${StateModel.idFromName(destinationState)}"
    }

  /**
   * @return bid difference
   */
  fun bidDifference(): String {
    if (numBids > 1 && lowestBid != null && lowestBid!! > 0) {
      return transactionBid?.diffFromLowestBid(lowestBid!!, isPMTIndent()) ?: ""
    }
    return ""
  }

  /**
   * @return bid status
   */
  fun bidStatus() = TransactionBidStatus.byStatusKey(transactionBid?._status ?: "na")

  override fun filter(query: String) =
    origin.contains(query, true) || destination.contains(query, true)
        || originState.contains(query, true) || destinationState.contains(query, true)

  /**
   * @return bid text
   */
  fun bidText() = "Bid placed for ₹ ${StringUtils.formatAmount(
      transactionBid?.bidAmount ?: 0.0
  )}" + if (isPMTIndent()) " /MT" else ""

  /**
   * @return true if indent type(pmt/ftl)
   */
  fun isPMTIndent() = commercialType?.toLowerCase() == "pmt"

}

/**
 * Truck specification detail
 */
data class TruckSpecification(
  @SerializedName("default_MG") val defaultMG: Double,
  @SerializedName("truck_display_name") val truckDispName: String
)

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"
const val HomeBidsRequestAction_PlaceBid = "place_bid"