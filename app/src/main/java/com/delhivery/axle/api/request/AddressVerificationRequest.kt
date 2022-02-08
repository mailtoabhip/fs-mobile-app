package com.delhivery.axle.api.request
import com.google.gson.annotations.SerializedName


data class UpdateAddressVerificationRequest(
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("address") var businessAddress: String,
    @SerializedName("is_same_as_gst") var isSameAsGst: Boolean

)
data class AddAddressRequest(
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("address") var address: String,
    @SerializedName("proof_document_type") var proofDocumentType: String,
    @SerializedName("document_urls") var documentUrls: List<String>,
    @SerializedName("address_type") var addressType: String
    )
{

}
