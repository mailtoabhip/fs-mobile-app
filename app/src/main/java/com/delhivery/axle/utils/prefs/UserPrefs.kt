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

  var spId: String?
    set(value) = editor.putString(PrefKeys.SupplierId, value).apply()
    get() = prefs.getString(PrefKeys.SupplierId, null)

  /* Routes update flag */
  var routeUpdate: Boolean
    set(value) = editor.putBoolean(PrefKeys.RouteUpdate, value).apply()
    get() = prefs.getBoolean(PrefKeys.RouteUpdate, false)

  /* Routes update flag */
  var phoneNumber: String?
    set(value) = editor.putString(PrefKeys.Phone, value).apply()
    get() = prefs.getString(PrefKeys.Phone, "")

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val CityCode = "city_code"
    const val RouteUpdate = "route_update"
    const val SupplierId = "supplier_id"
  }
}