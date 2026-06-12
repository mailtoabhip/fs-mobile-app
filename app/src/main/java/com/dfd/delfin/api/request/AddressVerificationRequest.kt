package com.dfd.delfin.api.request
import com.google.gson.annotations.SerializedName


data class UpdateAddressVerificationRequest(
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("address") var businessAddress: String,
    @SerializedName("is_same_as_gst") var isSameAsGst: Boolean

)
data class AddAddressModel(
    @SerializedName("phone_number") var phoneNumber: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("proof_document_type") var proofDocumentType: String? = null,
    @SerializedName("document_urls") var documentUrls: List<String>? = null,
    @SerializedName("address_type") var addressType: String? = null,
    @SerializedName("is_deleted") var isDeleted: Boolean? = null)
