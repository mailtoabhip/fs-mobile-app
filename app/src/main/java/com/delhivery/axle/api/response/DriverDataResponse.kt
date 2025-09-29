package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class DriverDataResponse(
    @SerializedName("driver_name")var driverName:String?,
    @SerializedName("driver_phone")var driverPhone:String?,
)
