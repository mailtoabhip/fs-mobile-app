package com.delhivery.axle.api.request
import com.google.gson.annotations.SerializedName

data class AddAddressVerificationRequest(
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("business_address") var businessAddress: String,
    @SerializedName("proof_document_type") var proofDocumentType: String,
    @SerializedName("proof_document_url") var proofDocumentUrl: String

)
data class UpdateAddressVerificationRequest(
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("business_address") var businessAddress: String,
    @SerializedName("is_same_as_gst") var isSameAsGst: Boolean
)