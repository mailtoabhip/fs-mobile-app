package com.delhivery.axle.ui.home.fragments.loads

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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
) : BaseViewModel(), BidDetailsCreateEditDialogInterface, BidConfirmReviseDialogInterface {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* route/lane preferene live data */
  var routesLiveData = MutableLiveData<Boolean>()

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()

  /* revise bid live data */
  var reviseBidLiveData = MutableLiveData<Pair<Boolean, Int>>()

  /* loads count live data */
  var loadsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var loadPricePercent = 0

  /* pagination params */
  var hasMoreData = true
  var more_default_loads = false
  var vehicleTypes: String?= null
  var passing_vehicle_type: String?= null
  var filterVehicleType: Boolean?= null
  var offset = 0
  var total = 0
  var hasOrionLoadOnce = false

  /* vehicle_type filter */
  var vehicleStr = userPrefs.truckTypes
  var type = userPrefs.demandType

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
  fun fetchUserTransactions(
    paginate: Boolean = false, express: String?= null,
    isExpress: Boolean = false, infoSearch: Boolean = false, excludeTruckTypes: String?= null) {
    if (!paginate || infoSearch) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    passing_vehicle_type = vehicleStr
    vehicleTypes = passing_vehicle_type
    if (infoSearch) {
      vehicleTypes = null
    }

    dataLoadingLiveData.postValue(true)

    compositeDisposable += transactionsRepository.fetchLoadBoardTransactions(offset, type, vehicleTypes, express, excludeTruckTypes, filterVehicleType)
        .flatMap { t ->
          offset = t.offset
          total = t.total
          hasMoreData = t.offset != t.total
          loadPricePercent = t.loadPricePercent
          more_default_loads = t.more_loads
          loadsCountLiveData.postValue(total)

          Single.zip(
              bidsRepository.bidsForLoads(t.transactions),
              bidsRepository.bulkLowestBidsForLoads(t.transactions),
              BiFunction<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>,
                  Triple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>>> { t1, t2 ->
                Triple(t1.first, t1.second, t2.second)
              })
        }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))

              val loads = _tRes.first
              val bids = _tRes.second

              if (total == 0 && !infoSearch) {
                add(Pair(HomeLoadsWarningItem_NoLoads, Add))
              } else {
                add(Pair(HomeLoadsSearchItem(), AddUpdate))
                add(Pair(HomeLoadsFilterItem(HomeLoadsFilterItemData(isExpress)), AddUpdate))
                for (load in loads.toMutableList()) {
                  try {
                    val lowestBid = _tRes.third.filter { b ->
                      b.transactionId.safeEquals(load.transactionId)
                    }[0]
                    load.lowestBid = lowestBid.minBid
                    load.numBids = lowestBid.numBids
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

                if (!hasMoreData && !hasOrionLoadOnce && more_default_loads) {
                  add(Pair(HomeLoadsInfoItem(), AddUpdate))
                }
                add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
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
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int
  ) {
    compositeDisposable += bidsRepository.createBid(
        isPMT, transactionId, bidAmount, pmtRate, commercialType
    )
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
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int
  ) {
    compositeDisposable += bidsRepository.editBid(
        isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate
    )
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

  override fun reviseBid(position: Int) {
    reviseBidLiveData.postValue(Pair(true, position))
  }

  /**
   * Fetch lowest bid of a particular transaction
   */
  fun fetchLowestBid(transaction: HomeBidsRequestItemData, pos: Int) {
    compositeDisposable += bidsRepository.bulkLowestBidsForLoads(listOf(transaction))
        .onBackground()
        .progress()
        .subscribe { res, error ->
          if (!error && res != null) {
            transaction.lowestBid = res.second[0].minBid
            lowestBidLiveData.postValue(Pair(pos, transaction))
          } else {
            lowestBidLiveData.postValue(Pair(pos, transaction))
          }
        }
  }
}

private const val BidsUpdateDelay = 1L