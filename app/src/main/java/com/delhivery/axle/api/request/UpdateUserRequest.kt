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
    @SerializedName("can_view_third_party_loads") val canViewThirdPartyLoads:Boolean? = null,
    @SerializedName("is_trucking_business_doc_uploaded") val isTruckingDocumentUploaded:Boolean? =null,
    @SerializedName("rc_number") val rcNumber:String? =null,
    @SerializedName("cin_number") val cinNumber:String? =null,
    @SerializedName("udyog_aadhaar_number") val udyogAadhaarNumber:String? =null,
    @SerializedName("shop_establishment_number") val shopEstablishmentNumber:String? =null,
    @SerializedName("route_preferences") val routePreferences:List<OriginDestinations>? =null,
    @SerializedName("truck_preferences") val truckPreferences:List<String>? =null,
    @SerializedName("number_of_owned_trucks") val numberOfOwnedTrucks:Int? =null,
    @SerializedName("number_of_attached_trucks") val numberOfAttachedTrucks:Int? =null,
    @SerializedName("account_number") val accountNumber:String? =null,
    @SerializedName("ifsc_code") val ifscCode:String? =null,
    @SerializedName("account_holder_name") val accountHolderName:String? =null,
    @SerializedName("name_declaration") val nameDeclaration:String? =null,
    @SerializedName("vendor_policy_accepted") var vendorPolicyAccepted: Boolean?=null,
    @SerializedName("aadhaar_policy_accepted") var aadhaarPolicyAccepted: Boolean?=null,
    @SerializedName("requested_deletion") var requestedDeletion: Boolean?=null


)

data class OriginDestinations(
    @SerializedName("origin") var origin: RouteDetails?=null,
    @SerializedName("destination") var destination: RouteDetails?=null
)

data class RouteDetails(
    @SerializedName("location_id") var locationId: String?=null,
    @SerializedName("location") var location: String?=null,
    @SerializedName("location_type") var locationType: String?=null
)
