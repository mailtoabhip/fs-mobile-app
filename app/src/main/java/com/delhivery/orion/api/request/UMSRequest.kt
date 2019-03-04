package com.delhivery.orion.api.request

import com.google.gson.annotations.SerializedName

data class RequestOTP(@SerializedName("phone_number") val phoneNo: String) {
  companion object {
    /**
     * Get appended phone no request
     *
     * @param phoneNo raw phone number
     * @return [RequestOTP] request body
     */
    fun getRequest(phoneNo: String) = RequestOTP("+91$phoneNo")
  }
}

data class OTPLoginRequest(
  @SerializedName("phone_number") val phoneNo: String,
  @SerializedName("otp") val otp: String
) {
  companion object {
    /**
     * Get appended phone no and otp request for login
     *
     * @param phoneNo raw phone number
     * @param otp received otp
     */
    fun getRequest(
      phoneNo: String,
      otp: String
    ) = OTPLoginRequest("+91$phoneNo", otp)
  }
}