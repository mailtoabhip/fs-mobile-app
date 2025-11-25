package com.delhivery.axle.tokenExpiryHandling

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.config.UrlConfig.AppID
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RefreshTokenWorker(
    appContext: Context,
    params: WorkerParameters,
    private val userPrefs: UserPrefs,
    private val okHttpClient: OkHttpClient,
    private val authInterceptor: DelhiveryNetworkInterceptor
) : CoroutineWorker(appContext, params) {
    
    companion object {
        const val WORK_NAME = "RefreshTokenWorker"
        private const val TAG = "RefreshTokenWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Token refresh work started")
            
            if (userPrefs.jwtToken == null) {
                Log.d(TAG, "No JWT token found, skipping refresh")
                return Result.success()
            }

            val url = if (BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat") {
                "https://api-stage-ums.delhivery.com/v2/refresh_token/?force=1"
            } else {
                "https://api-ums.delhivery.com/v2/refresh_token/?force=1"
            }

            refreshToken(url)
            Log.d(TAG, "Token refresh completed successfully")
            Result.success()
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during token refresh", e)
            Result.retry()
        } catch (e: IOException) {
            Log.e(TAG, "Network error during token refresh", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during token refresh", e)
            Result.failure()
        }
    }

    private suspend fun refreshToken(url: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .addHeader("X-APP-ID", AppID.url())
            .build()

        val response = okHttpClient.newCall(request).await()
        
        try {
            val strResponse = response?.body?.string()
            if (strResponse != null) {
                val json = JSONObject(strResponse)
                if (!json.isNull("jwt")) {
                    val jwtToken = json.optString("jwt")
                    if (jwtToken.isNotNullOrEmpty()) {
                        userPrefs.jwtToken = jwtToken
                        authInterceptor.updateJWT(jwtToken)
                        Log.d(TAG, "JWT token updated successfully")
                    } else {
                        Log.w(TAG, "Received empty JWT token")
                    }
                } else {
                    Log.w(TAG, "No JWT field in response")
                }
            } else {
                Log.w(TAG, "Response body is null")
            }
        } finally {
            response.close()
        }
    }

    /**
     * Converts OkHttp Call to a suspend function using coroutines
     */
    private suspend fun okhttp3.Call.await(): Response = suspendCancellableCoroutine { continuation ->
        enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancel exceptions
            }
        }
    }

    class Factory @Inject constructor(
        val userPrefs: UserPrefs,
        val okHttpClient: OkHttpClient,
        val authInterceptor: DelhiveryNetworkInterceptor
    ) : DaggerWorkerFactory.ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
            RefreshTokenWorker(appContext, params, userPrefs, okHttpClient, authInterceptor)
    }
}