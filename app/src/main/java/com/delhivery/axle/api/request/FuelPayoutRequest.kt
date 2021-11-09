package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FuelPayoutRequest(
    @SerializedName("card_type") var cardType : String,
    @SerializedName("fuel_mobile_no") var fuelMobileNo : String,
    @SerializedName("fuel_payout") var fuelPayout : String,
    @SerializedName("omc_name") var omcName : String,
    @SerializedName("omc_id") var omcId : String,
    @SerializedName("update_type") var updateType : String
)

