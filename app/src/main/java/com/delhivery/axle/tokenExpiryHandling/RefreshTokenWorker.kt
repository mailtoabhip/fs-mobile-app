package com.delhivery.axle.tokenExpiryHandling

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.SyncOfferData.MyWorker
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.PriceRepository
import com.delhivery.axle.config.UrlConfig.AppID
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RefreshTokenWorker(appContext: Context, params: WorkerParameters,var userPrefs: UserPrefs, var okHttpClient: OkHttpClient,var authInterceptor: DelhiveryNetworkInterceptor) :
    CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "RefreshTokenWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d("prefs","Work Started")
            if(!isMyServiceRunning(RefreshAuthTokenService::class.java)) {
                val intent = Intent(applicationContext, RefreshAuthTokenService::class.java)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                  validateAndRefreshToken()
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(intent)
                } else {
                    applicationContext.startService(intent)
                }
            }
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = applicationContext.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                return true
            }
        }
        return false
    }

  private fun validateAndRefreshToken() {
    Log.d("prefs","service")

    if (userPrefs.jwtToken != null) {
      Log.d("prefs","service2")
      if (BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat") {
        refreshToken("https://api-stage-ums.delhivery.com/v2/refresh_token/?force=1")
      } else {
        refreshToken("https://api-ums.delhivery.com/v2/refresh_token/?force=1")
      }
    }
  }

  private fun refreshToken(url: String) {
    val request = Request.Builder()
      .url(url)
      .addHeader("X-APP-ID", AppID.url())
      .build()

    okHttpClient.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {

      }

      override fun onResponse(call: Call, response: Response) {
        try {
          val strResponse = response?.body()?.string()
          val json = JSONObject(strResponse)
          if (!json.isNull("jwt")) {
            val jwtToken = json.optString("jwt")
            if (jwtToken.isNotNullOrEmpty()){
              userPrefs.jwtToken = jwtToken
              authInterceptor.updateJWT(jwtToken)
            }
          }
          WorkManager.getInstance(applicationContext).cancelUniqueWork(RefreshTokenWorker.WORK_NAME)
        } catch (e: Exception){
          e.printStackTrace()
        }
      }
    })
  }

  class Factory @Inject constructor(
    val userPrefs: UserPrefs,
    val okHttpClient: OkHttpClient,
    val authInterceptor: DelhiveryNetworkInterceptor
  ) : DaggerWorkerFactory.ChildWorkerFactory {

    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
      RefreshTokenWorker(appContext, params,userPrefs, okHttpClient, authInterceptor  )
  }
}