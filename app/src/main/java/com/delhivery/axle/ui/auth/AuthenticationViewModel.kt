package com.delhivery.axle.ui.auth

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.repository.AuthenticationRepository
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoadRequest
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.axle.ui.auth.AuthenticationUIState.OTP
import com.delhivery.axle.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState.SelectRoute
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import retrofit2.HttpException
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class AuthenticationViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val userRepository: UserRepository,
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

  /* binding vars */
  var phoneNo: String = ""

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
            phoneNo = ""
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
                userPrefs.userName = it.name
                userPrefs.tdsRate = it.getTDS()
                if (it.hasRoutes()) {
                  userPrefs.cityCode = it.userRoutes()
                      .get(0)
                      .origin.cityId
                } else {
                  userPrefs.cityCode = it.baseCityCode
                }
                val msg =
                  if (_otpRes.second.isNotNullOrEmpty()) _otpRes.second else "Error validating OTP"
                Triple(_otpRes.first, msg, it)
              }
        }
        .onBackground()
        .subscribe { _res, error ->
          state = if (!error && _res.first) {
            userPrefs.hasLoggedIn = true
            if (_res.third.hasRoutes()) {
              LoadRequest
            } else {
              SelectRoute
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

}