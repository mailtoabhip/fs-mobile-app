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
import com.auth0.android.jwt.JWT
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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

    // Clean URL selection
    private val refreshUrl: String =
        if (BuildConfig.FLAVOR in listOf("development", "uat"))
            "https://api-stage-ums.delhivery.com/v2/refresh_token/?force=1"
        else
            "https://api-ums.delhivery.com/v2/refresh_token/?force=1"

    override suspend fun doWork(): Result {
        val attempt = runAttemptCount
        Log.d(TAG, "Token refresh work started (attempt $attempt)")

        // No token → no refresh needed
        val existingToken = userPrefs.jwtToken
        if (existingToken == null) {
            Log.d(TAG, "No JWT token found, skipping refresh")
            //cancel work manager
            WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
            //
            return Result.success()
        }

        // Check if token is expired before making API call
        if (isTokenExpired(existingToken)) {
            Log.d(TAG, "Token is expired, skipping refresh API call")
            //cancel work manager
            WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
            //
            return Result.success()
        }

        return try {
            val status = refreshToken(refreshUrl)

            if (status == 200) {
                Log.d(TAG, "Token refresh completed successfully, canceling periodic work")
                //cancel work manager
                WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)

//************************************************************************************************************************************************
//***********************************************THIS CODE IS TO TEST THE HTTP EXCEPTION FLOW*****************************************************
//************************************************************************************************************************************************
//                if (BuildConfig.FLAVOR.equals("uat", ignoreCase = true)){
//                    Log.d(TAG, "UAT build configuration enabled.")
//                    // Throw an HttpException with correct code
//                    throw HttpException(
//                        retrofit2.Response.error<Unit>(
//                            401,
//                            ResponseBody.create(MediaType.parse("text/plain"), "Token refresh failed")
//                        )
//                    )
//                }
//************************************************************************************************************************************************
//*************************************************************END OF SECTION*********************************************************************
//************************************************************************************************************************************************

                return Result.success()
            }

            // Throw an HttpException with correct code
            throw HttpException(
                retrofit2.Response.error<Unit>(
                    status,
                    ResponseBody.create(MediaType.parse("text/plain"), "Token refresh failed")
                )
            )

        } catch (e: HttpException) {
            val statusCode = e.code()
            Log.e(TAG, "HTTP error: $statusCode", e)

            logError(e, "HttpException")

            when {
                statusCode in 400..499 -> {
                    Log.w(TAG, "4xx client error – not retrying")
                    Result.failure()
                }

                shouldRetry(attempt) -> Result.retry()

                else -> Result.failure()
            }

        } catch (e: IOException) {
            Log.e(TAG, "Network error during token refresh", e)

            logError(e, "IOException")

            if (shouldRetry(attempt)) Result.retry() else Result.failure()

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)

            logError(e, e.javaClass.simpleName)

            Result.failure()
        }
    }

    /**
     * Logs error details to Firebase Crashlytics
     */
    private fun logError(e: Throwable, type: String) {
        val jwtInfo = extractJWTInfo()
        
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("refresh_token_attempt", runAttemptCount)
            setCustomKey("error_type", type)
            setCustomKey("user_id", userPrefs.userId() ?: "")
            setCustomKey("user_name", userPrefs.userName ?: "")
            setCustomKey("iat", jwtInfo.issuedAt)
            setCustomKey("exp", jwtInfo.expiry)
            setCustomKey("toe", jwtInfo.toe)
            recordException(e)
        }
    }

    /**
     * Checks if the JWT token is expired
     * @param token The JWT token string to check
     * @return true if token is expired or null, false otherwise
     */
    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) {
            return true
        }

        return try {
            val jwt = JWT(token)
            val expiresAt = jwt.expiresAt
            
            if (expiresAt == null) {
                // If expiry date cannot be extracted, consider it expired to be safe
                Log.w(TAG, "Cannot extract expiry date from token, considering expired")
                return true
            }

            val isExpired = expiresAt.before(Date())
            if (isExpired) {
                Log.d(TAG, "Token expired at: ${expiresAt}, current time: ${Date()}")
            }
            isExpired
        } catch (e: Exception) {
            Log.e(TAG, "Error checking token expiry", e)
            // If we can't parse the token, consider it expired to be safe
            true
        }
    }

    /**
     * Extracts JWT token information (issuedAt, expiry, toe)
     */
    private fun extractJWTInfo(): JWTInfo {
        return try {
            val token = userPrefs.jwtToken
            if (token.isNullOrEmpty()) {
                return JWTInfo("", "", "")
            }

            val jwt = JWT(token)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            // Extract issuedAt (iat) - parse from claim value
            val issuedAt = try {
                val iatClaim = jwt.claims["iat"]
                val iatTimestamp = when {
                    iatClaim != null -> {
                        // Try asString first, then toString as fallback
                        iatClaim.asString()?.toLongOrNull() 
                            ?: iatClaim.toString().toLongOrNull()
                    }
                    else -> null
                }
                iatTimestamp?.let { dateFormat.format(Date(it * 1000L)) } ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting JWT info", e)
                ""
            }

            // Extract expiry (exp) - try Date property first, then claim value
            val expiry = jwt.expiresAt?.let { dateFormat.format(it) } ?: try {
                val expClaim = jwt.claims["exp"]
                val expTimestamp = when {
                    expClaim != null -> {
                        // Try asString first, then toString as fallback
                        expClaim.asString()?.toLongOrNull() 
                            ?: expClaim.toString().toLongOrNull()
                    }
                    else -> null
                }
                expTimestamp?.let { dateFormat.format(Date(it * 1000L)) } ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting JWT info", e)
                ""
            }

            // Extract toe (custom claim) - parse from claim value as timestamp
            val toe = try {
                val toeClaim = jwt.claims["toe"]
                val toeTimestamp = when {
                    toeClaim != null -> {
                        // Try asString first, then toString as fallback
                        toeClaim.asString()?.toLongOrNull() 
                            ?: toeClaim.toString().toLongOrNull()
                    }
                    else -> null
                }
                toeTimestamp?.let { dateFormat.format(Date(it * 1000L)) } ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting JWT info", e)
                ""
            }

            Log.d(TAG, "issuedAt=$issuedAt")
            Log.d(TAG, "expiry=$expiry")
            Log.d(TAG, "toe=$toe")

            JWTInfo(issuedAt, expiry, toe)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting JWT info", e)
            JWTInfo("N/A", "N/A", "N/A")
        }
    }

    /**
     * Data class to hold JWT information
     */
    private data class JWTInfo(
        val issuedAt: String,
        val expiry: String,
        val toe: String
    )

    /**
     * Determines if retry should occur based on the attempt number
     */
    private fun shouldRetry(attempt: Int): Boolean = attempt < MAX_RETRY_ATTEMPTS

    /**
     * Executes the refresh token call synchronously (safe inside Dispatchers.IO)
     */
    private suspend fun refreshToken(url: String): Int = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .addHeader("X-APP-ID", AppID.url())
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val code = response.code()

            if (code == 200) {
                val bodyStr = response.body()?.string().orEmpty()
                val json = JSONObject(bodyStr)

                val jwt = json.optString("jwt")
                if (jwt.isNotNullOrEmpty()) {
                    userPrefs.jwtToken = jwt
                    authInterceptor.updateJWT(jwt)
                    Log.d(TAG, "JWT token updated successfully")
                } else {
                    Log.w(TAG, "Missing or empty JWT in response")
                }
            } else {
                Log.w(TAG, "Token refresh failed with code $code")
            }

            code
        }
    }

    /**
     * Factory for Dagger injection
     */
    class Factory @Inject constructor(
        private val userPrefs: UserPrefs,
        private val okHttpClient: OkHttpClient,
        private val authInterceptor: DelhiveryNetworkInterceptor
    ) : DaggerWorkerFactory.ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
            RefreshTokenWorker(appContext, params, userPrefs, okHttpClient, authInterceptor)
    }
}
