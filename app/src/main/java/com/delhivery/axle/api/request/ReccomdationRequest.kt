package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ReccomdationRequest(
  @SerializedName("sp_id") val spId: String,
  @SerializedName("limit") val limit: Int,
  @SerializedName("offset") val offset: Int,
  @SerializedName("demand_types") val vendorType: String ? = null,
  @SerializedName("truck_types") val vehicleType: String? = null,
  @SerializedName("split_view_count")val splitViewCount:Boolean?=null,
  @SerializedName("only_count")val onlyCount:Boolean?=null,
  @SerializedName("loads_active") val loadActive:Boolean =false
)
