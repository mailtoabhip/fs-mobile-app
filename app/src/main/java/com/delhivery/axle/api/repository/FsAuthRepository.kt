package com.delhivery.axle.api.repository

import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.request.FsInitiateRequest
import com.delhivery.axle.api.request.FsRefreshTokenRequest
import com.delhivery.axle.api.request.FsResendRequest
import com.delhivery.axle.api.request.FsUpdateProfileRequest
import com.delhivery.axle.api.request.FsVerifyRequest
import com.delhivery.axle.api.response.FsInitiateData
import com.delhivery.axle.api.response.FsLogoutData
import com.delhivery.axle.api.response.FsRefreshTokenData
import com.delhivery.axle.api.response.FsResendData
import com.delhivery.axle.api.response.FsUpdateProfileData
import com.delhivery.axle.api.response.FsUserProfile
import com.delhivery.axle.api.response.FsVerifyData
import com.delhivery.axle.api.response.toResource
import com.delhivery.axle.api.service.FsAuthService
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.Date
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
        userPrefs.isNewUser = data.isNewUser ?: false
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
        fsAuthService.getProfile().toResource()
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
        lastName: String? = null,
        commConsent: Boolean? = null
    ): Resource<FsUpdateProfileData> = safeApiCall {
        require(firstName != null && commConsent != null) {
            "FirstName and Communication Consent is required"
        }
        val data = fsAuthService.updateProfile(
            FsUpdateProfileRequest(
                firstName = firstName,
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
     * @param accessToken  The expired access token
     */
    suspend fun refreshToken(
        refreshToken: String,
        accessToken: String
    ): Resource<FsRefreshTokenData> = safeApiCall {
        val data = fsAuthService.refreshToken(
            FsRefreshTokenRequest(
                refreshToken = refreshToken,
                accessToken = accessToken
            )
        ).toResource()
        userPrefs.jwtToken = data.accessToken
        networkInterceptor.updateJWT(data.accessToken)
        data
    }

    fun authStatus(): Boolean {
        if (!userPrefs.jwtToken.isNotNullOrEmpty() || !userPrefs.refreshToken.isNotNullOrEmpty()) {
            return false
        }
        return try {
            val expiresAt = JWT(userPrefs.refreshToken!!).expiresAt
            if (expiresAt != null && expiresAt.after(Date())) {
                true
            } else {
                /* expired: clear credentials and logout */
                localLogout()
                false
            }
        } catch (e: Exception) {
            /* malformed token: clear credentials and logout */
            localLogout()
            false
        }
    }

    fun localLogout() {
        networkInterceptor.updateJWT(null)
        userPrefs.clearPrefs()
    }
}
