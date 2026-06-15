package com.dfd.delfin.ui.home.fragments.wallet.viewpager

import com.dfd.delfin.R

/**
 * On-boarding config
 */
data class WalletIntroScreen(
  val title: String,
  val message: String,
  val icon: Int
)

/**
 * On-boarding config [List] of [WalletIntroScreen]
 */
val WalletIntroConfig = listOf(
    WalletIntroScreen(
        "Introducing Axle Money",
        "Receive Advance amount as Axle Money - Use for fuel purchase and transfer remaining amount from Axle Money your bank",
        R.drawable.ic_balance_intro
    ),
    WalletIntroScreen(
        "Introductory Offer of 3% Cashback on Fuel Purchase",
        "Receive 3% cashback on fuel usage in bank account; lower your Operating Costs with us",
        R.drawable.ic_fuel_intro
    ),
    WalletIntroScreen(
        "Track your payments easily",
        "Get digital record of all transactions along with date and Bank Transaction ID",
        R.drawable.ic_transaction_intro
    )
)