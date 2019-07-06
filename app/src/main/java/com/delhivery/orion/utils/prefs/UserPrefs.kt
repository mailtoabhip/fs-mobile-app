package com.delhivery.orion.utils.prefs

import android.content.Context
import com.delhivery.orion.injection.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User preferences
 */
@Singleton
class UserPrefs @Inject constructor(@ApplicationContext private val context: Context) : BasePrefs(
    context
) {
  override fun prefsName() = PrefNames.UserPrefs

  /* JWT Token */
  var jwtToken: String?
    set(value) = editor.putString(PrefKeys.JWTToken, value).apply()
    get() = prefs.getString(PrefKeys.JWTToken, null)

  /* Base City Code */
  var baseCityCode: String?
    set(value) = editor.putString(PrefKeys.BaseCityCode, value).apply()
    get() = prefs.getString(PrefKeys.BaseCityCode, null)

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val BaseCityCode = "base_city_code"
  }
}