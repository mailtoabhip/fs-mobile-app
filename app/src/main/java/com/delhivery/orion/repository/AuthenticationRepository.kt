package com.delhivery.orion.repository

import com.auth0.android.jwt.JWT
import com.delhivery.orion.api.UMSService
import com.delhivery.orion.api.request.OTPLoginRequest
import com.delhivery.orion.api.request.RequestOTP
import com.delhivery.orion.network.DelhiveryNetworkInterceptor
import com.delhivery.orion.utils.extensions.isNotNullOrEmpty
import com.delhivery.orion.utils.prefs.UserPrefs
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository
 */
@Singleton
class AuthenticationRepository @Inject constructor(
  private val umsService: UMSService,
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
    umsService.requestOTP(RequestOTP.getRequest(phoneNo))
        .map {
          Pair(true, it.successMsg)
        }
        .onErrorReturn {
          /* handle error if needed */
          Pair(false, "Invalid phone number")
        }

  /**
   * Verify OTP
   */
  fun verifyOTP(
    phoneNo: String,
    otp: String
  ) = umsService.otpLogin(OTPLoginRequest.getRequest(phoneNo, otp))
      .map {
        handleJWTToken(it.jwtToken)
        Pair(true, "")
      }
      .onErrorReturn {
        /* handle error if needed */
        Pair(false, "Invalid OTP")
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
  fun logout() {
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