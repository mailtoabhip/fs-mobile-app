package com.delhivery.orion.ui.auth

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoginSuccess
import com.delhivery.orion.ui.auth.AuthenticationUIState.OTP
import com.delhivery.orion.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class AuthenticationViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) :
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
    compositeDisposable += authenticationRepository.verifyOTP(phoneNo, _otp)
        .onBackground()
        .subscribe { _res, error ->
          state = if (!error && _res.first) {
            LoginSuccess
          } else {
            errorLiveData.postValue(Pair(InvalidOTP, _res.second))
            OTP
          }
        }
  }

}