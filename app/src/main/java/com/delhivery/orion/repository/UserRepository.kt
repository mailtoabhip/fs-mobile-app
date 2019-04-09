package com.delhivery.orion.repository

import com.auth0.android.jwt.JWT
import com.delhivery.orion.api.UserService
import com.delhivery.orion.api.request.UpdateUserRoutesRequest
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
  fun userId() = "ums::user::30a8a924-522b-11e9-b316-0227a8987d6e"
  //(jwt.claims["sub"]?.asString()!!)

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
   * Add new route to user prefs
   */
  fun addRoutes(routes: List<RouteMappingModel>) =
    getUser()
        .flatMap { _user ->
          val _routes = mutableListOf<RouteMappingModel>()
          _user.routes?.let { _routes.addAll(it) }
          _routes.addAll(routes)
          updateRoutes(_routes)
        }

  /**
   * Update user routes and get all routes
   */
  fun updateRoutes(routes: List<RouteMappingModel>) =
    userService.updateUserRoutes(userId(), UpdateUserRoutesRequest(routes))
        .flatMap {
          getUser(false)
        }.map { it.userRoutes() }
}