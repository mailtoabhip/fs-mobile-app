package com.delhivery.orion.ui.auth

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoadRequest
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.orion.ui.auth.AuthenticationUIState.OTP
import com.delhivery.orion.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.orion.ui.auth.AuthenticationUIState.SelectRoute
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class AuthenticationViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val userRepository: UserRepository
) :
    BaseViewModel() {

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
    //make api call and move to otp state
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
        BiFunction<Pair<Boolean, String>, Any, Pair<Boolean, String>> { t1, _ -> t1 })
        .flatMap { _otpRes ->
          userRepository.getUser(false)
              .map {
                Triple(_otpRes.first, _otpRes.second, it)
              }
        }
        .onBackground()
        .subscribe { _res, error ->
          state = if (!error && _res.first) {
            if (_res.third.hasRoutes()) {
              LoadRequest
            } else {
              SelectRoute
            }
          } else {
            errorLiveData.postValue(Pair(InvalidOTP, _res.second))
            OTP
          }
        }
  }

}