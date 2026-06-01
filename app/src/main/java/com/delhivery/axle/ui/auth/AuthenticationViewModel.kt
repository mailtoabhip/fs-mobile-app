package com.delhivery.axle.ui.auth

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FsAuthRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState.AccountDetails
import com.delhivery.axle.ui.auth.AuthenticationUIState.HomePage
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.axle.ui.auth.AuthenticationUIState.OTP
import com.delhivery.axle.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * View model for [AuthenticationActivity]
 */
class AuthenticationViewModel @Inject constructor(
    private val fsAuthRepository: FsAuthRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    var otpStatusLiveData = MutableLiveData<Boolean>()

    /* states */
    var stateLiveData = MutableLiveData<AuthenticationUIState>()
    var state: AuthenticationUIState = PhoneNo
        set(value) {
            stateLiveData.postValue(value)
        }

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

    /* binding vars */
    var phoneNo: String = ""
    var otpSendCount: Int = 1

    /** Session token from /auth/initiate — null for new users, required for existing users */
    private var loginSession: String? = null

    /**
     * Initiate OTP flow (POST /api/v1/auth/initiate).
     * Handles both new and existing users.
     */
    fun sendOTP() {
        if (!isConnected) return

        val rawPhone = phoneNo.replace(" ", "")
        if (rawPhone.length < 10) {
            errorLiveData.postValue(Pair(InvalidPhoneNo, null))
            return
        }

        userPrefs.phoneNumber = rawPhone
        otpStatusLiveData.postValue(true)
        showProgress()

        viewModelScope.launch {
            when (val result = fsAuthRepository.initiate(rawPhone)) {
                is Resource.Success -> {
                    showProgress(false)
                    val data = result.data
                    if (data != null) {
                        loginSession = data.session
                        state = OTP
                    } else {
                        otpStatusLiveData.postValue(false)
                        errorLiveData.postValue(Pair(InvalidOTP, "Failed to send OTP"))
                        state = PhoneNo
                    }
                }
                is Resource.Failure -> {
                    showProgress(false)
                    otpStatusLiveData.postValue(false)
                    Log.e("AuthVM", "initiate failed: code=${result.errorCode}, error=${result.apiError}")
                    errorLiveData.postValue(Pair(InvalidOTP, "Failed to send OTP. Please try again."))
                    state = PhoneNo
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Resends OTP (POST /api/v1/auth/resend).
     * Keeps the user on the OTP screen on success.
     */
    fun resendOTP() {
        if (!isConnected) return

        val rawPhone = phoneNo.replace(" ", "")
        if (rawPhone.length < 10) {
            errorLiveData.postValue(Pair(InvalidPhoneNo, null))
            return
        }

        otpStatusLiveData.postValue(true)
        showProgress()

        viewModelScope.launch {
            when (val result = fsAuthRepository.resend(rawPhone)) {
                is Resource.Success -> {
                    showProgress(false)
                    otpStatusLiveData.postValue(true)
                }
                is Resource.Failure -> {
                    showProgress(false)
                    otpStatusLiveData.postValue(false)
                    Log.e("AuthVM", "resend failed: code=${result.errorCode}, error=${result.apiError}")
                    errorLiveData.postValue(Pair(InvalidOTP, "Failed to resend OTP. Please try again."))
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Verify OTP (POST /api/v1/auth/verify).
     * On success, fetches user profile to decide navigation:
     *  - Profile has first_name → existing user → [HomePage]
     *  - Profile missing first_name → new user → [AccountDetails]
     */
    fun verifyOTP(otp: CharArray) {
        if (!isConnected) return

        state = LoginProgress
        val otpString = otp.joinToString("")
        val rawPhone = phoneNo.replace(" ", "")

        viewModelScope.launch {
            when (val result = fsAuthRepository.verify(rawPhone, otpString, loginSession)) {
                is Resource.Success -> {
                    val tokens = result.data
                    if (tokens != null) {
                        // Token is persisted and interceptor updated inside FsAuthRepository.verify()
                        userPrefs.hasLoggedIn = true
                        userPrefs.lastLoginTime = Date().time

                        // Fetch profile to determine navigation
                        when (val profileResult = fsAuthRepository.getProfile()) {
                            is Resource.Success -> {
                                val profile = profileResult.data
                                if (profile != null && profile.firstName.isNotNullOrEmpty() && profile.lastName.isNotNullOrEmpty()) {
                                    // Existing user with completed profile
                                    userPrefs.userName = "${profile.firstName} ${profile.lastName.orEmpty()}".trim()
                                    userPrefs.phoneNumber = profile.phone
                                    userPrefs.firstName =  profile.firstName
                                    userPrefs.lastName =  profile.lastName
                                    state = HomePage
                                } else {
                                    // New user or incomplete profile
                                    state = AccountDetails
                                }
                            }
                            is Resource.Failure -> {
                                // Profile fetch failed — show error and let user retry OTP
                                Log.w("AuthVM", "Profile fetch failed: ${profileResult.apiError}")
                                errorLiveData.postValue(Pair(InvalidOTP, "Something went wrong. Please try again."))
                                state = OTP
                            }
                            Resource.Loading -> { /* no-op */ }
                        }
                    } else {
                        errorLiveData.postValue(Pair(InvalidOTP, ""))
                    }
                }
                is Resource.Failure -> {
                    Log.e("AuthVM", "verify failed: code=${result.errorCode}, error=${result.apiError}")
                    userPrefs.hasLoggedIn = false
                    errorLiveData.postValue(Pair(InvalidOTP, ""))
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun logout() {
        userPrefs.clearPrefs()
    }
}
