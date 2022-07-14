package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ResetKycDataRequest(
  @SerializedName("phone_number") var phoneNumber: String = "",
  @SerializedName("reset_point") var resetPoint: String = "",
  @SerializedName("pan_number") var panNumber: String? = null,
  @SerializedName("gst_number") val gstNumber:String? = null,
  @SerializedName("is_address_same_as_gst") val isAddressSameAsGST:Boolean? = null
)