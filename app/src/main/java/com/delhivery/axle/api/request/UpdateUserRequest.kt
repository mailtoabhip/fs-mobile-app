package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest (
    @SerializedName("phone_number") var phoneNumber: String = "",
    @SerializedName("user_mode") var userMode: String? = null,
    @SerializedName("user_role") var userRole: String? = null,
    @SerializedName("user_name") var userName: String? = null,
    @SerializedName("business_name") var businessName: String? = null,
    @SerializedName("referral_code") var referralCode: String? = null,
    @SerializedName("receive_whatsapp_notifications") var receiveWhatsappNotifications: Boolean? = null,
    @SerializedName("is_location_enabled") var isLocationEnabled: Boolean? = null,
    @SerializedName("pan_number") var panNumber: String? = null,
    @SerializedName("aadhaar_number") val aadhaarNumber:String? =null,
    @SerializedName("gst_number") val gstNumber:String? = null,
    @SerializedName("profile_image_url") val profileImageUrl:String? = null,
    @SerializedName("can_view_third_party_loads") val can_view_third_party_loads:Boolean? = null
)