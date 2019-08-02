package com.delhivery.axle.utils.prefs

import android.content.Context
import com.delhivery.axle.injection.qualifier.ApplicationContext
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

  /* Base/Origin City Code */
  var cityCode: String?
    set(value) = editor.putString(PrefKeys.CityCode, value).apply()
    get() = prefs.getString(PrefKeys.CityCode, null)

  var phoneNumber: String?
    set(value) = editor.putString(PrefKeys.PhoneNumber, value).apply()
    get() = prefs.getString(PrefKeys.PhoneNumber, "")

  /* Routes update flag */
  var routeUpdate: Boolean
    set(value) = editor.putBoolean(PrefKeys.RouteUpdate, value).apply()
    get() = prefs.getBoolean(PrefKeys.RouteUpdate, false)

  /* LoggedIn flag */
  var hasLoggedIn: Boolean
    set(value) = editor.putBoolean(PrefKeys.HasLoggedIn, value).apply()
    get() = prefs.getBoolean(PrefKeys.HasLoggedIn, false)

  /* TDS rate */
  var tdsRate: Int
    set(value) = editor.putInt(PrefKeys.TdsRate, value).apply()
    get() = prefs.getInt(PrefKeys.TdsRate, 99)

  /* Username */
  var userName: String
    set(value) = editor.putString(PrefKeys.UserName, value).apply()
    get() = prefs.getString(PrefKeys.UserName, "No name").toString()

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val CityCode = "city_code"
    const val RouteUpdate = "route_update"
    const val PhoneNumber = "phone_number"
    const val HasLoggedIn = "has_logged_in"
    const val TdsRate = "tds_rate"
    const val UserName = "user_name"
  }
}