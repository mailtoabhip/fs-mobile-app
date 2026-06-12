package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class UpdateRouteRequest(
  @SerializedName("origin") var origin: RouteDetails,
  @SerializedName("destinations") var destination: List<RouteDetails>,
  @SerializedName("old_origin") var oldOrigin: RouteDetails?
)
