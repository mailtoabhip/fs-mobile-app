package com.delhivery.orion.utils

import android.content.Context
import android.content.SharedPreferences
import com.delhivery.orion.injection.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App Prefs, provide application preferences interface
 *
 */
@Singleton
class AppPrefs @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("", Context.MODE_PRIVATE)
    }

    /**
     * Clear all preferences
     */
    fun clearPrefs() = prefs.edit().clear().apply()

    /* Sample Pref */
    var token: String?
        set(value) = prefs.edit().putString(PrefKeys.Token, value).apply()
        get() = prefs.getString(PrefKeys.Token, null)
}

/**
 * App Preferences Keys
 *
 */
internal object PrefKeys {
    const val Token = "token"
}