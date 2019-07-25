package com.delhivery.axle.data.home.bids

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

data class HomeBidsRequestItemData(
  @SerializedName("material_type") val materialType: String,
  @SerializedName("creation_time") val creationTime: String,
  @SerializedName("pickup_location") val pickupLocation: String,
  @SerializedName("speed") val speed: String,
  @SerializedName("contract_price") val contractPrice: Int,
  @SerializedName("contract_id") val contractId: String,
  @SerializedName("client_name") val clientName: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("container_type") val containerType: String?,
  @SerializedName("truck_axle") val truckAxle: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("status") val status: String,
  @SerializedName("required_on") val _requiredOn: String,
  @SerializedName("pod_required") val podRequired: Boolean,
  @SerializedName("target_price") val targetPrice: Int,
  @SerializedName("request_type") val requestType: String,
  @SerializedName("truck_size") val truckSize: String?,
  @SerializedName("client_id") val clientId: String,
  @SerializedName("uuid") private val uuid: String?,
  @SerializedName("transaction_id") val transactionId: String?,
  @SerializedName("drop_location") val dropLocation: String,
  @SerializedName("truck_type") val truckType: String?,
  @SerializedName("updation_time") val updationTime: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("origin_city_code") val originCityCode: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("destination_city_code") val destinationCityCode: String,
  @SerializedName("truck_specifications") val truckSpecs: TruckSpecifications,
  @SerializedName("truck_display_name") val truckDisplayName: String?,
  var transactionBid: TransactionBid? = null,
  var showing: Boolean = false
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid ?: transactionId!!

  fun loadDetails() = "Load: ${StringUtils.capitalize(materialType) ?: "Not available"}"

  fun targetPrice() = "₹ ${String.format("%,d", targetPrice * 95 / 100)}"

  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  fun pickUpLocationName() = StringUtils.capitalize(pickupLocation) ?: ""

  fun bidAmount() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted -> "₹ ${String.format("%,d", transactionBid!!.bidAmount)}"
      else -> "₹ ${String.format("%,d", targetPrice * 95 / 100)}"
    }
  } else {
    "₹ ${String.format("%,d", targetPrice * 95 / 100)}"
  }

  fun amountLabel() = when (bidStatus()) {
    Open -> "Bid Price"
    Accepted -> "Confirmed Price"
    else -> "Trip Price"
  }

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
  fun truckTypeDetails() = truckDisplayName

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

  fun tripPriceDifference(): String {
    return transactionBid?.targetPriceDiff(targetPrice * 95 / 100) ?: ""
  }

  fun bidStatus() = TransactionBidStatus.byStatusKey(transactionBid?._status ?: "na")

  override fun filter(query: String) =
    origin.contains(query, true) || destination.contains(query, true)
        || originState.contains(query, true) || destinationState.contains(query, true)

  fun bidText() = "Bid placed for ₹ ${String.format("%, d", transactionBid?.bidAmount)}"
}

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"
const val HomeBidsRequestAction_PlaceBid = "place_bid"
const val HomeBidsRequestAction_AcceptBid = "accept_bid"

data class TruckSpecifications(
  @SerializedName("height") val height: Any?,
  @SerializedName("size") val size: String?,
  @SerializedName("axle") val axle: String?,
  @SerializedName("tonnage") val capacity: String?
)