package com.dfd.delfin.api.repository

import com.auth0.android.jwt.JWT
import com.dfd.delfin.api.service.LoadBoardService
import com.dfd.delfin.api.service.UMSService
import com.dfd.delfin.api.service.UserService
import com.dfd.delfin.config.UrlConfig
import com.dfd.delfin.api.response.FsUserProfile
import com.dfd.delfin.data.UserModel
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.prefs.UserPrefs
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
    loadBoardService.userDetails().convertResponse().map { profile ->
      profile.toUserModel()
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

  private fun FsUserProfile.toUserModel() = UserModel(
    supplierDetails = null,
    truckTypes = null,
    clientDetails = null,
    userId = id ?: "",
    phoneNumber = phone,
    phoneNo = phone,
    userName = buildString {
      if (!firstName.isNullOrEmpty()) append(firstName)
      if (!middleName.isNullOrEmpty()) {
        if (isNotEmpty()) append(" ")
        append(middleName)
      }
      if (!lastName.isNullOrEmpty()) {
        if (isNotEmpty()) append(" ")
        append(lastName)
      }
    }.takeIf { it.isNotEmpty() },
    demandType = emptyList(),
    isUserVerified = isActive == true,
    selectionChangeCount = null,
    userMode = null,
    userRole = null,
    userType = null,
    businessName = null,
    referralCode = null,
    otherAddress = null,
    businessAddress = null,
    isPanVerified = null,
    isAadhaarVerified = null,
    isGstVerified = null,
    isRcVerified = null,
    verificationStatus = null,
    profileImageUrl = null,
    name = null,
    canViewThirdPartyLoads = null,
    isIdentityVerified = null,
    isGstsByPanNotRegistered = null,
    isTruckingDocumentUploaded = null,
    noOfVerificationIssues = null,
    isBankDetailsRejected = null,
    isAddressSameAsGST = null,
  )

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