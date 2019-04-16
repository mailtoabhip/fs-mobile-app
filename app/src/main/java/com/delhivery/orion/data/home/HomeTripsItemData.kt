package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class HomeTripsItemData(
  @SerializedName("action_time") val actionTime: String,
  @SerializedName("client_id") val clientId: String,
  @SerializedName("client_name") val clientName: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("origin") val origin: String,
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("trip_status") val tripStatus: String,
  @SerializedName("vendor_id") val vendorId: String,
  @SerializedName("vendor_name") val vendorName: String,
  @SerializedName("vehicle") val vehicleDetails: TripVehicleDetails,
  @SerializedName("driver") val driverDetails: TripDriverDetails?,
  val id: String = UUID.randomUUID().toString()
) : BaseKeyTypeModel<String>() {
  override fun key() = id
}

data class TripDriverDetails(
  @SerializedName("name") val driverName: String?,
  @SerializedName("phone_number") val driverPhoneNo: String?,
  @SerializedName("licence_number") val licenceNo: String?
)

data class TripVehicleDetails(
  @SerializedName("vehicle_number") val vehicleNo: String
)