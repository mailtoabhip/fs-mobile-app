package com.delhivery.axle.network

import android.util.Log
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import okhttp3.Interceptor
import okhttp3.Request

/**
 * Singleton Delhivery Network Interceptor as per requirements
 */
class DelhiveryNetworkInterceptor private constructor() : Interceptor {
  companion object {
    /* public singleton lazy instance */
    val instance: DelhiveryNetworkInterceptor by lazy {
      DelhiveryNetworkInterceptor()
    }
  }

  private var jwtToken: String? = null

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