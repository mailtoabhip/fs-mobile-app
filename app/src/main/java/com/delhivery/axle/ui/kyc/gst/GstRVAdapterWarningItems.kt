package com.delhivery.axle.ui.kyc.gst

import com.delhivery.axle.data.gst.GstTimeOutAction
import com.delhivery.axle.data.gst.GstWarningAction_NoResult
import com.delhivery.axle.data.gst.GstWarningItemData
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.data.search.SearchWarningItemData
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.data.transactions.TransactionWarningAction_NoTransactions
import com.delhivery.axle.data.transactions.TransactionWarningItemData
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionWarningItem

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