package com.delhivery.orion.repository

import com.auth0.android.jwt.JWT
import com.delhivery.orion.api.UserService
import com.delhivery.orion.api.request.UpdateUserBaseCityRequest
import com.delhivery.orion.api.request.UpdateUserRoutesRequest
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.RouteMappingModel
import com.delhivery.orion.data.UserModel
import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.prefs.UserPrefs
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository
 * -- contains most of business logic related to User
 *
 */
@Singleton
class UserRepository @Inject constructor(
  private val appDatabase: AppDatabase,
  private val userPrefs: UserPrefs,
  private val userService: UserService
) : BaseRepository() {

  /* JWT token */
  private val jwt by lazy {
    JWT(userPrefs.jwtToken!!)
  }

  /* user model cache */
  private var user: UserModel? = null

  /**
   * Current user id
   */
  fun userId() =
    "ums::user::fcb31360-7ae4-11e9-9d32-0223f692f646"
//    (jwt.claims["sub"]?.asString()!!)

  /**
   * User full name
   */
  fun username() =
    JWT(userPrefs.jwtToken!!).claims.let { "${it["first_name"]?.asString()} ${it["last_name"]?.asString()}" }

  /**
   * Get user selected routes
   */
  fun userRoutes(cache: Boolean = true) = if (!cache || user == null) {
    getUser()
  } else {
    Single.just(user!!)
  }.map { it.routes }

  /**
   * Get user
   */
  fun getUser(cache: Boolean = true) = if (!cache || user == null) {
    userService.userDetails(userId())
        .convertResponse()
        .doOnSuccess { user = it }
  } else {
    Single.just(user)
  }

  /**
   * Update user routes and get all routes
   */
  fun updateBaseCityAndRoutes(
    city: CityModel?,
    routes: List<RouteMappingModel>
  ) =
    userService.updateCity(
        userId(), UpdateUserBaseCityRequest(
        city?.city ?: "", city?.cityId ?: "",
        routes
    )
    )

  /**
   * Update user routes and get all routes
   */
  fun updateUserRoutes(
    routes: List<RouteMappingModel>
  ) =
    userService.updateUserRoutes(userId(), UpdateUserRoutesRequest(routes))
}