package com.delhivery.orion.api.response

import com.google.gson.annotations.SerializedName

/**
 * Base Response for all APIs
 */
data class BaseResponse<M : Any>(
  @SerializedName("data") val responseData: M,
  @SerializedName("success") val isSuccess: Boolean,
  @SerializedName("error_code") val errorCode: Int,
  @SerializedName("error_message") val errorMessage: String
)
