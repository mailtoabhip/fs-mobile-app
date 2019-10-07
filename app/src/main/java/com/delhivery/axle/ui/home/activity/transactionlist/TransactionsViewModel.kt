package com.delhivery.axle.ui.home.activity.transactionlist

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.WalletData
import com.delhivery.axle.api.response.WalletDataResponse
import com.delhivery.axle.api.response.WalletTransactionsResponse
import com.delhivery.axle.data.transactions.TransactionComparator
import com.delhivery.axle.data.transactions.TransactionHeaderItemData
import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class TransactionsViewModel @Inject constructor(
  private val walletRepository: WalletRepository
) : BaseViewModel() {

  var transactionsLiveData =
    MutableLiveData<List<Pair<BaseTransactionsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

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