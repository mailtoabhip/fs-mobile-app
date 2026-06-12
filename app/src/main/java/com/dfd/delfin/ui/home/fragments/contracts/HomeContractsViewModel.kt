package com.dfd.delfin.ui.home.fragments.contracts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.TransactionStatus.Requested
import com.dfd.delfin.api.repository.TransactionsRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.response.TruckResponseArray
import com.dfd.delfin.data.bids.TransactionBid
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance

import javax.inject.Inject

class HomeContractsViewModel@Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var userLoadsDataFetch =
    MutableLiveData<List<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var bulkBidActionLiveData = MutableLiveData<Pair<Int,List<TransactionBid>>>()

  /* loads count live data */
  var contractsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>, HomeBidsRequestItemData>>()

  var loadPricePercent = 0

  /* pagination params */
  var hasMoreData = true
  var fecthToCalled = true
  var more_default_loads = false
  var vehicleTypes: String?= null
  var passing_vehicle_type: String?= null
  var filterVehicleType: Boolean?= null
  var offset = 0
  var total = 0
  var allActiveFetched = false
  var transactionIds:String?=null

  var hasOrionLoadOnce = false

  /* vehicle_type filter */
  var vehicleStr = userPrefs.truckTypes
  //var type = userPrefs.demandType

  var paginateCount =0

  var txnIds:ArrayList<String> = ArrayList()
  var searchAfterCreationTime :String? = null
  var searchAfterTransactionId : String? = null

  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

  /**
   * Fetch user [Requested] transactions
   */
  fun fetchUserTransactions(
    paginate: Boolean = false, demandType: String,contractType: String?=null,isFlexible:Boolean?=null,includeFlexibleContracts:Boolean?=null){
    if (!paginate ) {
      searchAfterTransactionId = null
      searchAfterCreationTime = null
      offset = 0
      allActiveFetched = false
    } else if (paginate && !hasMoreData) {
      return
    }

    if (!paginate) {
      txnIds.clear()
    }

    if (paginate) {
      paginateCount += 1
      Pair(HomeContractsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    passing_vehicle_type = vehicleStr
    vehicleTypes = passing_vehicle_type


    dataLoadingLiveData.postValue(true)
    val mainTrace = Firebase.performance.newTrace("fetch_contract_transactions")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_for_contract_transactions_parallel")
    mainTrace.start()
    Log.d("cntrctVM", "srchaftr $searchAfterCreationTime searctTrans $searchAfterTransactionId")
  }

}



private const val BidsUpdateDelay = 1L