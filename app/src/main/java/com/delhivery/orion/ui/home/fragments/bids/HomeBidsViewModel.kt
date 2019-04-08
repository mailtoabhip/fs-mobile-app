package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.UserModel
import com.delhivery.orion.data.home.HomeBidsHeaderItemData
import com.delhivery.orion.data.home.HomeBidsSearchItemData
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionStatus.Requested
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class HomeBidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

  /* static live data */
  var staticData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var hasMoreData = true
  var offset = 0

  /**
   * Fetch user static data
   */
  fun fetchStaticData() {
    compositeDisposable += Single.zip(
        userRepository.getUser(), bidsRepository.userBidsCount(),
        BiFunction<UserModel, Pair<Int, Int>, Triple<UserModel, Int, Int>> { t1, t2 ->
          Triple(t1, t2.first, t2.second)
        })
        .onBackground()
        .doOnSuccess { showProgress() }
        .subscribe { _data, error ->
          if (!error) {
            _data.apply {
              val _items =
                mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>()

              /* add header counts */
              _items.add(Pair(HomeBidsHeaderItem(HomeBidsHeaderItemData(second, third)), Update))

              /* show no routes warning item when no routes found */
              if (first.userRoutes().isEmpty()) {
                _items.add(Pair(HomeBidsWarningItem_SelectRoutes, AddUpdate))
                showProgress(false)
              }
              /* start fetching transactions */
              else {
                fetchUserTransactions(false)
              }
              /* post to static data */
              staticData.postValue(_items)
            }
          } else {
            error.printStackTrace()
            showProgress(false)
          }
        }
  }

  /**
   * Fetch user transactions
   */
  fun fetchUserTransactions(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }
    compositeDisposable += transactionsRepository.transactions(offset, Requested)
        .onBackground()
        .progress()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            offset = _tRes.offset
            hasMoreData = _tRes.offset != _tRes.total

            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && _tRes.total == 0) {
                add(Pair(HomeBidsWarningItem_EditRoutePrefs, AddUpdate))
              }
              /* post all transactions as add */
              else {
                add(Pair(HomeBidsSearchItem(HomeBidsSearchItemData(_tRes.total)), Update))
                _tRes.transactions.forEach { _item ->
                  add(Pair(HomeBidsRequestItem(_item), Add))
                }
              }
            }
                .let { staticData.postValue(it) }
          } else {
            error.printStackTrace()
          }
        }
  }
}