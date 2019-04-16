package com.delhivery.orion.api.response

import com.delhivery.orion.data.home.HomeTripsItemData
import com.google.gson.annotations.SerializedName

data class TripsResponse(
  @SerializedName("count") val total: Int,
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("results") val trips: List<HomeTripsItemData>
)