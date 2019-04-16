package com.delhivery.orion.utils

import android.content.Intent
import com.delhivery.orion.BuildConfig
import com.delhivery.orion.api.response.ErrorResponseBody
import com.delhivery.orion.exception.HttpErrorCode
import com.delhivery.orion.exception.HttpErrorCode.Forbidden
import com.delhivery.orion.exception.HttpErrorCode.Unauthorized
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.auth.AuthenticationActivity
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
  private val gson: Gson
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
      gson.fromJson<ErrorResponseBody>(
          response().errorBody()?.string(), ErrorResponseBody::class.java
      )
    } catch (e: Exception) {
      //parsing exception
      e.printStackTrace()
      null
    }
    val errorMessage = errorResponseBody?.errorBody?.errorMessage ?: errorCode.errorMessage
    when (errorCode) {
      Unauthorized, Forbidden -> tokenExpired(errorMessage)
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
      Unauthorized, Forbidden -> tokenExpired(errorMessage)
      else -> dialogUtils.showErrorDialog(errorMessage, ErrorDialogDismissTimeout)
    }
  }

  /**
   * Handle token expired/forbidden
   */
  private fun tokenExpired(message: String) {
    authRepository.logout()
    uiUtils.showToast(message)
    Intent(activity, AuthenticationActivity::class.java)
        .let {
          activity.startActivity(it)
        }
    activity.finish()
  }
}

/* dialog dismiss timeout in sec */
private const val ErrorDialogDismissTimeout = 3L