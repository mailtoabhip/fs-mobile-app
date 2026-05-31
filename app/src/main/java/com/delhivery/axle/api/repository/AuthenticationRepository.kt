package com.delhivery.axle.api.repository

import android.util.Log
import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.request.OTPLoginRequest
import com.delhivery.axle.api.request.PasswordLoginRequest
import com.delhivery.axle.api.request.RequestOTP
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.api.service.UMSService
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.ErrorLogger
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
  private val userPrefs: UserPrefs,
  private val networkInterceptor: DelhiveryNetworkInterceptor,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  init {
    /* Update JWT token from prefs to Network Interceptor */
    networkInterceptor.updateJWT(userPrefs.jwtToken)
  }

  /**
   * Handle jwt token post success login
   */
  private fun handleJWTToken(jwtToken: String) {
    try {
      // Validate token can be parsed before storing
      // Trim whitespace and validate token is not empty
      val trimmedToken = jwtToken.trim()
      if (trimmedToken.isEmpty()) {
        Log.e("Auth", "JWT token is empty after trimming")
        return
      }
      Log.d("Auth", "Attempting to parse JWT token, length: ${trimmedToken.length}")
      JWT(trimmedToken)
      userPrefs.jwtToken = trimmedToken
      networkInterceptor.updateJWT(trimmedToken)
      Log.d("Auth", "JWT token successfully stored")
    } catch (e: Exception) {
      // Log error and don't store invalid token
      Log.e("Auth", "Invalid JWT token received", e)
      Log.e("Auth", "Token that failed: ${jwtToken.take(100)}...")
      Log.e("Auth", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
      e.printStackTrace()
    }
  }

  /**
   * Logout current user
   */
  fun logout(intent :String = "notFromUser") {
    if(intent== "notFromUser"){
      userPrefs.lastLoggedInUserId = userPrefs.userId()
      //analyticsUtil.trackEvent(EVENT_AUTO_LOGOUT)
    }
    networkInterceptor.updateJWT(null)
    userPrefs.clearPrefs()
    /* delete user pref db's and other user-related cache */
  }

  /**
   * Get auth status if user authenticated or not
   */
  fun authStatus(): Boolean {
    if (!userPrefs.jwtToken.isNotNullOrEmpty()) {
      return false
    }
    return try {
      val expiresAt = JWT(userPrefs.jwtToken!!).expiresAt
      if (expiresAt != null && expiresAt.after(Date())) {
        true
      } else {
        /* expired: clear credentials and logout */
        logout()
        false
      }
    } catch (e: Exception) {
      /* malformed token: clear credentials and logout */
      logout()
      false
    }
  }
}