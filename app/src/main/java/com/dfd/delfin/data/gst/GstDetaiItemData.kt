package com.dfd.delfin.data.gst

import com.dfd.delfin.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GstDetailData(
        @SerializedName("gst_number") var gstNumber : String = "",
        @SerializedName("currSelectedGst") var currSelectedGst : String? = null,
        @SerializedName("prevSelectedGst") var prevSelectedGst : String? = null,
        var gstDetailItemData : GstDetailItemData? = null
): BaseKeyTypeModel<String>(), Serializable {
    override fun key() =  gstNumber
}

data class GstDetailItemData(
        @SerializedName("gst_number") var gstNumber : String? = null,
        @SerializedName("phone_number") var phoneNumber : String? = null,
        @SerializedName("address") var address : String? = null,
        @SerializedName("is_selected") var isSelected : Boolean? = null
):Serializable

/* actions */
const val GstAction_ViewDetails = "gst_details"