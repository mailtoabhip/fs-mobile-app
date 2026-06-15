package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class RecommendedDriverResponse(
    @SerializedName("recommended_drivers")var recommendedDrivers:List<DriverDataResponse>?,

    )
data class DriverDataResponse(
    @SerializedName("driver_name")var driverName:String?,
    @SerializedName("driver_phone")var driverPhone:String?,
)
