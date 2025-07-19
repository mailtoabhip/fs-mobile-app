package com.delhivery.axle.utils.extensions

import android.util.Log
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ErrorResponseBody
import com.delhivery.axle.api.response.PaymentErrorResponseBody
import com.delhivery.axle.api.response.TPSBaseResponse
import com.delhivery.axle.api.response.TPSErrorBody
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
    Log.d("DelhiveryExtension", it.responseData.toString())
    if (it.isSuccess) {
      try {
        it.responseData
      } catch (e:Exception){
        Log.d("DelhiveryExcept", it.responseData.toString())
        it.responseData
      }
    } else {
      Log.d("DelhiveryExcept", it.responseData.toString())
      throw it.toHttpException()
    }
  }

fun <M : Any, T : TPSBaseResponse<M>> Single<T>.convertTPSResponse(): Single<M> =
  map {
    if (it.message=="Success") {
      it.responseData
    } else {
      throw it.toHttpException()
    }
  }

/**
 * Handle response and based on [BaseMessageResponse.isSuccess] flag,
 * response is passed or exception is thrown
 *
 */
fun Single<BaseMessageResponse>.convertMessageResponse(): Single<String> =
  map {
    if (it.isSuccess) {
      it.message
    } else {
      throw it.toHttpException()
    }
  }

/**
 * Get [ErrorResponseBody] from response throwable
 * */
fun Throwable.errorResponseBody() = if (this is HttpException) {
  val errorResponseBody = try {
    Gson().fromJson(response()?.errorBody()?.string(), ErrorResponseBody::class.java)
  } catch (e: Exception) {
    //parsing exception
    e.printStackTrace()
    null
  }
  errorResponseBody
} else {
  null
}

fun Throwable.errorPaymentResponseBody() = if (this is HttpException) {
  val errorResponseBody = try {
    Gson().fromJson(response()?.errorBody()?.string(), PaymentErrorResponseBody::class.java)
  } catch (e: Exception) {
    //parsing exception
    e.printStackTrace()
    null
  }
  errorResponseBody
} else {
  null
}

fun Throwable.errorTPSResponseBody() = if (this is HttpException) {
    val errorResponseBody = try {
        Gson().fromJson(response()?.errorBody()?.string(), TPSErrorBody::class.java)
    } catch (e: Exception) {
        //parsing exception
        e.printStackTrace()
        null
    }
    errorResponseBody
} else {
    null
}