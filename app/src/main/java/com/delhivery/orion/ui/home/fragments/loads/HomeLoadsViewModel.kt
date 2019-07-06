package com.delhivery.orion.ui.home.fragments.loads

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionStatus.Requested
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import com.delhivery.orion.utils.extensions.safeEquals
import com.delhivery.orion.utils.prefs.UserPrefs
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [HomeLoadsFragment]
 *
 **
 */
class HomeLoadsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs,
  private val bidsRepository: BidsRepository
) : BaseViewModel(), BidDetailsCreateEditDialogInterface {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* route/lane preferene live data */
  var routesLiveData = MutableLiveData<Boolean>()

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  /* loads count live data */
  var loadsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var hasMoreData = true
  var offset = 0
  var total = 0

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
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)

    compositeDisposable += transactionsRepository.transactions(offset)
        .flatMap { t ->
          offset = t.offset
          total = t.total
          hasMoreData = t.offset != t.total
          loadsCountLiveData.postValue(total)
          bidsRepository.bidsForLoads(t.transactions)
        }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))

              val loads = _tRes.first
              val bids = _tRes.second

              if (total == 0) {
                add(Pair(HomeLoadsWarningItem_NoLoads, Add))
              } else {
                for (load in loads.toMutableList()) {
                  try {
                    load.transactionBid =
                      bids.filter { b ->
                        b.transactionId.safeEquals(load.transactionId)
                      }
                          .get(0)
                  } catch (e: Exception) {
                    Log.d("No Bid found for: ", load.transactionId)
                  }
                  add(Pair(HomeLoadsRequestItem(load), Add))
                }

                if (!hasMoreData) {
                  add(Pair(HomeLoadsInfoItem(), Add))
                }
              }
            }
                .let { userLoadsData.postValue(it) }
          } else {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))
              /* TODO add refresh list item */
            }
                .let { userLoadsData.postValue(it) }
            /* remove progress item */
            error.handle()
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * Checks if user has added routes/lane preference
   */
  fun checkUserRoutes() {
    compositeDisposable += userRepository.getUser()
        .onBackground()
        .subscribe { _user, error ->
          if (!error) {
            userPrefs.baseCityCode = _user.baseCityCode
            routesLiveData.postValue(_user.hasRoutes())
          } else {
            error.handle()
          }
        }
  }

  override fun createBid(
    transactionId: String,
    bidAmount: Int,
    position: Int
  ) {
    compositeDisposable += bidsRepository.createBid(transactionId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }

  override fun editBid(
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    position: Int
  ) {
    compositeDisposable += bidsRepository.editBid(transactionId, bidId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }
}

private const val BidsUpdateDelay = 1L