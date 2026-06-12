package com.dfd.delfin.ui.home.activity.transactionlist

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.WalletRepository
import com.dfd.delfin.api.response.WalletData
import com.dfd.delfin.api.response.WalletDataResponse
import com.dfd.delfin.api.response.WalletTransactionsResponse
import com.dfd.delfin.data.transactions.TransactionComparator
import com.dfd.delfin.data.transactions.TransactionHeaderItemData
import com.dfd.delfin.data.transactions.TransactionsItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Add
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 * Viewmodel for [TransactionsActivity]
 */
class TransactionsViewModel @Inject constructor(
  private val walletRepository: WalletRepository
) : BaseViewModel() {

  var transactionsLiveData =
    MutableLiveData<List<Pair<BaseTransactionsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /**
   * Fetch wallet transactions
   */
  fun fetchTransactions() {
    compositeDisposable += Single.zip(walletRepository.fetchWalletData(),
        walletRepository.fetchWalletTransactions(),
        BiFunction<WalletDataResponse, WalletTransactionsResponse, Pair<WalletData, List<TransactionsItemData>>> { t1, t2 ->
          Pair(t1.wallet, t2.transactions.sortedWith(TransactionComparator()))
        })
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseTransactionsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(
                  Pair(
                      TransactionHeaderItem(
                          TransactionHeaderItemData(_res.first, _res.second.size)
                      ), AddUpdate
                  )
              )
              if (_res.second.isNullOrEmpty()) {
                add(Pair(TransactionWarningItem_Transaction, Add))
              } else {
                for (transaction in _res.second) {
                  add(Pair(TransactionDataItem(transaction), Add))
                }
              }
            }
                .let {
                  transactionsLiveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseTransactionsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(TransactionWarningItem_TimeOut, Add))
            }
                .let { transactionsLiveData.postValue(it) }
          }
        }
  }
}