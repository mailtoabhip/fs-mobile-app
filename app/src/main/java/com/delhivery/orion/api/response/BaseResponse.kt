package com.delhivery.orion.api.response

import com.google.gson.annotations.SerializedName

/**
 * Base Response for all APIs
 */
data class BaseResponse<M : Any>(
  @SerializedName("data") val responseData: M?,
  @SerializedName("success") val isSuccess: Boolean,
  @SerializedName("error") val errorBody: BaseErrorResponse?
)

data class BaseErrorResponse(
  @SerializedName("message") val errorMessage: String,
  @SerializedName("code") private val _errorCode: String
) {
  /**
   * Error code as [Integer]
   */
  fun errorCode() = Integer.parseInt(_errorCode)
}