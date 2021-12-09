package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

data class TruckResponse(
//    @SerializedName("success")val status:Boolean,
//    @SerializedName("data") val data:ArrayList<TruckResponseArray>
    @SerializedName("success" ) var success : Boolean?   = null,
    @SerializedName("data"    ) var data    : List<TruckResponseArray> = arrayListOf()

)

data class TruckResponseArray(
//    @SerializedName("truck_type") val truckType: String,
//    @SerializedName("truck_display_name") val truckDisplayName: String,
//    @SerializedName("truck_uuid") val truckUUID: String,
//    @SerializedName("max_capacity") val maxCapacity: Int,
//    @SerializedName("default_MG") val defultMG: Double,
//    @SerializedName("min_capacity") val minCapacity: Int


    @SerializedName("truck_type"         ) var truckType        : String?   = null,
    @SerializedName("truck_uuid"         ) var truckUuid        : String?   = null,
    @SerializedName("allowed_tonnages"   ) var allowedTonnages  : List<Double> = arrayListOf(),
    @SerializedName("truck_display_name" ) var truckDisplayName : String?   = null,
    @SerializedName("max_capacity"       ) var maxCapacity      : Double?      = null,
    @SerializedName("default_MG"         ) var defaultMG        : Double?   = null,
    @SerializedName("min_capacity"       ) var minCapacity      : Double?      = null,
    @SerializedName("sourced_as"         ) var sourcedAs        : String?   = null

)
