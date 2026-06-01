package com.delhivery.axle.api.repository

import android.util.Log
import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.request.UpdateUserAccessRequest
import com.delhivery.axle.api.request.UpdateUserFCMTokenRequest
import com.delhivery.axle.api.request.UpdateUserRoutesRequest
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.api.service.UMSService
import com.delhivery.axle.api.service.UserService
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.UserRespone
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
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
  private val umsService: UMSService,
  private val loadBoardService: LoadBoardService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /* JWT token */
  private val jwt by lazy {
    userPrefs.jwtToken?.let { JWT(it) }
  }

  /* user model cache */
  private var user: UserModel? = null

  /**
   * Current user id
   */
  fun userId(): String {
    return try {
      userPrefs.jwtToken?.let { token ->
        if (token.isBlank()) {
          ""
        } else {
          JWT(token).let { jwt ->
            jwt.claims["sub"]?.asString() ?: ""
          }
        }
      } ?: ""
    } catch (e: Exception) {
      // Handle JWT parsing errors
      userPrefs.jwtToken = null
      ""
    }
  }

  /**
   * Get user
   */
  fun getUser(cache: Boolean = true): Single<UserModel> = if (!cache || user == null) {
    loadBoardService.userDetails(userId()).convertResponse().map {
      it.userModel[0]
    }.onBackground()
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
   * Get delegation token for AWS
   */
  fun getDelegationToken(target: String) = loadBoardService.getDelegationToken(target)

  /**
   * Fetch roles and permissions
   */
  fun fetchUserRoles() = umsService.fetchUserRole(userId(), UrlConfig.AppID.url())
  /**
   * get omc details
   */
  fun getOMCs(offset: Int, limit: Int, payee: String) = userService.getOMCs(offset, limit, payee)

  /**
   * get kyc docs
   */
  fun getKycDocs() = loadBoardService.kycDocs(userId())
}