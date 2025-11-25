package com.delhivery.axle.tokenExpiryHandling

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.config.UrlConfig.AppID
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
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
        private const val MAX_RETRY_ATTEMPTS = 3
        const val INITIAL_BACKOFF_DELAY_MINUTES = 1L
    }

    override suspend fun doWork(): Result {
        val runAttemptCount = runAttemptCount
        Log.d(TAG, "Token refresh work started (attempt $runAttemptCount)")
        
        return try {
            if (userPrefs.jwtToken == null) {
                Log.d(TAG, "No JWT token found, skipping refresh")
                return Result.success()
            }

            val url = if (BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat") {
                "https://api-stage-ums.delhivery.com/v2/refresh_token/?force=1"
            } else {
                "https://api-ums.delhivery.com/v2/refresh_token/?force=1"
            }

            val refreshSuccess = refreshToken(url)
            if (refreshSuccess == 200) {
                Log.d(TAG, "Token refresh completed successfully, canceling periodic work")
                // Cancel the periodic work after successful refresh
                WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
            } else {
                // ❗ THROW HttpException WITH STATUS CODE
                val errorBody = ResponseBody.create(MediaType.parse("text/plain"), "Token refresh failed")
                throw HttpException(retrofit2.Response.error<Unit>(refreshSuccess, errorBody))
            }
            Result.success()
        } catch (e: HttpException) {
            val statusCode = e.code()
            Log.e(TAG, "HTTP error during token refresh: statusCode=$statusCode, attempt=$runAttemptCount", e)

            // Log to Firebase Crashlytics
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("refresh_token_attempt", runAttemptCount)
                setCustomKey("http_status_code", statusCode)
                setCustomKey("user_id", userPrefs.userId()?:"")
                setCustomKey("user_name", userPrefs.userName?:"")
                recordException(e)
            }
            
            // Don't retry on client errors (4xx) - these are likely permanent failures
            if (statusCode in 400..499) {
                Log.w(TAG, "Client error ($statusCode) - not retrying")
                return Result.failure()
            }
            
            // Retry on server errors (5xx) and other HTTP errors
            if (shouldRetry(runAttemptCount)) {
                Log.d(TAG, "Will retry after backoff delay")
                return Result.retry()
            } else {
                Log.e(TAG, "Max retry attempts reached for HTTP error")
                return Result.failure()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during token refresh, attempt=$runAttemptCount", e)
            
            // Log to Firebase Crashlytics
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("refresh_token_attempt", runAttemptCount)
                setCustomKey("error_type", "IOException")
                setCustomKey("user_id", userPrefs.userId()?:"")
                setCustomKey("user_name", userPrefs.userName?:"")
                recordException(e)
            }
            
            // Retry network errors with backoff
            if (shouldRetry(runAttemptCount)) {
                Log.d(TAG, "Will retry network error after backoff delay")
                return Result.retry()
            } else {
                Log.e(TAG, "Max retry attempts reached for network error")
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during token refresh, attempt=$runAttemptCount", e)
            
            // Log to Firebase Crashlytics
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("refresh_token_attempt", runAttemptCount)
                setCustomKey("error_type", e.javaClass.simpleName)
                setCustomKey("user_id", userPrefs.userId()?:"")
                setCustomKey("user_name", userPrefs.userName?:"")
                recordException(e)
            }
            
            // Don't retry unexpected errors
            Result.failure()
        }
    }
    
    /**
     * Determines if the work should be retried based on attempt count
     */
    private fun shouldRetry(attemptCount: Int): Boolean {
        return attemptCount < MAX_RETRY_ATTEMPTS
    }

    private suspend fun refreshToken(url: String): Int = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .addHeader("X-APP-ID", AppID.url())
            .build()

        val response = okHttpClient.newCall(request).await()
        
        try {
            val resCode = response.code()
            Log.d("resCode===>>>", ""+resCode)
            if (resCode == 200) {
                val strResponse = response.body()?.string()?:""
                val json = JSONObject(strResponse)
                if (!json.isNull("jwt")) {
                    val jwtToken = json.optString("jwt")
                    if (jwtToken.isNotNullOrEmpty()) {
                        userPrefs.jwtToken = jwtToken
                        authInterceptor.updateJWT(jwtToken)
                        Log.d(TAG, "JWT token updated successfully")
                        return@withContext resCode
                    } else {
                        Log.w(TAG, "Received empty JWT token")
                    }
                } else {
                    Log.w(TAG, "No JWT field in response")
                }
            } else {
                Log.w(TAG, "Response body is null")
            }
            return@withContext resCode
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