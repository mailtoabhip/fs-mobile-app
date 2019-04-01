package com.delhivery.orion.repository

import com.auth0.android.jwt.JWT
import com.delhivery.orion.api.UserService
import com.delhivery.orion.data.toRoutes
import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.prefs.UserPrefs
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

  /**
   * User full name
   */
  fun username() =
    JWT(userPrefs.jwtToken!!).claims.let { "${it["first_name"]?.asString()} ${it["last_name"]?.asString()}" }

  /**
   * Get user selected routes
   */
  fun userRoutes() =
    userService.userRoutes("")
        .convertResponse()
        .map {
          it.toRoutes()
        }

}