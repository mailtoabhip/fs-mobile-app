package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FacilityAddressResponse(
    @SerializedName("property_address_details")var propertyAddressDetails:PropertyAddressDetails?,
    @SerializedName("property_name")var propertyName:String?,
    @SerializedName("property_pin_code")var propertyPinCode:String?

)

data class PropertyAddressDetails(
    @SerializedName("address")var address: String?=null
)
