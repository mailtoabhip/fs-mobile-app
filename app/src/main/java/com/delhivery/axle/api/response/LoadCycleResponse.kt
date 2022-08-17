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

data class FrequentTripsResponse(
        @SerializedName("count") val total: Int,
        @SerializedName("has_next") val hasNext: Boolean,
        @SerializedName("results") val trips: List<FreqTripsItemData>
)

data class FreqTripsItemData(
        @SerializedName("origin_city_id") val originCityId: String?,
        @SerializedName("destination_city_id") val destinationCityId: String?,
        @SerializedName("truck_display_name") val truckDisplayName: String?,
        @SerializedName("vis_value") val visValue: Boolean? = null)