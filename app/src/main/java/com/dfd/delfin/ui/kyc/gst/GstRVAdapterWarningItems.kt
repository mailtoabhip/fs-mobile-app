package com.dfd.delfin.ui.kyc.gst

import com.dfd.delfin.data.gst.GstTimeOutAction
import com.dfd.delfin.data.gst.GstWarningAction_NoResult
import com.dfd.delfin.data.gst.GstWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val GstWarningItem_Transaction = GstWarningItem(
        GstWarningItemData(
                "No Gst details found",
                "Please try again",
                "", GstWarningAction_NoResult
        )
)

val GstItem_TimeOut = GstWarningItem(
        GstWarningItemData(
                "Session timed out!",
                "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
                "REFRESH", GstTimeOutAction
        )
)