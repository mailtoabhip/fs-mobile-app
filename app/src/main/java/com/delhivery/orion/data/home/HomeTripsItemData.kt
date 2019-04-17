package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class HomeTripsItemData(
  @SerializedName("action_time") val actionTime: String,
  @SerializedName("arrival_time") val arrivalTime: String?,
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
  @SerializedName("bid_details") val bidDetails: TripBidDetails?,
  @SerializedName("city_code") val cityCode: String?,
  @SerializedName("current_location") val currentLocation: String?,
  @SerializedName("origin_city_id") val originCityId: String?,
  @SerializedName("destination_city_id") val destinationCityId: String?,
  @SerializedName("is_advance_required") val isAdvancePaymentRequired: Boolean?,
  @SerializedName("is_epod_available") val isEPodAvailable: Boolean?,
  @SerializedName("loading_advice") val loadingAdvice: String?,
  @SerializedName("loading_location") val loadingLocation: String?,
  @SerializedName("reached_time") val reachedTime: String?,
  @SerializedName("required_on") val requiredOn: String?,
  @SerializedName("unloading_location") val unloadingLocation: String?,
  @SerializedName("user_name") val userName: String?
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

data class TripBidDetails(
  @SerializedName("advance_payout") val advancePayout: Int?,
  @SerializedName("bid_price") val bidPrice: Int?,
  @SerializedName("effective_price") val effectivePrice: Int?,
  @SerializedName("fuel_payout") val fuelPayout: Int?
)

enum class TripStatus(
  val statusKey: String,
  val status: String
) {
  InTrasit("in_transit", "In Transit"),
  TripCancelled("trip_cancelled", "Trip Cancelled"),
  TripCompleted("trip_completed", "Trip Completed"),
  TruckArrived("truck_arrived", "Truck Arrived"),
  TruckConfirmed("truck_confirmed", "Truck Confirmed"),
  TruckLoaded("truck_loaded", "Truck Loaded"),
  TruckReached("truck_reached", "Truck Reached"),
  TruckUnloaded("truck_unloaded", "Truck Unloaded"),
  Unknown("unknown", "Unknown");

  companion object {

    /**
     * Get [TripStatus] from response key
     */
    fun byKey(statusKey: String) =
      values().filter { it.statusKey.equals(statusKey, true) }.firstOrNull() ?: Unknown
  }
}