package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ReccomdationRequest(
  @SerializedName("sp_id") val spId: String,
  @SerializedName("limit") val limit: Int,
  @SerializedName("offset") val offset: Int

)
