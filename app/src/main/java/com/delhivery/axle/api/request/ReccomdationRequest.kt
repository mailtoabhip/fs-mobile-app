package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class ReccomdationRequest(
  @SerializedName("sp_id") val spId: String,
  @SerializedName("limit") val limit: Int,
  @SerializedName("offset") val offset: Int,
  @SerializedName("demand_types") val vendorType: String ? = "orion",
  @SerializedName("truck_types") val vehicleType: String? = null
)
