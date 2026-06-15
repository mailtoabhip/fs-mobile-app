package com.dfd.delfin.network

import android.util.Log
import com.auth0.android.jwt.JWT
import com.dfd.delfin.config.UrlConfig
import com.dfd.delfin.utils.prefs.UserPrefs
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that handles 401 responses by refreshing the access token
 * using the stored refresh token, then retrying the failed request transparently.
 *
 * Uses Kotlin coroutines Mutex to ensure only one token refresh happens at a time.
 * If multiple requests get a 401 simultaneously, only the first one triggers a refresh;
 * the rest wait and reuse the new token.
 *
 * Flow:
 * 1. API call returns 401
 * 2. TokenAuthenticator.authenticate() is invoked by OkHttp
 * 3. Calls POST /api/v1/auth/refresh with { "refresh_token": "<stored_refresh_token>" }
 * 4. On success: stores new access_token in SharedPreferences, updates interceptor
 * 5. Retries original request with the new access token
 * 6. If refresh fails (401/400): returns null → user needs to re-login
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val userPrefs: UserPrefs,
    private val networkInterceptor: DelfinNetworkInterceptor,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val sessionManager: SessionManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY_COUNT = 2
        private const val REFRESH_ENDPOINT = "api/v1/auth/refresh"
        private val tokenMutex = Mutex()

        private val JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8")

        // Reuse a single OkHttpClient for all refresh calls to avoid creating
        // new thread pools and connection pools on every 401.
        private val refreshClient: OkHttpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }

    private val refreshUrl: String = "${UrlConfig.FsAuthService.url().trimEnd('/')}/$REFRESH_ENDPOINT"

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        val retryCount = response.request().header("X-Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Max retry count reached, not retrying")
            return null
        }

        // No refresh token available — can't refresh
        val storedRefreshToken = userPrefs.refreshToken
        if (storedRefreshToken.isNullOrEmpty()) {
            Log.w(TAG, "No refresh token available, cannot refresh")
            return null
        }

        return try {
            val newAccessToken = runBlocking {
                tokenMutex.withLock {
                    // Check if token was already refreshed by another thread
                    val currentToken = userPrefs.jwtToken
                    val requestToken = response.request().header("Authorization")
                        ?.removePrefix("Bearer ")

                    if (!currentToken.isNullOrEmpty()
                        && requestToken != null
                        && requestToken != currentToken
                        && !isTokenExpired(currentToken)
                    ) {
                        // Token was already refreshed by another thread — use the new one
                        Log.d(TAG, "Token already refreshed by another thread")
                        return@withLock currentToken
                    }

                    // Perform the refresh
                    refreshAccessToken(storedRefreshToken)
                }
            } ?: return null

            // Retry the original request with the new access token
            response.request().newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .header("X-Retry-Count", (retryCount + 1).toString())
                .build()

        } catch (e: Exception) {
            Log.e(TAG, "Error during token refresh", e)
            null
        }
    }

    /**
     * Calls POST /api/v1/auth/refresh with the refresh token in the body.
     * Updates UserPrefs and DelhiveryNetworkInterceptor on success.
     *
     * API Contract:
     * - Request: { "refresh_token": "<token>" }
     * - Success (200): { "success": true, "data": { "access_token": "...", "id_token": "..." } }
     * - Error (400/401): { "success": false, "error": { "message": "...", "code": ... } }
     *
     * @param refreshToken The refresh token to send
     * @return New access token on success, null on failure
     */
    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            Log.d(TAG, "Refreshing access token...")
            Log.d(TAG, "Refresh URL: $refreshUrl")
            Log.d(TAG, "Refresh token (first 20 chars): ${refreshToken.take(20)}...")

            // Reuse the shared OkHttpClient to avoid allocating new pools per refresh
            val client = refreshClient

            val body = JSONObject().apply {
                put("refresh_token", refreshToken)
            }

            Log.d(TAG, "Refresh request body: $body")

            val requestBody = RequestBody.create(JSON_MEDIA_TYPE, body.toString())

            val requestId = java.util.UUID.randomUUID().toString()
            // Use cached IP — don't block refresh on a network call for metadata
            val clientIp = deviceInfoProvider.cachedPublicIp()
            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Device-Id", deviceInfoProvider.deviceId)
                .addHeader("X-Device-Model", deviceInfoProvider.deviceModel)
                .addHeader("X-App-Version", deviceInfoProvider.appVersion)
                .addHeader("X-Platform", deviceInfoProvider.platform)
                .addHeader("X-Os-Version", deviceInfoProvider.osVersion)
                .addHeader("X-Request-Id", requestId)
                .apply {
                    if (clientIp.isNotEmpty()) {
                        addHeader("X-Client-Ip", clientIp)
                    }
                }
                .addHeader("Authorization", "Bearer ${userPrefs.jwtToken ?: ""}")
                .build()

            val response = client.newCall(request).execute()
            response.use { resp ->
                val code = resp.code()
                val responseBody = resp.body()?.string().orEmpty()

                Log.d(TAG, "Refresh response code: $code")
                Log.d(TAG, "Refresh response body: $responseBody")

                if (code == 200) {
                    val json = JSONObject(responseBody)

                    val success = json.optBoolean("success", false)
                    if (!success) {
                        Log.w(TAG, "Refresh response success=false")
                        return null
                    }

                    val data = json.optJSONObject("data")
                    val accessToken = data?.optString("access_token")
                    val newRefreshToken = data?.optString("refresh_token")

                    if (!accessToken.isNullOrEmpty()) {
                        // Store the new tokens
                        userPrefs.jwtToken = accessToken
                        if (!newRefreshToken.isNullOrEmpty()) {
                            userPrefs.refreshToken = newRefreshToken
                        }
                        networkInterceptor.updateJWT(accessToken)
                        Log.d(TAG, "Access token refreshed successfully (first 20 chars): ${accessToken.take(20)}...")
                        accessToken
                    } else {
                        Log.w(TAG, "Refresh response missing access_token in data")
                        null
                    }
                } else {
                    Log.w(TAG, "Token refresh failed with HTTP $code")

                    // Parse error response for detailed logging
                    try {
                        val errorJson = JSONObject(responseBody)
                        val error = errorJson.optJSONObject("error")
                        val errorMessage = error?.optString("message") ?: errorJson.optString("message", "Unknown error")
                        Log.w(TAG, "Refresh error message: $errorMessage")
                    } catch (_: Exception) {
                        Log.w(TAG, "Refresh error body (raw): $responseBody")
                    }

                    // Refresh failed — clear local session so user is forced to re-login
                    Log.w(TAG, "Clearing local session due to refresh failure")
                    networkInterceptor.updateJWT(null)
                    userPrefs.clearPrefs()
                    sessionManager.onSessionExpired()

                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token refresh: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    /**
     * Checks if a JWT token is expired.
     */
    private fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            val expiresAt = jwt.expiresAt ?: return true
            expiresAt.before(Date())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JWT for expiry check", e)
            true
        }
    }
}
