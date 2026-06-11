package com.delhivery.axle.network

import android.util.Log
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.utils.prefs.UserPrefs
import okhttp3.Interceptor
import okhttp3.Request
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton Delhivery Network Interceptor as per requirements
 */

@Singleton
class DelhiveryNetworkInterceptor @Inject constructor(
  var userPrefs: UserPrefs,
  var connectionLiveData: ConnectionLiveData,
  private val deviceInfoProvider: DeviceInfoProvider
) : Interceptor {

  /**
   * Update jwt token — kept for backward compatibility with callers that
   * explicitly refresh the cached value, but the interceptor now always
   * reads directly from [userPrefs] so stale-cache issues are impossible.
   */
  fun updateJWT(jwtToken: String? = null) {
    userPrefs.jwtToken = jwtToken
  }

  override fun intercept(chain: Interceptor.Chain) =
    chain.request().newBuilder().let { builder: Request.Builder ->

        /* Block request if no internet connection */
        if (!connectionLiveData.isConnected()) {
            throw IOException("No internet connection")
        }

      /* Device & request metadata headers — only these go out */
      val requestId = UUID.randomUUID().toString()
      builder.header("X-Device-Id", deviceInfoProvider.deviceId)
      builder.header("X-Device-Model", deviceInfoProvider.deviceModel)
      builder.header("X-App-Version", deviceInfoProvider.appVersion)
      builder.header("X-Platform", deviceInfoProvider.platform)
      builder.header("X-OS-Version", deviceInfoProvider.osVersion)
      builder.header("X-Client-Ip", deviceInfoProvider.awaitPublicIp())
      builder.header("X-Request-Id", requestId)

      val path = chain.request().url().encodedPath()

      /* Attach Bearer token for all authenticated endpoints */
      val isUnauthenticated = path == "/api/v1/auth/initiate"
          || path == "/api/v1/auth/verify"
          || path == "/api/v1/auth/resend"
      if (!isUnauthenticated) {
        userPrefs.jwtToken?.let { token ->
          builder.header("Authorization", "Bearer $token")
          if (BuildConfig.DEBUG) {
            Log.d("DelhiveryInterceptor", "Authorization → Bearer ${token.take(8)}…")
          }
        }
      }

      if (BuildConfig.DEBUG) {
        Log.d("DelhiveryInterceptor", "[$path] Headers → X-Device-Id=${deviceInfoProvider.deviceId}, " +
            "X-Device-Model=${deviceInfoProvider.deviceModel}, " +
            "X-App-Version=${deviceInfoProvider.appVersion}, " +
            "X-Platform=${deviceInfoProvider.platform}, " +
            "X-OS-Version=${deviceInfoProvider.osVersion}, " +
            "X-Request-Id=$requestId, " +
            "X-Client-Ip=${deviceInfoProvider.awaitPublicIp()}")
      }

      chain.proceed(builder.build())
    }

}