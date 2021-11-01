package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FuelPayoutRequest(
    @SerializedName("action_code") var action_code: String?,
    @SerializedName("action_sub_code") var action_sub_code: String?,
    @SerializedName("trip_id") var trip_id: String?,
    @SerializedName("data") var data: FuelPayoutData
)

data class FuelPayoutData(
    @SerializedName("user_confirmation") var userConfirmation: Boolean,
    @SerializedName("fuel_payout") var fuelPayout: Int,
    @SerializedName("omc_name") var omcName: String,
    @SerializedName("omc_id") var omcId: String,
    @SerializedName("card_type") var cardType: String,
    @SerializedName("fuel_mobile_no") var fuelMobileNo: String,
    @SerializedName("selected_bid") var selectedBid: String
)
