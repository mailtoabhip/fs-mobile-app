package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class TruckResponse(
    @SerializedName("success") var success : Boolean?   = null,
    @SerializedName("data") var data : List<TruckResponseArray> = arrayListOf()

)

data class TruckResponseArray(
    @SerializedName("truck_type") var truckType : String?   = null,
    @SerializedName("truck_uuid") var truckUuid : String?   = null,
    @SerializedName("allowed_tonnages") var allowedTonnages : List<Double> = arrayListOf(),
    @SerializedName("truck_display_name") var truckDisplayName : String?   = null,
    @SerializedName("max_capacity") var maxCapacity  : Double?      = null,
    @SerializedName("default_MG") var defaultMG  : Double?   = null,
    @SerializedName("min_capacity") var minCapacity : Double?      = null,
    @SerializedName("sourced_as") var sourcedAs  : String?   = null

)
