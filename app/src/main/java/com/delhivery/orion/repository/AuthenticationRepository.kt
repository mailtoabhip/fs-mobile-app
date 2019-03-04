package com.delhivery.orion.repository

import com.delhivery.orion.api.UMSService
import com.delhivery.orion.api.request.OTPLoginRequest
import com.delhivery.orion.api.request.RequestOTP
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository
 */
@Singleton
class AuthenticationRepository @Inject constructor(private val UMSService: UMSService) :
    BaseRepository() {

  /**
   * Send otp to phone number and return if success and error message
   */
  fun sendOTP(phoneNo: String) =
    UMSService.requestOTP(RequestOTP.getRequest(phoneNo))
        .map {
          Pair(true, it.successMsg)
        }
        .onErrorReturn {
          /* handle error if needed */
          Pair(false, "Invalid phone number")
        }

  /**
   * Verify OTP
   */
  fun verifyOTP(
    phoneNo: String,
    otp: String
  ) = UMSService.otpLogin(OTPLoginRequest.getRequest(phoneNo, otp))
      .map {
        //todo handle jwt here
        Pair(true, "")
      }
      .onErrorReturn {
        /* handle error if needed */
        Pair(false, "Invalid OTP")
      }
}