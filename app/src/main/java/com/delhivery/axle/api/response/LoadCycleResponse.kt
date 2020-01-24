package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.google.gson.annotations.SerializedName

/**
 * Response container for search api
 */
data class SearchTripsResponse(
  @SerializedName("count") val total: Int,
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("results") val trips: List<HomeTripsItemData>
)