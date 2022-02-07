package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest (
    @SerializedName("phone_number") var phone_number: String = "",
    @SerializedName("user_mode") var user_mode: String? = null,
    @SerializedName("user_role") var user_role: String? = null,
    @SerializedName("user_name") var user_name: String? = null,
    @SerializedName("business_name") var business_name: String? = null,
    @SerializedName("referral_code") var referral_code: String? = null,
    @SerializedName("receive_whatsapp_notifications") var receive_whatsapp_notifications: Boolean? = null,
    @SerializedName("is_location_enabled") var is_location_enabled: Boolean? = null,
    @SerializedName("pan_number") var pan_number: String? = null
)