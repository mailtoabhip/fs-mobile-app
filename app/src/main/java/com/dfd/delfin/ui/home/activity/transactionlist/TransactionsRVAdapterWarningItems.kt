package com.dfd.delfin.ui.home.activity.transactionlist

import com.dfd.delfin.data.transactions.TransactionTimeOutAction
import com.dfd.delfin.data.transactions.TransactionWarningAction_NoTransactions
import com.dfd.delfin.data.transactions.TransactionWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val TransactionWarningItem_Transaction = TransactionWarningItem(
    TransactionWarningItemData(
        "No Transactions found",
        "",
        "", TransactionWarningAction_NoTransactions
    )
)

val TransactionWarningItem_TimeOut = TransactionWarningItem(
    TransactionWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", TransactionTimeOutAction
    )
)

