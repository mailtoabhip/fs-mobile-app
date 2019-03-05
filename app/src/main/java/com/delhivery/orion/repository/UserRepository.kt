package com.delhivery.orion.repository

import com.auth0.android.jwt.JWT
import com.delhivery.orion.database.AppDatabase
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
  private val userPrefs: UserPrefs
) : BaseRepository() {

  /**
   * User full name
   */
  fun username() =
    JWT(userPrefs.jwtToken!!).claims.let { "${it["first_name"]?.asString()} ${it["last_name"]?.asString()}" }
}