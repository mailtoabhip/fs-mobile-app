package com.delhivery.axle.data.address

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class AddressDetailData(
    @SerializedName("phone_number") var phone_number : String?,
    @SerializedName("address") var address : String
): BaseKeyTypeModel<String>(), Serializable {
    override fun key() =  address
}

/* actions */
const val AddressAction_ViewDetails = "Address_details"