package com.delhivery.axle.utils.extensions

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ErrorResponseBody
import com.google.gson.Gson
import io.reactivex.Single
import retrofit2.HttpException

/**
 * Handle response and based on [BaseResponse.isSuccess] flag,
 * response is passed or exception is thrown
 *
 */
fun <M : Any, T : BaseResponse<M>> Single<T>.convertResponse(): Single<M> =
  map {
    if (it.isSuccess) {
      return@map it.responseData
    } else {
      throw it.toHttpException()
    }
  }

/**
 * Get [ErrorResponseBody] from response throwable
 * */
fun Throwable.errorResponseBody() = if (this is HttpException) {
  val errorResponseBody = try {
    Gson().fromJson<ErrorResponseBody>(
        response()?.errorBody()?.string(), ErrorResponseBody::class.java
    )
  } catch (e: Exception) {
    //parsing exception
    e.printStackTrace()
    null
  }
  errorResponseBody
} else {
  null
}