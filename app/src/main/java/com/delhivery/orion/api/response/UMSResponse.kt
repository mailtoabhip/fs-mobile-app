package com.delhivery.orion.api.response

import com.google.gson.annotations.SerializedName

data class OTPSentResponse(@SerializedName("success") val successMsg: String)

data class LoginResponse(@SerializedName("jwt") val jwtToken: String)