package com.delhivery.axle.network

import android.util.Log
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import okhttp3.Interceptor
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton Delhivery Network Interceptor as per requirements
 */

@Singleton
class DelhiveryNetworkInterceptor @Inject constructor(
  var userPrefs: UserPrefs,
  var connectionLiveData: ConnectionLiveData
) : Interceptor {

  private var jwtToken: String? = userPrefs.jwtToken

  /**
   * Update jwt token
   *
   * @param jwtToken new JWT Token, by  default clears token
   */
  fun updateJWT(jwtToken: String? = null) {
    this.jwtToken = jwtToken
  }

  override fun intercept(chain: Interceptor.Chain) =
    chain.request().newBuilder().let { builder: Request.Builder ->
      /* Block request if no internet connection */
      if (!connectionLiveData.isConnected()) {
        throw IOException("No internet connection")
      }
      if (jwtToken.isNotNullOrEmpty()) {
        if (BuildConfig.DEBUG) {
          Log.d("Authorization", "Bearer $jwtToken")
        }
        builder.addHeader("Authorization", "Bearer $jwtToken")
      } else {
        Log.d("DelhiveryInterceptor", "intercept:: no jwt token")
      }
      /* request for json response */
      builder.addHeader("Accept", "application/json")
      chain.proceed(builder.build())
    }

}