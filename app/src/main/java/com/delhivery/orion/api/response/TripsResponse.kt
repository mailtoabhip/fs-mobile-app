package com.delhivery.orion.api.response

import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.google.gson.annotations.SerializedName

data class TripsResponse(
  @SerializedName("count") val total: Int,
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("results") val trips: List<HomeTripsItemData>
)

data class TripSummaryResponse(
  @SerializedName("in_transit") val inTransit: Int,
  @SerializedName("trip_cancelled") val tripCancelled: Int,
  @SerializedName("trip_completed") val tripCompleted: Int,
  @SerializedName("truck_arrived") val truckArrived: Int,
  @SerializedName("truck_confirmed") val truckConfirmed: Int,
  @SerializedName("truck_loaded") val truckLoaded: Int,
  @SerializedName("truck_reached") val truckReached: Int,
  @SerializedName("truck_unloaded") val truckUnloaded: Int
)