package com.delhivery.orion.ui.home.fragments.loads

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.repository.TransactionStatus.Requested
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeLoadsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository
) : BaseViewModel() {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var routesLiveData = MutableLiveData<Boolean>()

  var hasMoreData = true
  var offset = 0

  /**
   * Fetch user [Requested] transactions
   */
  fun fetchUserTransactions(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }
    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    compositeDisposable += transactionsRepository.transactions(offset, Requested)
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            offset = _tRes.offset
            hasMoreData = _tRes.offset != _tRes.total

            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))

              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && _tRes.total == 0) {
                add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
              }
              /* post all transactions as add */
              else {
                _tRes.transactions.forEach { _item ->
                  add(Pair(HomeLoadsRequestItem(_item), Add))
                }
              }
            }
                .let { userLoadsData.postValue(it) }
          } else {
            /* remove progress item */
            Pair(HomeLoadsProgressItem(), Remove).let { userLoadsData.postValue(listOf(it)) }
            error.handle()
          }
          showProgress(false)
        }
  }

  /**
   * Checks if user routes/lane preference
   */
  fun checkUserRoutes() {
    compositeDisposable += userRepository.getUser()
        .onBackground()
        .subscribe { _user, error ->
          if (!error) {
            routesLiveData.postValue(_user.hasRoutes())
          } else {
            error.handle()
          }
        }
  }
}