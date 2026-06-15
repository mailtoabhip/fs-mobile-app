package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class PopularLocationsResponse(
  @SerializedName("city") val city: String,
  @SerializedName("city_code") val cityCode: String?
)
