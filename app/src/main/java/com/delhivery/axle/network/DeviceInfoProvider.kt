package com.delhivery.axle.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.delhivery.axle.injection.qualifier.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides device metadata and public IP for network request headers.
 *
 * All static fields are computed once at construction.
 * [publicIp] is fetched asynchronously at app startup via [fetchPublicIp].
 * Uses [java.net.URL] (not OkHttp) to avoid circular interceptor dependency.
 */
@Singleton
class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val deviceId: String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""

    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"

    val appVersion: String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    } catch (_: PackageManager.NameNotFoundException) {
        ""
    }

    val platform: String = "android"

    val osVersion: String = Build.VERSION.RELEASE

    @Volatile
    var publicIp: String = ""
        private set

    private val ipDeferred = CompletableDeferred<String>()

    init {
        Log.d(TAG, "──────────────────────────────────────")
        Log.d(TAG, "Device info collected:")
        Log.d(TAG, "  X-Device-Id    : $deviceId")
        Log.d(TAG, "  X-Device-Model : $deviceModel")
        Log.d(TAG, "  X-App-Version  : $appVersion")
        Log.d(TAG, "  X-Platform     : $platform")
        Log.d(TAG, "  X-OS-Version   : $osVersion")
        Log.d(TAG, "  X-Client-Ip    : (pending fetch)")
        Log.d(TAG, "──────────────────────────────────────")
    }

    /**
     * Fetches the device's public IP address from api.ipify.org.
     * Should be called once at app startup from a coroutine scope.
     * On failure, [publicIp] remains empty — subsequent requests will omit the header value.
     */
    suspend fun fetchPublicIp() {
        try {
            val ip = withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    URL("https://api.ipify.org").readText().trim()
                }
            }
            publicIp = ip
            ipDeferred.complete(ip)
            Log.d(TAG, "Public IP fetched successfully: $publicIp")
        } catch (e: Exception) {
            ipDeferred.complete("")
            Log.w(TAG, "Failed to fetch public IP: ${e.message}")
        }
    }

    /**
     * Re-fetches the public IP when network changes (e.g., WiFi → mobile data).
     * Safe to call multiple times — updates the cached [publicIp] in place.
     */
    suspend fun refreshPublicIp() {
        try {
            val ip = withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    URL("https://api.ipify.org").readText().trim()
                }
            }
            if (ip.isNotEmpty()) {
                publicIp = ip
                Log.d(TAG, "Public IP refreshed: $publicIp")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh public IP: ${e.message}")
        }
    }

    /**
     * Returns the public IP, blocking until the fetch completes.
     * Safe to call from OkHttp interceptor threads (IO).
     * Returns empty string if the fetch hasn't completed within the timeout.
     */
    fun awaitPublicIp(): String {
        val timeoutMs = 10000L
        if (publicIp.isNotEmpty()) return publicIp
        return try {
            runBlocking {
                withTimeoutOrNull(timeoutMs) { ipDeferred.await() } ?: ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "Timeout waiting for public IP: ${e.message}")
            ""
        }
    }

    companion object {
        private const val TAG = "DeviceInfoProvider"
    }
}
