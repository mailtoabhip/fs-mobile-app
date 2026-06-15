package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class RcVerificationRequest(
    @SerializedName("vehicle_number") var vehicleNumber: String
    )
