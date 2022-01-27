package com.delhivery.axle.ui.auth

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIError.*
import com.delhivery.axle.ui.auth.AuthenticationUIState.*
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

/**
 * View model for [AuthenticationActivity]
 */
class AuthenticationViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val userRepository: UserRepository,
  private val notificationRepository: NotificationRepository,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  var otpStatusLiveData = MutableLiveData<Boolean>()

  /* states */
  var stateLiveData = MutableLiveData<AuthenticationUIState>()
  var state: AuthenticationUIState = PhoneNo
    set(value) {
      stateLiveData.postValue(value)
    }

  /* error live data */
  var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

  /* otp data */
  var otpCurrent:String = ""

  /* binding vars */
  var phoneNo: String = ""
  var otpSendCount: Int = 1

  /**
   * Send OTP
   */
  fun sendOTP() {
    if (!isConnected) return

    if (phoneNo.length < 10) {
      errorLiveData.postValue(Pair(InvalidPhoneNo, null))
      return
    }

    userPrefs.phoneNumber = phoneNo
    //make api call and move to otp state
    otpStatusLiveData.postValue(true)

    compositeDisposable += authenticationRepository.sendOTP(phoneNo)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          state = if (!error && _res.first) {
            OTP
          } else {
            errorLiveData.postValue(Pair(InvalidPhoneNo, _res.second))
            PhoneNo
          }
        }
  }

  /**
   * Verify OTP
   */
  fun verifyOTP(otp: CharArray) {
    if (!isConnected) return

    /* set state to login progress and verify otp */
    state = LoginProgress
    val _otp = otp.joinToString("")
    compositeDisposable += Single.zip(
        authenticationRepository.verifyOTP(phoneNo, _otp),
        Single.timer(1000, MILLISECONDS), //add delay for animation
        BiFunction<Pair<Boolean, String>, Any, Pair<Boolean, String?>> { t1, _ -> t1 })
        .flatMap { _otpRes ->
          userRepository.getUser(false)
              .map {
                val msg = if (_otpRes.second.isNotNullOrEmpty()) {
                  _otpRes.second
                } else {
                  "Error validating OTP"
                }
                Triple(_otpRes.first, msg, it)
              }
        }
        .onBackground()
        .subscribe { _res, error ->
          state = if (!error && _res.first) {
            if (!_res.third.supplierEnabled) {
              userPrefs.hasLoggedIn = false
              Disabled
            } else if (_res.third.isDeleted) {
              userPrefs.hasLoggedIn = false
              Disabled
            } else if (_res.third.hasRoutes() && userPrefs.hasEditedRoute) {
              userPrefs.hasLoggedIn = true
              userPrefs.lastLoginTime = Date().time
             //LoadRequest
              AccountSetup
            } else {
              userPrefs.hasLoggedIn = true
              userPrefs.hasEditedRoute = true
              userPrefs.lastLoginTime = Date().time
              //SelectRoute
              AccountSetup
            }
          } else {
            if (error is HttpException) {
              userPrefs.hasLoggedIn = false
              error.handle()
            }
            errorLiveData.postValue(Pair(InvalidOTP, ""))
            OTP
          }
        }
  }

  /**
   * Login password
   */
  fun loginUsingPassword(userName:String,password:String) {
    /* set state to login progress and verify otp */
    state = LoginProgress
    compositeDisposable += Single.zip(
      authenticationRepository.loginUsingPassword(userName, password),
      Single.timer(1000, MILLISECONDS), //add delay for animation
      BiFunction<Pair<Boolean, String>, Any, Pair<Boolean, String?>> { t1, _ -> t1 })
      .flatMap { _otpRes ->
        userRepository.getUser(false)
          .map {
            val msg = if (_otpRes.second.isNotNullOrEmpty()) {
              _otpRes.second
            } else {
              "Error validating UserName/Password"
            }
            Triple(_otpRes.first, msg, it)
          }
      }
      .onBackground()
      .subscribe { _res, error ->
        state = if (!error && _res.first) {
          if (!_res.third.supplierEnabled) {
            userPrefs.hasLoggedIn = false
            Disabled
          } else if (_res.third.isDeleted) {
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
//          if (error is HttpException) {
//            userPrefs.hasLoggedIn = false
//            error.handle()
//          }
          errorLiveData.postValue(Pair(InvalidPassword, ""))
          Password
        }
      }
  }

  /**
   * Mark notification as read
   */
  fun markNotificationRead(id: String) {
    compositeDisposable += notificationRepository.markNotificationRead(id)
        .onBackground()
        .subscribe { _, _ ->

        }
  }

}