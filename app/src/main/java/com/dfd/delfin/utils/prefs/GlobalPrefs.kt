package com.dfd.delfin.utils.prefs

import android.content.Context
import com.dfd.delfin.injection.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalPrefs @Inject constructor(@ApplicationContext private val context: Context) : BasePrefs(
    context
) {
  override fun prefsName() = PrefNames.GlobalPrefs

  /* is location permission requested */
  var isLocationPermissionRequested: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsLocationPermissionRequested, value).apply()
    get() = prefs.getBoolean(PrefKeys.IsLocationPermissionRequested, false)

  /* is onboarding completed */
  var isOnboardingCompleted: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsOnboardingCompleted, value).apply()
    get() = prefs.getBoolean(PrefKeys.IsOnboardingCompleted, false)

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val IsLocationPermissionRequested = "is_location_permission_requested"
    const val IsOnboardingCompleted = "is_onboarding_completed"
  }
}