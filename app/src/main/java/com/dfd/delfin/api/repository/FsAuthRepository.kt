package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.FsInitiateRequest
import com.dfd.delfin.api.request.FsResendRequest
import com.dfd.delfin.api.request.FsUpdateProfileRequest
import com.dfd.delfin.api.request.FsVerifyRequest
import com.dfd.delfin.api.response.FsInitiateData
import com.dfd.delfin.api.response.FsLogoutData
import com.dfd.delfin.api.response.FsResendData
import com.dfd.delfin.api.response.FsUpdateProfileData
import com.dfd.delfin.api.response.FsUserProfile
import com.dfd.delfin.api.response.FsVerifyData
import com.dfd.delfin.api.response.toResource
import com.dfd.delfin.api.service.FsAuthService
import com.dfd.delfin.network.DelhiveryNetworkInterceptor
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for the FS Auth Service.
 *
 * All methods wrap service calls in [safeApiCall] and return a typed [Resource].
 * Token storage and interceptor updates are handled here; routing lives in the ViewModel.
 */
@Singleton
class FsAuthRepository @Inject constructor(
    private val fsAuthService: FsAuthService,
    private val userPrefs: UserPrefs,
    private val networkInterceptor: DelhiveryNetworkInterceptor,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

    init {
        // Seed the interceptor with any token already persisted from a previous session
        networkInterceptor.updateJWT(userPrefs.jwtToken)
    }

    /**
     * Initiates OTP flow for both new and existing users.
     * POST /api/v1/auth/initiate
     *
     * Check [FsInitiateData.isNewUser] to distinguish signup vs login:
     *  - true  → new user, [FsInitiateData.session] will be null
     *  - false → existing user, [FsInitiateData.session] must be passed to [verify]
     *
     * @param phone 10-digit phone number (without country code)
     */
    suspend fun initiate(phone: String): Resource<FsInitiateData> = safeApiCall {
        fsAuthService.initiate(FsInitiateRequest(phone = phone)).toResource()
    }

    /**
     * Resends OTP for the given phone number.
     * POST /api/v1/auth/resend
     *
     * @param phone 10-digit phone number (without country code)
     */
    suspend fun resend(phone: String): Resource<FsResendData> = safeApiCall {
        fsAuthService.resend(FsResendRequest(phone = phone)).toResource()
    }

    /**
     * Verifies OTP for both signup and login flows.
     * Stores the access token in prefs and updates the network interceptor on success.
     * POST /api/v1/auth/verify
     *
     * @param phone   10-digit phone number
     * @param otp     6-digit OTP received via SMS
     * @param session Session token from [initiate] — required for existing users, null for new users
     */
    suspend fun verify(
        phone: String,
        otp: String,
        session: String? = null
    ): Resource<FsVerifyData> = safeApiCall {
        val data = fsAuthService.verify(
            FsVerifyRequest(phone = phone, otp = otp, session = session)
        ).toResource()
        // Persist token and update interceptor so subsequent authenticated calls work immediately
        userPrefs.jwtToken = data.accessToken
        userPrefs.refreshToken = data.refreshToken
        networkInterceptor.updateJWT(data.accessToken)
        userPrefs.firstName = data.profileDetails.firstName
        userPrefs.middleName = data.profileDetails.middleName
        userPrefs.lastName = data.profileDetails.lastName
        userPrefs.commConsent = data.profileDetails.commConsent?:false
        data
    }

    /**
     * Revokes all refresh tokens for the current user across all devices.
     * Clears the token from prefs and interceptor after a successful logout.
     */
    suspend fun logout(): Resource<FsLogoutData> = safeApiCall {
        val data = fsAuthService.logout().toResource()
        //localLogout()
        data
    }

    /**
     * Returns the current user's profile from DFS DB.
     * Requires a valid token — call after [verify] or on app resume.
     */
    suspend fun getProfile(): Resource<FsUserProfile> = safeApiCall {
        val data = fsAuthService.getProfile().toResource()
        userPrefs.firstName = data.firstName
        userPrefs.middleName = data.middleName
        userPrefs.lastName = data.lastName
        userPrefs.commConsent = data.commConsent?:false
        data
    }

    /**
     * Updates the user's profile fields.
     *
     * @param firstName   Updated first name
     * @param lastName    Updated last name (optional)
     * @param commConsent Consent to receive app updates via contact details
     */
    suspend fun updateProfile(
        firstName: String? = null,
        middleName: String? = null,
        lastName: String? = null,
        commConsent: Boolean? = null
    ): Resource<FsUpdateProfileData> = safeApiCall {
        require(firstName != null && commConsent != null) {
            "Name and Communication Consent is required"
        }
        val data = fsAuthService.updateProfile(
            FsUpdateProfileRequest(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                commConsent = commConsent
            )
        ).toResource()
        // If the server returns a non-null first_name, the user has completed profile setup
        if (!data.user.firstName.isNullOrEmpty()) {
            userPrefs.isNewUser = false
        }
        data
    }

    /**
     * Generates a new access token and ID token using a valid refresh token.
     * Updates the interceptor with the new access token on success.
     *
     * @param refreshToken The refresh token received during login or signup
     */
/*    suspend fun refreshToken(
        refreshToken: String
    ): Resource<FsRefreshTokenData> = safeApiCall {
        val data = fsAuthService.refreshToken(
            FsRefreshTokenRequest(
                refreshToken = refreshToken
            )
        ).toResource()
        userPrefs.jwtToken = data.accessToken
        networkInterceptor.updateJWT(data.accessToken)
        data
    }*/

    fun isTokenPresent(): Boolean {
        return userPrefs.jwtToken.isNotNullOrEmpty() && userPrefs.refreshToken.isNotNullOrEmpty()
        /*return try {
            val expiresAt = JWT(userPrefs.refreshToken!!).expiresAt
            if (expiresAt != null && expiresAt.after(Date())) {
                true
            } else {
                *//* expired: clear credentials and logout *//*
                localLogout()
                false
            }
        } catch (e: Exception) {
            *//* malformed token: clear credentials and logout *//*
            localLogout()
            false
        }*/
    }

    fun localLogout() {
        networkInterceptor.updateJWT(null)
        userPrefs.clearPrefs()
    }
}
