package com.delhivery.axle.ui.home.activity.transactionlist

import androidx.annotation.LayoutRes
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.transactions.TransactionsItemData

/**
 * Base Transaction state with [containerId]
 */
abstract class TransactionState(@LayoutRes val containerId: Int)

/**
 * Fuel Cashback Credit
 */
data class TransactionStateFuelCashbackCredit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_cashback_credit_transaction)

/**
 * Unused Fuel Credit
 */
data class TransactionStateFuelRevertCredit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_credit_revert_transaction)

/**
 * Fuel Credit
 */
data class TransactionStateFuelDebit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_debit_transaction)

/**
 * Advance Credit
 */
data class TransactionStateAdvanceCredit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_advance_credit_transaction)

/**
 * Bank transfer
 */
data class TransactionStateBankTransfer(
  var transaction: TransactionsItemData
) : TransactionState(R.layout.view_bank_transfer_transaction)

/**
 * Loading
 */
data class TransactionStateLoading(
  val loading: Boolean = true
) : TransactionState(R.layout.view_bid_details_loading_bids)
