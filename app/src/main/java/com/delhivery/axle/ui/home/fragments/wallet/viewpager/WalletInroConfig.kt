package com.delhivery.axle.ui.home.fragments.wallet.viewpager

import com.delhivery.axle.R

/**
 * On-boarding config
 */
data class WalletIntoScreen(
  val title: String,
  val message: String,
  val icon: Int
)

/**
 * On-boarding config [List] of [WalletIntoScreen]
 */
val WalletIntroConfig = listOf(
    WalletIntoScreen(
        "Introducing your balance",
        "Receive advance money in Your Balance - Use for fuel purchase and transfer remaining to your bank",
        R.drawable.ic_balance_intro
    ),
    WalletIntoScreen(
        "Introductory Offer Avail 3% cashback on fuel purchase",
        "Get 3% cashback into account on fuel used and lower your operating cost while working with us",
        R.drawable.ic_fuel_intro
    ),
    WalletIntoScreen(
        "Track your payments easily",
        "Get digital record of all transactions along with date and Bank Transaction ID",
        R.drawable.ic_transaction_intro
    )
)