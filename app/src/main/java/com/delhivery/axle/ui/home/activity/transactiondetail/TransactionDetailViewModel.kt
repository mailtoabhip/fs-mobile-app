package com.delhivery.axle.ui.home.activity.transactiondetail

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.transactions.TransactionChannel
import com.delhivery.axle.data.transactions.TransactionChannel.HPCL
import com.delhivery.axle.data.transactions.TransactionChannel.IOCL
import com.delhivery.axle.data.transactions.TransactionType.ADVANCE_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.CREDIT
import com.delhivery.axle.data.transactions.TransactionType.DEBIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_DEBIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_REFUND_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.RECONCILIATION_DEBIT
import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionState
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateAdvanceCredit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateBankTransfer
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelCashbackCredit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelDebit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelRevertCredit
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TransactionDetailViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  val bank: String get() = userPrefs.bankName

  lateinit var transaction: TransactionsItemData

  var transactionStateLiveData = MutableLiveData<TransactionState>()

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

      CREDIT, ADVANCE_CREDIT, PETRO_REFUND_CREDIT, PETRO_CASHBACK_CREDIT -> {
        if (transaction.tripId.isNotNullOrEmpty()) {
          fetchTrip()
        }
      }

      PETRO_CASHBACK_DEBIT, RECONCILIATION_DEBIT -> {
        transactionStateLiveData.postValue(TransactionStateBankTransfer(transaction))
      }

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
              PETRO_REFUND_CREDIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateFuelRevertCredit(transaction, _res)
                )
              }
              PETRO_CASHBACK_CREDIT -> {
                transactionStateLiveData.postValue(
                    TransactionStateFuelCashbackCredit(transaction, _res)
                )
              }
            }
          } else {

          }
        }
  }

}