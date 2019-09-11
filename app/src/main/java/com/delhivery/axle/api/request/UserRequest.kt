package com.delhivery.axle.api.request

import com.delhivery.axle.data.RouteMappingModel
import com.google.gson.annotations.SerializedName

data class UpdateUserBaseCityRequest(
  @SerializedName("base_city") val city: String,
  @SerializedName("base_city_code") val cityCode: String,
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
)

data class UpdateUserRoutesRequest(
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
)

data class UpdateUserAccessRequest(
  @SerializedName("accessed_by_axle") val accessed: Boolean = true
)