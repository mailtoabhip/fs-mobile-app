package com.delhivery.axle.utils.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Security Preferences Manager
 * 
 * Type-safe wrapper for security-related SharedPreferences.
 * Manages root detection results and security state.
 * 
 * Usage:
 * ```
 * val securityPrefs = SecurityPrefs(context)
 * securityPrefs.isDeviceRooted = true
 * ```
 * 
 * @author Security Team
 * @version 1.0
 */
class SecurityPrefs(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Flag indicating if device has been detected as rooted.
     */
    var isDeviceRooted: Boolean
        get() = prefs.getBoolean(KEY_IS_ROOTED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ROOTED, value).apply()
    
    /**
     * Flag indicating if device has been detected as emulator.
     */
    var isDeviceEmulator: Boolean
        get() = prefs.getBoolean(KEY_IS_EMULATOR, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_EMULATOR, value).apply()
    
    /**
     * Flag indicating if root check has been completed in this session.
     */
    var rootCheckCompleted: Boolean
        get() = prefs.getBoolean(KEY_ROOT_CHECK_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ROOT_CHECK_COMPLETED, value).apply()
    
    /**
     * Comma-separated string of detection methods that identified root.
     * Example: "SU Binary, Root Apps, Test Keys"
     */
    var rootDetectionMethods: String?
        get() = prefs.getString(KEY_DETECTION_METHODS, null)
        set(value) = prefs.edit().putString(KEY_DETECTION_METHODS, value).apply()
    
    /**
     * Timestamp (in milliseconds) of the last root check.
     */
    var lastRootCheckTimestamp: Long
        get() = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CHECK_TIME, value).apply()
    
    /**
     * Check if device is insecure (rooted or emulator).
     */
    fun isDeviceInsecure(): Boolean = isDeviceRooted || isDeviceEmulator
    
    /**
     * Clear all security check related data.
     * Useful for testing or when user resets app.
     */
    fun clearRootCheckData() {
        prefs.edit()
            .remove(KEY_IS_ROOTED)
            .remove(KEY_IS_EMULATOR)
            .remove(KEY_ROOT_CHECK_COMPLETED)
            .remove(KEY_DETECTION_METHODS)
            .remove(KEY_LAST_CHECK_TIME)
            .apply()
    }
    
    companion object {
        private const val PREF_NAME = "security_prefs"
        private const val KEY_IS_ROOTED = "is_device_rooted"
        private const val KEY_IS_EMULATOR = "is_device_emulator"
        private const val KEY_ROOT_CHECK_COMPLETED = "root_check_completed"
        private const val KEY_DETECTION_METHODS = "root_detection_methods"
        private const val KEY_LAST_CHECK_TIME = "last_root_check_time"
    }
}

