package com.delhivery.axle.ui.profile.kycdetails.fragments

import com.delhivery.axle.data.doc.DocWarningAction_NoResult
import com.delhivery.axle.data.doc.DocWarningItemData
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
 * @Zeplin https://zpl.io/2pvmPol
 */
val DocWarningItem_Transaction = DocWarningItem(
        DocWarningItemData(
                "No documents found",
                "Please try again",
                "", DocWarningAction_NoResult
        )
)

val DocItem_TimeOut = DocWarningItem(
        DocWarningItemData(
                "Session timed out!",
                "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
                "REFRESH", GstTimeOutAction
        )
)