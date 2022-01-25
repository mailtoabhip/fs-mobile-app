package com.delhivery.axle.data.gst

import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class GstDetailData(
        @SerializedName("gst_number") var gst_number : String,
        @SerializedName("phone_number") var phone_number : String?,
        @SerializedName("address") var address : String?
): BaseKeyTypeModel<String>(), Serializable {
    override fun key() =  gst_number
}

/* actions */
const val GstAction_ViewDetails = "gst_details"
