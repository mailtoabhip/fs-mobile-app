package com.dfd.delfin.ui.home.activity.fuel

import com.dfd.delfin.data.transactions.TransactionTimeOutAction
import com.dfd.delfin.data.transactions.TransactionWarningAction_NoTransactions
import com.dfd.delfin.data.transactions.TransactionWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val ActiveTripWarningItem_NoTrip = ActiveTripWarningItem(
    TransactionWarningItemData(
        "No Active trips",
        "You can only take fuel credit against vehicles that are on their way right now",
        "START BIDDING NOW!", TransactionWarningAction_NoTransactions
    )
)

val ActiveTripWarningItem_TimeOut = ActiveTripWarningItem(
    TransactionWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", TransactionTimeOutAction
    )
)

