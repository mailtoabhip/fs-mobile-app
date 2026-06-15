package com.dfd.delfin.data.doc

import com.dfd.delfin.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DocDetailData(
        @SerializedName("doc_url") var docUrl : String = "",
        @SerializedName("doc_path") var docPath : String? = null,
        @SerializedName("verification_status") val verificationStatus: String?,
        @SerializedName("verification_overall_type") val verificationOverallType: String?,
        @SerializedName("verification_type") val verificationType: String?,
        @SerializedName("verification_status_reason_code") val verificationStatusReasonCode: String?,
        @SerializedName("verification_status_reason_message") val verificationStatusReasonMessage: String?
): BaseKeyTypeModel<String>(), Serializable {
    override fun key() =  docUrl
}


/* actions */
const val DocAction_ViewDetails = "doc_download"

const val DocAction_ShowImage = "show_image"
