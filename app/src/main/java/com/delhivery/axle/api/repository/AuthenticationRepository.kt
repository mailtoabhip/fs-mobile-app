package com.delhivery.axle.api.repository

import android.util.Log
import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.request.OTPLoginRequest
import com.delhivery.axle.api.request.PasswordLoginRequest
import com.delhivery.axle.api.request.RequestOTP
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.api.service.UMSService
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.EVENT_AUTO_LOGOUT
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository
 */
@Singleton
class AuthenticationRepository @Inject constructor(
  private val umsService: UMSService,
  private val loadBoardService: LoadBoardService,
  private val userPrefs: UserPrefs
) : BaseRepository() {

  init {
    /* Update JWT token from prefs to Network Interceptor */
    DelhiveryNetworkInterceptor.instance.updateJWT(userPrefs.jwtToken)
  }

  /**
   * Send otp to phone number and return if success and error message
   */
  fun sendOTP(phoneNo: String) =
    loadBoardService.requestOTP(RequestOTP.getRequest(phoneNo))
      .map {
        Pair(true, it.successMsg)
      }
      .onErrorReturn {
        /* handle error if needed */
        Pair(false, it.errorResponseBody()?.errorBody?.errorMessage.toString())
      }

  /**
   * Verify OTP
   */
  fun verifyOTP(
    phoneNo: String,
    otp: String
  ) = loadBoardService.otpLogin(OTPLoginRequest.getRequest(phoneNo, otp))
      .map {
        it.responseData?.jwtToken?.let { it1 -> handleJWTToken(it1) }
        Pair(true, "")
      }
      .onErrorReturn {
        /* handle error if needed */
        Pair(false, "Invalid OTP")
      }

  /**
   * Verify Password Login
   */
  fun loginUsingPassword(
    userName: String,
    password: String
  ) = umsService.requestPasswordVerification(PasswordLoginRequest.getRequest(userName, password))
    .map {
      handleJWTToken(it.jwtToken)
      Pair(true, "")
    }
    .onErrorReturn {
      /* handle error if needed */
      Pair(false, "Invalid Password")
    }

  /**
   * Handle jwt token post success login
   */
  private fun handleJWTToken(jwtToken: String) {
    userPrefs.jwtToken = jwtToken
    DelhiveryNetworkInterceptor.instance.updateJWT(jwtToken)
  }

  /**
   * Logout current user
   */
  fun logout(intent :String = "notFromUser") {
    if(intent== "notFromUser"){
      //analyticsUtil.trackEvent(EVENT_AUTO_LOGOUT)
    }
    DelhiveryNetworkInterceptor.instance.updateJWT(null)
    userPrefs.clearPrefs()
    /* delete user pref db's and other user-related cache */
  }

  /**
   * Get auth status if user authenticated or not
   */
  fun authStatus() = when (userPrefs.jwtToken.isNotNullOrEmpty()) {
    true -> JWT(userPrefs.jwtToken!!).expiresAt.let { expiresAt ->
      when (expiresAt != null && expiresAt.after(Date())) {
        true -> true
        false -> {
          /* expired: clear credentials and logout */
          logout()
          false
        }
      }
    }
    false -> false
  }
}