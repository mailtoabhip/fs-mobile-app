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
  @SerializedName("trip_status") private val _tripStatus: String,
  @SerializedName("vendor_id") val vendorId: String,
  @SerializedName("vendor_name") val vendorName: String,
  @SerializedName("vehicle") val vehicleDetails: TripVehicleDetails,
  @SerializedName("driver") val driverDetails: TripDriverDetails?,
  val id: String = UUID.randomUUID().toString()
) : BaseKeyTypeModel<String>() {
  override fun key() = transactionId

  /**
   * Formatted driver details as per UI
   */
  fun formattedDriverDetails() = "${driverDetails?.driverName} (${driverDetails?.driverPhoneNo})"

  /**
   * Trip Status [TripStatus]
   */
  fun status() = TripStatus.byKey(_tripStatus)
}

data class TripDriverDetails(
  @SerializedName("name") val driverName: String?,
  @SerializedName("phone_number") val driverPhoneNo: String?,
  @SerializedName("licence_number") val licenceNo: String?
)

data class TripVehicleDetails(
  @SerializedName("vehicle_number") val vehicleNo: String
)

enum class TripStatus(
  val statusKey: String,
  val status: String
) {
  TripCompleted("trip_completed", "Trip Completed"),
  TruckUnloaded("truck_unloaded", "Truck Unloaded"),
  TruckLoaded("truck_loaded", "Truck Loaded"),
  TruckConfirmed("truck_confirmed", "Truck Confirmed"),
  TruckArrived("truck_arrived", "Truck Arrived"),
  TruckReached("truck_reached", "Truck Reached"),
  TripCancelled("trip_cancelled", "Trip Cancelled"),
  InTrasit("in_transit", "In Transit"),
  Unknown("unknown", "Unknown");

  companion object {

    /**
     * Get [TripStatus] from response key
     */
    fun byKey(statusKey: String) =
      values().filter { it.statusKey.equals(statusKey, true) }.firstOrNull() ?: Unknown
  }
}