package com.dfd.delfin.ui.home.activity.transactionlist

import androidx.annotation.LayoutRes
import com.dfd.delfin.R
import com.dfd.delfin.data.fuelcards.FuelCardData
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.transactions.TransactionChannel.ORACLE
import com.dfd.delfin.data.transactions.TransactionType.ADVANCE_AUTO_DEBIT
import com.dfd.delfin.data.transactions.TransactionType.ADVANCE_CREDIT
import com.dfd.delfin.data.transactions.TransactionType.CREDIT
import com.dfd.delfin.data.transactions.TransactionType.DEBIT
import com.dfd.delfin.data.transactions.TransactionType.DEBIT_NOTE
import com.dfd.delfin.data.transactions.TransactionType.PETRO_CASHBACK_CREDIT
import com.dfd.delfin.data.transactions.TransactionType.PETRO_CASHBACK_DEBIT
import com.dfd.delfin.data.transactions.TransactionType.PETRO_REFUND_CREDIT
import com.dfd.delfin.data.transactions.TransactionType.RECONCILIATION_DEBIT
import com.dfd.delfin.data.transactions.TransactionsItemData

/**
 * Base Transaction state with [containerId]
 */
abstract class TransactionState(@LayoutRes val containerId: Int)

/**
 * [PETRO_CASHBACK_CREDIT]
 */
data class TransactionStateFuelCashbackCredit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_cashback_credit_transaction)

/**
 * [PETRO_REFUND_CREDIT]
 */
data class TransactionStateFuelRevertCredit(
  var transaction: TransactionsItemData,
  var tripAndFuel: Pair<HomeTripsItemData, FuelCardData>
) : TransactionState(R.layout.view_fuel_credit_revert_transaction)

/**
 * [DEBIT]
 */
data class TransactionStateFuelDebit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_debit_transaction)

/**
 * [ADVANCE_CREDIT], [CREDIT]
 */
data class TransactionStateAdvanceCredit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_advance_credit_transaction)

/**
 * [PETRO_CASHBACK_DEBIT], [DEBIT] from [ORACLE]
 */
data class TransactionStateBankTransfer(
  var transaction: TransactionsItemData
) : TransactionState(R.layout.view_bank_transfer_transaction)

/**
 * [RECONCILIATION_DEBIT]
 */
data class TransactionStateReconciliationDebit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_reconciliation_debit_transaction)

/**
 * [PETRO_CASHBACK_DEBIT]
 */
data class TransactionStateFuelCashbackDebit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_fuel_cashback_debit_transaction)

/**
 * [ADVANCE_AUTO_DEBIT]
 */
data class TransactionStateAdvanceAutoDebit(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_advance_auto_debit_transaction)

/**
 * [DEBIT_NOTE]
 */
data class TransactionStateDebitNote(
  var transaction: TransactionsItemData,
  var trip: HomeTripsItemData
) : TransactionState(R.layout.view_debit_note_transaction)

/**
 * Loading
 */
data class TransactionStateLoading(
  val loading: Boolean = true
) : TransactionState(R.layout.view_bid_details_loading_bids)
