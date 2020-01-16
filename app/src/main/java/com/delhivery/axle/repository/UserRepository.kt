package com.delhivery.axle.repository

import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.UMSService
import com.delhivery.axle.api.UserService
import com.delhivery.axle.api.request.UpdateUserAccessRequest
import com.delhivery.axle.api.request.UpdateUserFCMTokenRequest
import com.delhivery.axle.api.request.UpdateUserRoutesRequest
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.prefs.UserPrefs
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
  private val appDB: AppDatabase,
  private val userPrefs: UserPrefs,
  private val userService: UserService,
  private val umsService: UMSService
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
//    when (BuildConfig.FLAVOR) {
//      "development" -> "ums::user::fcb31360-7ae4-11e9-9d32-0223f692f646"
//      else ->
    (jwt.claims["sub"]?.asString()!!)
//    }

  /**
   * Get user
   */
  fun getUser(cache: Boolean = true): Single<UserModel> = if (!cache || user == null) {
    userService.userDetails(userId())
        .convertResponse()
        .onBackground()
        .doOnSuccess {
          if (it != null) {
            user = it
            userPrefs.saveUser(it)
          }
        }
  } else {
    Single.just(user)
  }

  /**
   * Update user routes and get all routes
   */
  fun updateUserRoutes(routes: List<RouteMappingModel>) =
    userService.updateUserRoutes(userId(), UpdateUserRoutesRequest(routes))

  /**
   * Update app access flag
   */
  fun updateUserAppAccess() = userService.updateUserAppAccess(userId(), UpdateUserAccessRequest())

  /**
   * Update FCM token
   */
  fun updateFCMToken(fcmToken: String) =
    userService.updateFCMToken(
        userId(), UpdateUserFCMTokenRequest.getRequest(fcmToken)
    ).convertMessageResponse()

  /**
   * Get delegation token for AWS
   */
  fun getDelegationToken(target: String) = umsService.getDelegationToken(target)
}