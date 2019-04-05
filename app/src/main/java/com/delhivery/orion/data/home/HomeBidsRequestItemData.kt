package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class HomeBidsRequestItemData(
  @SerializedName("material_type") val materialType: String,
  @SerializedName("creation_time") val creationTime: String,
  @SerializedName("pickup_location") val pickupLocation: String,
  @SerializedName("speed") val speed: String,
  @SerializedName("contract_price") val contractPrice: String,
  @SerializedName("contract_id") val contractId: String,
  @SerializedName("client_name") val clientName: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("container_type") val containerType: String,
  @SerializedName("truck_axle") val truckAxle: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("status") val status: String,
  @SerializedName("required_on") val requiredOn: String,
  @SerializedName("pod_required") val podRequired: Boolean,
  @SerializedName("target_price") val targetPrice: String,
  @SerializedName("request_type") val requestType: String,
  @SerializedName("truck_size") val truckSize: String,
  @SerializedName("client_id") val clientId: String,
  @SerializedName("uuid") val uuid: String,
  @SerializedName("drop_location") val dropLocation: String,
  @SerializedName("truck_type") val truckType: String,
  @SerializedName("updation_time") val updationTime: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("origin_city_code") val originCityCode: String,
  @SerializedName("destination_state") val destinationState: String,
  @SerializedName("destination_city_code") val destinationCityCode: String
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid

  fun loadDetails() = "Load: $materialType"
}

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"