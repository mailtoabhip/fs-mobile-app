package com.delhivery.axle.data

import com.google.gson.annotations.SerializedName

data class TruckModel(
    @SerializedName("truck_display_name") val truckDisplayName: String,
    @SerializedName("truck_type") val truckType: String,
)


data class RouteInfo(
    @SerializedName("truck_display_name") val truckDisplayName: String,
    @SerializedName("truck_type") val truckType: String,
)


data class CostData(
    @SerializedName("kpm") val kpm: String,
    @SerializedName("hpd") val hpd: String,
    @SerializedName("dpm") val dpm: String,
    @SerializedName("rpk") val rpk: String,
    @SerializedName("rpd") val rpd: String,
    @SerializedName("rph") val rph: String,
    @SerializedName("nep") val nep: String,
)
