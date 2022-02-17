package com.delhivery.axle.ui.profile.kycdetails.fragments

import com.delhivery.axle.data.doc.DocTimeOutAction
import com.delhivery.axle.data.doc.DocWarningAction_NoResult
import com.delhivery.axle.data.doc.DocWarningItemData

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
                "REFRESH", DocTimeOutAction
        )
)