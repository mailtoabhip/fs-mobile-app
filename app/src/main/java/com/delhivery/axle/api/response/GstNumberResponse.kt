package com.delhivery.axle.api.response

import android.util.Log
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstDetailItemData
import com.google.gson.annotations.SerializedName

data class GstNumberData(
        @SerializedName("pan_number") var pan_number : String?,
        @SerializedName("gstin_numbers") var gstin_numbers : List<String>,
        @SerializedName("gstin_details") var gstin_details :List<GstDetailData>?
) {

    fun getGstList(gstinNumbers: List<String>): List<GstDetailData> {
        val gstin_details: ArrayList<GstDetailData> = ArrayList()
        if (!gstinNumbers.isNullOrEmpty()) {
            for (gst in gstinNumbers) {
                gstin_details.add(GstDetailData(gstNumber = gst))
            }
            Log.d("jfsjs", "fsfmko")
        }
        return gstin_details
    }
}

