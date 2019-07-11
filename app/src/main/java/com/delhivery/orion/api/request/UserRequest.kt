package com.delhivery.orion.api.request

import com.delhivery.orion.data.RouteMappingModel
import com.google.gson.annotations.SerializedName

data class UpdateUserBaseCityRequest(
  @SerializedName("base_city") val city: String,
  @SerializedName("base_city_code") val cityCode: String,
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
)

data class UpdateUserRoutesRequest(
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
)