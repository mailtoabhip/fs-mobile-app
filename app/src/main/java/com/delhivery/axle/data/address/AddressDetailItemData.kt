package com.delhivery.axle.data.address

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class AddressDetailData(
    @SerializedName("phone_number") var phone_number : String? ,
    @SerializedName("address") var address : String,
    @SerializedName("proof_document_type") var proofDocumentType: String? = null,
    @SerializedName("document_urls") var documentUrls: List<String>? = null,
    @SerializedName("address_type") var addressType: String? = null,
    @SerializedName("is_deleted") var isDeleted: Boolean? = null,
    @SerializedName("is_selected") var isSelected: Boolean? = null

): BaseKeyTypeModel<String>(), Serializable {
    override fun key() =  address
     fun get() = addressType
    fun getPhoneNumber() = phone_number
    fun getProofDocType() = proofDocumentType
    fun getDocUrl() = documentUrls
    fun getIsDeleted() = isDeleted
    fun getIsSelected() = isSelected



}

/* actions */
const val AddressAction_ViewDetails = "Address_details"