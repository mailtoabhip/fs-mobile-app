package com.delhivery.axle.api.response

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * Base Response for all APIs with data key
 */
data class BaseResponse<M : Any>(
  @SerializedName("data") val responseData: M?,
  @SerializedName("success") val isSuccess: Boolean,
  @SerializedName("error") val errorBody: BaseErrorResponse?
) {
  /**
   * Convert to [HttpException] when success is false
   */
  fun toHttpException(): HttpException {
    val responseBody = ResponseBody.create(MediaType.parse("application/json"), Gson().toJson(this))
    val response = Response.error<Any>(errorBody?.errorCode() ?: 400, responseBody)
    return HttpException(response)
  }
}

/**
 * Base Message response for all APIs with only message and success key
 */
data class BaseMessageResponse(
  @SerializedName("message") val message: String,
  @SerializedName("success") val isSuccess: Boolean,
  @SerializedName("error") val errorBody: BaseErrorResponse?
) {
  /**
   * Convert to [HttpException] when success is false
   */
  fun toHttpException(): HttpException {
    val responseBody = ResponseBody.create(MediaType.parse("application/json"), Gson().toJson(this))
    val response = Response.error<Any>(errorBody?.errorCode() ?: 400, responseBody)
    return HttpException(response)
  }
}

/**
 * Base error response
 */
data class BaseErrorResponse(
  @SerializedName("message") val errorMessage: String,
  @SerializedName("code") private val _errorCode: Int,
  @SerializedName("data") val data: List<ObjectErrorData>?= null
) {
  /**
   * Error code as [Integer]
   */
  fun errorCode() = _errorCode
}
data class ObjectErrorData(
  @SerializedName("is_duplicate_pan") val isDuplicatePan: Boolean?
)

/**
 * Error response body
 */
data class ErrorResponseBody(
  @SerializedName("success") val isSuccess: Boolean,
  @SerializedName("error") val errorBody: BaseErrorResponse
)