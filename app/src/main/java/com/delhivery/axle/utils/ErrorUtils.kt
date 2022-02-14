package com.delhivery.axle.utils

import com.delhivery.axle.BuildConfig
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.response.ErrorResponseBody
import com.delhivery.axle.exception.HttpErrorCode
import com.delhivery.axle.exception.HttpErrorCode.Forbidden
import com.delhivery.axle.exception.HttpErrorCode.Unauthorized
import com.delhivery.axle.injection.scope.ActivityScope
import com.google.gson.Gson
import dagger.android.support.DaggerAppCompatActivity
import retrofit2.HttpException
import javax.inject.Inject

@ActivityScope
class ErrorUtils @Inject constructor(
  private val activity: DaggerAppCompatActivity,
  private val authRepository: AuthenticationRepository,
  private val uiUtils: UiUtils,
  private val dialogUtils: DialogUtils,
  private val gson: Gson,
  private val navigationUtils: NavigationUtils
) {

  /**
   * Handle exception
   */
  fun handle(throwable: Throwable) {
    when (throwable) {
      is HttpException -> throwable.handle()
      else -> dialogUtils.showErrorDialog(
          throwable.message ?: "Error: ${throwable.javaClass.simpleName}",
          ErrorDialogDismissTimeout
      )
    }
    if (BuildConfig.DEBUG) {
      /* print stack trace */
      throwable.printStackTrace()
    }
  }

  /**
   * Handle http exception
   */
  private fun HttpException.handle() {
    val errorCode = HttpErrorCode.exceptionFromCode(code())
    val errorResponseBody = try {
      gson.fromJson(
          response().errorBody()?.string(), ErrorResponseBody::class.java
      )
    } catch (e: Exception) {
      //parsing exception
      e.printStackTrace()
      null
    }

    val errorMessage = try {
      errorResponseBody?.errorBody?.errorMessage ?: errorCode.errorMessage
    } catch (e: Exception) {
      HttpErrorCode.UnknownError.errorMessage
    }

    when (errorCode) {
      Unauthorized, Forbidden -> navigationUtils.logout(errorMessage)
      else -> dialogUtils.showErrorDialog(errorMessage, ErrorDialogDismissTimeout)
    }
  }

  /**
   * Handle error repsonse body
   */
  fun handleErrorResponseBody(
    httpException: HttpException,
    errorResponseBody: ErrorResponseBody?
  ) {
    val errorCode = HttpErrorCode.exceptionFromCode(httpException.code())
    val errorMessage = errorResponseBody?.errorBody?.errorMessage ?: errorCode.errorMessage
    when (errorCode) {
      Unauthorized, Forbidden -> navigationUtils.logout(errorMessage)
      else -> dialogUtils.showErrorDialog(errorMessage, ErrorDialogDismissTimeout)
    }
  }

  /**
   * Handle error repsonse body
   */
  fun getErrorResponseBody(
    httpException: HttpException,
    errorResponseBody: ErrorResponseBody?
  ):String {
    val errorCode = HttpErrorCode.exceptionFromCode(httpException.code())
    val errorMessage = errorResponseBody?.errorBody?.errorMessage ?: errorCode.errorMessage
    when (errorCode) {
      Unauthorized, Forbidden ->{ navigationUtils.logout(errorMessage)
                                  return ""
      }
      else -> return errorMessage
    }

  }

}

/* dialog dismiss timeout in sec */
private const val ErrorDialogDismissTimeout = 3L