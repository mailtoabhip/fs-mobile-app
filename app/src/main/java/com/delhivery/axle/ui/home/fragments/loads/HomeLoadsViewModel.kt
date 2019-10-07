package com.delhivery.axle.ui.home.fragments.loads

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.repository.BidsRepository
import com.delhivery.axle.repository.TransactionStatus.Requested
import com.delhivery.axle.repository.TransactionsRepository
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
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
  private val bidsRepository: BidsRepository,
  val userPrefs: UserPrefs
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

  var loadPricePercent = 0

  /* pagination params */
  var hasMoreData = true
  var offset = 0
  var total = 0

  /**
   * Getter/Setter for route update flag to preferences
   */
  var routeUpdated: Boolean
    get() = userPrefs.routeUpdate
    set(value) {
      userPrefs.routeUpdate = value
    }

  /**
   * Check FCM registration flag
   */
  fun isFCMTokenGenerated() = userPrefs.fcmTokenGenerated

  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

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

    compositeDisposable += transactionsRepository.fetchLoadBoardTransactions(offset)
        .flatMap { t ->
          offset = t.offset
          total = t.total
          hasMoreData = t.offset != t.total
          loadPricePercent = t.loadPricePercent
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
                add(Pair(HomeLoadsSearchItem(), AddUpdate))
                for (load in loads.toMutableList()) {
                  try {
                    load.loadPricePercent = loadPricePercent
                    load.transactionBid =
                      bids.filter { b ->
                        b.transactionId.safeEquals(load.transactionId)
                      }[0]
                  } catch (e: Exception) {
                    Log.d("No Bid found for: ", load.transactionId ?: "")
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
              /* add api time out item */
              add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
            }
                .let { userLoadsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * Checks if user has added routes/lane preference
   */
  fun checkUserRoutes() {
    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .subscribe { _user, error ->
          if (!error) {
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

  /**
   * Update app user access status
   */
  fun updateUserAppAccess() {
    compositeDisposable += userRepository.updateUserAppAccess()
        .onBackground()
        .subscribe { _, _ -> }
  }

  /**
   * Update FCM token
   */
  fun updateFCMToken(fcmToken: String) {
    compositeDisposable += userRepository.updateFCMToken(fcmToken)
        .onBackground()
        .subscribe { _, error ->
          if (!error) {
            userPrefs.fcmTokenGenerated = false
          }
        }
  }
}

private const val BidsUpdateDelay = 1L