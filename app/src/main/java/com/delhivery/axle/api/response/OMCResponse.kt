package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class OMCResponse (
    @SerializedName("status") var status: String,
    @SerializedName("mobile_number") var mobileNumber: String,
    @SerializedName("trip_id") var tripId: String,
    @SerializedName("card_number") var cardnumber: String,
    @SerializedName("amount") var amount: String,
    @SerializedName("message") var message: String

)

data class OMCDetails(
    @SerializedName("total") var total : Int,
    @SerializedName("count") var count : Int,
    @SerializedName("items") var omcDetailsList : List<OMCData>
)

data class OMCData(
    @SerializedName("name") var name : String,
    @SerializedName("uuid") var uuid : String
)
