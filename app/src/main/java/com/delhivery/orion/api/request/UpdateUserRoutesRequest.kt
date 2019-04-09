package com.delhivery.orion.api.request

import com.delhivery.orion.data.RouteMappingModel
import com.google.gson.annotations.SerializedName

data class UpdateUserRoutesRequest(
  @SerializedName("lane_preferences") val routes:List<RouteMappingModel>
)