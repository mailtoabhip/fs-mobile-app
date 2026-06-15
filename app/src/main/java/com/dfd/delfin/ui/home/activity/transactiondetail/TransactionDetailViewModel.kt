package com.dfd.delfin.ui.home.activity.transactiondetail

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.FuelRepository
import com.dfd.delfin.api.repository.TripsRepository
import com.dfd.delfin.data.fuelcards.FuelCardData
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.transactions.TransactionChannel
import com.dfd.delfin.data.transactions.TransactionChannel.HPCL
import com.dfd.delfin.data.transactions.TransactionChannel.IOCL
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
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionState
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateAdvanceAutoDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateAdvanceCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateBankTransfer
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelCashbackCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelCashbackDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelRevertCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateReconciliationDebit
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 * View model for [TransactionDetailActivity]
 */
class TransactionDetailViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val fuelRepository: FuelRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  val bank: String get() = userPrefs.bankName

  lateinit var transaction: TransactionsItemData

  var transactionStateLiveData = MutableLiveData<TransactionState>()

  /**
   * Updates transaction details or fetch required trip details
   */
  fun updateDetails() {
    when (transaction.transactionType()) {
      DEBIT -> {
        when (TransactionChannel.byType(transaction.channel ?: "")) {
          IOCL, HPCL -> {
            fetchTrip()
          }
          else -> {
            transactionStateLiveData.postValue(TransactionStateBankTransfer(transaction))
          }
        }
      }

      CREDIT, ADVANCE_CREDIT, PETRO_CASHBACK_CREDIT, RECONCILIATION_DEBIT,
      PETRO_CASHBACK_DEBIT, ADVANCE_AUTO_DEBIT, DEBIT_NOTE -> {
        if (transaction.tripId.isNotNullOrEmpty()) {
          fetchTrip()
        }
      }

      PETRO_REFUND_CREDIT -> {
        if (transaction.tripId.isNotNullOrEmpty() && transaction.fromAccNumber.isNotNullOrEmpty()) {
          fetchTripAndFuelCard()
        }
      }
      else ->{}
    }
  }

  private fun fetchTrip() {
    compositeDisposable += tripsRepository.tripDetails(transaction.tripId ?: "")
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            when (transaction.transactionType()) {
              DEBIT -> {
                transactionStateLiveData.postValue(TransactionStateFuelDebit(transaction, _res))
              }
              ADVANCE_CREDIT, CREDIT -> {
                transactionStateLiveData.postValue(TransactionStateAdvanceCredit(transaction, _res))
              }
              PETRO_CASHBACK_CREDIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateFuelCashbackCredit(transaction, _res)
                )
              }
              RECONCILIATION_DEBIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateReconciliationDebit(transaction, _res)
                )
              }
              PETRO_CASHBACK_DEBIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateFuelCashbackDebit(transaction, _res)
                )
              }
              ADVANCE_AUTO_DEBIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateAdvanceAutoDebit(transaction, _res)
                )
              }
              DEBIT_NOTE -> {
                transactionStateLiveData.postValue(
                    TransactionStateAdvanceAutoDebit(transaction, _res)
                )
              }
              else -> {
              }
            }
          } else {
            //Do Nothing
          }
        }
  }

  private fun fetchTripAndFuelCard() {
    compositeDisposable += Single.zip(
        tripsRepository.tripDetails(transaction.tripId ?: ""),
        fuelRepository.fetchFuelCard(transaction.tripId ?: "", transaction.fromAccNumber ?: ""),
        BiFunction<HomeTripsItemData, FuelCardData, Pair<HomeTripsItemData, FuelCardData>> { t1, t2 ->
          Pair(t1, t2)
        })
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            if (transaction.transactionType() == PETRO_REFUND_CREDIT) {
              transactionStateLiveData.postValue(
                  TransactionStateFuelRevertCredit(transaction, _res)
              )
            }
          } else {

          }
        }
  }

}