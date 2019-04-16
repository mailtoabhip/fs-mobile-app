package com.delhivery.orion.data.home

import android.support.annotation.DrawableRes
import com.delhivery.orion.R
import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.utils.DatePatterns
import com.delhivery.orion.utils.DateUtils
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
  @SerializedName("transaction_id") private val transactionId: String?,
  @SerializedName("drop_location") val dropLocation: String,
  @SerializedName("truck_type") val truckType: String?,
  @SerializedName("updation_time") val updationTime: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("origin_city_code") val originCityCode: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("destination_city_code") val destinationCityCode: String,
  @SerializedName("truck_specifications") val truckSpecs: TruckSpecifications,
  @SerializedName("truck_display_name") val truckDisplayName: String?
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid ?: transactionId!!

  fun loadDetails() = "Load: $materialType"

  @DrawableRes
  fun truckTypeDrawableRes() = when (containerType) {
    "closed" -> R.drawable.ic_closed_truck
    else -> R.drawable.ic_open_truck
  }

  /**
   * Formatted required at
   */
  fun requiredAt(): String {
    val requiredOn = DateUtils.parseDate(_requiredOn, DatePatterns.OrionDateFormat)
    val _diff = DateUtils.daysDiff(requiredOn)
    val reqDate = when (_diff) {
      0 -> "Today"
      1 -> "Tomorrow"
      else -> DateUtils.formatDate(requiredOn, "dd MMM,")
    }
    val reqTime = DateUtils.formatDate(requiredOn, "hh:mm a")
    return "$reqDate $reqTime"
  }

  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    when (DateUtils.daysDiff(DateUtils.parseDate(_requiredOn, DatePatterns.OrionDateFormat))) {
      0 -> R.drawable.bg_date_today
      1 -> R.drawable.bg_date_tomorrow
      else -> R.drawable.bg_date_others
    }

  /**
   * Get truck details/type
   */
  fun truckTypeDetails() = "$truckDisplayName"
}

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"

data class TruckSpecifications(
  @SerializedName("height") val height: Any?,
  @SerializedName("size") val size: String?,
  @SerializedName("axle") val axle: String?,
  @SerializedName("tonnage") val capacity: String?
)