package com.delhivery.orion.utils.prefs

import android.content.Context
import com.delhivery.orion.injection.qualifier.ApplicationContext
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
}

internal object PrefKeys {
  const val IsLocationPermissionRequested = "is_location_permission_requested"
}