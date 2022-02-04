package com.delhivery.axle.ui.accountdetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.ui.accountaction.AccountActionActivity
import com.delhivery.axle.ui.accountaction.AccountType
import com.delhivery.axle.ui.accountrole.AccountRole
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIState
import com.delhivery.axle.ui.auth.AuthenticationUIState.*
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.Single.zip
import io.reactivex.functions.BiFunction
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * View model for [AccountDetailsActivity]
 */
class AccountDetailsViewModel @Inject constructor(
        private val loadboardRepository: LoadboardRepository,
        private val userRepository: UserRepository,
        private val userPrefs: UserPrefs
) : BaseViewModel() {

    /* states */
    var stateLiveData = MutableLiveData<AuthenticationUIState>()
    var mode:AccountType? = null
    var role: AccountRole? = null

    var state: AuthenticationUIState = AuthenticationUIState.PhoneNo
        set(value) {
            stateLiveData.postValue(value)
        }

    var updateUserStatusLiveData = MutableLiveData<String>()
    var createUserStatusLiveData = MutableLiveData<String>()

    var username = MutableLiveData<String>()
    var business_name = MutableLiveData<String>()
    var referral_code = MutableLiveData<String>()
    var whatsapp = MutableLiveData<Boolean>()
    var termsCheck = MutableLiveData<Boolean>()
    var locationOption = MutableLiveData<Boolean>()

    /**
     * Create user
     */
    fun createAccount(updateUserRequest: UpdateUserRequest) {
        if (!isConnected) return
        state = LoginProgress
        updateUserRequest.phone_number = userPrefs.phoneNumber.toString()
        compositeDisposable += Single.zip(
                loadboardRepository.createUser(updateUserRequest),
                Single.timer(1000, TimeUnit.MILLISECONDS), //add delay for animation
        BiFunction<Pair<Boolean, String>, Any, Pair<Boolean, String?>> { t1, _ -> t1 }
        ).flatMap { _otpRes ->
            userRepository.getUser(false)
                    .map {
                        val msg = if (_otpRes.second.isNotNullOrEmpty()) {
                            _otpRes.second
                        } else {
                            "Account not created"
                        }
                        Triple(_otpRes.first, msg, it)
                    }
        }
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    state = if (!error && _res.first) {
                        if (!_res.third.is_sp_enabled && !_res.third.is_client_enabled) {
                            userPrefs.hasLoggedIn = false
                            AccountAction
                        } else if (_res.third.user_role.isNullOrEmpty()) {
                            userPrefs.hasLoggedIn = false
                            AuthenticationUIState.AccountRole
                        }else if (_res.third.user_name.isNullOrEmpty() || _res.third.business_name.isNullOrEmpty()) {
                            userPrefs.hasLoggedIn = false
                            AccountDetails
                        } else if (_res.third.supplier_details?.isDeleted == true) {
                            userPrefs.hasLoggedIn = false
                            Disabled
                        } else if (_res.third.hasRoutes() && userPrefs.hasEditedRoute) {
                            userPrefs.hasLoggedIn = true
                            userPrefs.lastLoginTime = Date().time
                            LoadRequest
                        } else {
                            userPrefs.hasLoggedIn = true
                            userPrefs.hasEditedRoute = true
                            userPrefs.lastLoginTime = Date().time
                            SelectRoute
                        }
                    } else {
                        if (error is HttpException) {
                            userPrefs.hasLoggedIn = false
                        }
                        OTP
                    }
                }
    }
}