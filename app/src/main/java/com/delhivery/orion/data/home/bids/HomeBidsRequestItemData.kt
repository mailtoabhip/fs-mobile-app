package com.delhivery.orion.data.home.bids

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.utils.ColorProviderUtils
import com.delhivery.orion.utils.DatePatterns
import com.delhivery.orion.utils.DateUtils
import com.delhivery.orion.utils.DrawableProviderUtils
import com.delhivery.orion.utils.StringUtils
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

  fun targetPrice() = "₹ $targetPrice"

  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  fun pickUpLocationName() = StringUtils.capitalize(pickupLocation) ?: ""

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
    ColorProviderUtils.getStatusColor(bidStatus().toLowerCase())

  /**
   * Get truck details/type
   */
  fun truckTypeDetails() = truckDisplayName

  /**
   * Trip display name for toolbar title
   */
  fun tripDisplayName() =
    "${StateModel.idFromName(originState)} - ${StateModel.idFromName(
        destinationState
    )} (${DateUtils.daysDiffStr(_requiredOn, DatePatterns.OrionDateFormat)})".toUpperCase()

  fun tripPriceDifference(): String {
    return transactionBid?.targetPriceDiff(targetPrice) ?: ""
  }

  fun bidStatus() = BidStatus.byKey(status)

  override fun filter(query: String) =
    origin.contains(query, true) || destination.contains(query, true)
        || originState.contains(query, true) || destinationState.contains(query, true)

  fun bidText() = "Bid successfully placed for ₹ ${transactionBid?.bidAmount}"
}

enum class BidStatus(
  val statusKey: String,
  val status: String
) {
  Requested("requested", "Active"),
  TruckConfirmed("truck_confirmed", "Confirmed"),
  TruckLoaded("truck_loaded", "Truck Loaded"),
  TruckReached("truck_reached", "Truck Reached"),
  TruckUnloaded("truck_unloaded", "Truck Unloaded"),
  Unknown("unknown", "Unknown");

  companion object {

    /**
     * Get [TripStatus] from response key
     */
    fun byKey(statusKey: String) =
      values().filter { it.statusKey.equals(statusKey, true) }.firstOrNull()?.status ?: statusKey
  }
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