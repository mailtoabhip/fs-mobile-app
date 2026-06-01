package com.delhivery.axle.ui.searchload.fragments.searchresults

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.AcceptAdhocIntracityBidBottomDialogInterface
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidsCreateEditInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [SearchResultsFragment]
 */
class SearchResultsViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val truckRepository: TruckRepository,
    private val tripsRepository: TripsRepository,
    val userPrefs: UserPrefs
) : BaseViewModel(),BidConfirmReviseDialogInterface,AcceptAdhocIntracityBidBottomDialogInterface {

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  /* search results live data */
  var searchResults = MutableLiveData<List<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()
  var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>,HomeBidsRequestItemData>>()
  var acceptBidLiveData = MutableLiveData<Pair<Int,Any>>()

    var editBulkLiveData= MutableLiveData<Pair<Int,String>>()
    var editFlg= mutableListOf<Boolean>(false,false,false)
    var bulkBidActionLiveData = MutableLiveData<Pair<Int,List<TransactionBid>>>()
    var loadPricePercent = 0
    var total =0
    var bidsCount =0

  /* revise bid live data */
  var reviseBidLiveData = MutableLiveData<Pair<Boolean, Int>>()
  var paginateCount=0
  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* pagination params */
  var offset=0
  var hasMoreData=false

  override fun reviseBid(transactionBid: TransactionBid?, position: Int) {
    reviseBidLiveData.postValue(Pair(true, position))
  }

    override fun acceptBid(
        position: Int,
        transactionId: String,
        supplierId: String,
        supplierName: String,
        bidAmount: Int,
        commercialType: String,
        vehicleNumber: String,
        driverPhone: String,
        driverName: String
    ) {
        tripsRepository.acceptTripBid(transactionId,supplierId,supplierName,bidAmount,commercialType,vehicleNumber,driverPhone,driverName)
            .onBackground()
            .subscribe { _res, error ->
                if (!error && _res != null) {
                    acceptBidLiveData.postValue(Pair(position, _res))
                } else {
                    error.handle()
                    acceptBidLiveData.postValue(null)
                }
            }
    }
}



private const val BidsUpdateDelay = 1L