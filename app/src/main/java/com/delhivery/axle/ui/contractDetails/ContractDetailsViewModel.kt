package com.delhivery.axle.ui.contractDetails

import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TPSRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserTrucksLoadLimit
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.api.response.FacilityAddressResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.BidDetailsContractCancelled
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_BulkLoad_Edit
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_CancelledBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_ContractResult
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_EditBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_LoadingBids
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBidFirst
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_RejectedBid
import com.delhivery.axle.ui.home.fragments.trucks.BaseHomeTrucksRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksRequestItem
import com.delhivery.axle.utils.extensions.errorTPSResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

class ContractDetailsViewModel @Inject constructor(private val transactionsRepository: TransactionsRepository,
                                                   private val bidsRepository: BidsRepository, private val tpsRepository: TPSRepository, val userPrefs: UserPrefs
): BaseViewModel(){



  /* transaction id */
  lateinit var transactionId: String

  /* transaction id */
  lateinit var requestType: String

  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()

  var bidPriceLiveData = MutableLiveData<TransactionBid>()

  var showSuccessPlaceReviseDialogLiveData = MutableLiveData<Triple<Pair<String,String>,String?,Pair<Boolean,Boolean>>>()
  var errorBiddingLiveData =MutableLiveData<Boolean>()
  var successBidLiveData =MutableLiveData<Pair<Boolean,Boolean>>()

  var hideProgress = MutableLiveData<Boolean>()
  lateinit var transaction: HomeBidsRequestItemData

  var bidCount =0
  var lowestBid:Double?=0.0
  var acceptedBid:Double?=0.0

  /**
   * Fetch transaction details
   */
  fun fetchTransactionDetails() {
    compositeDisposable += transactionsRepository.transactionDetails(transactionId, if(requestType==RequestType.Contract.type) userPrefs.userId()else null)
      .onBackground()
      .progress()
      .subscribe { _tRes, error ->
        if (!error) {
          transaction = _tRes
          transactionLiveData.postValue(_tRes)
          fetchTransactionBids()
        } else {
          transactionLiveData.postValue(null)
        }
      }
  }




  /* Fetch transaction bids and update UI as per response
  */
  fun fetchTransactionBids( action: Boolean = false) {
    compositeDisposable += bidsRepository.transactionBids(transactionId,true)
      .onBackground()
      .subscribe { _bRes, error ->
        if (!error) {
          bidCount=_bRes.third
          lowestBid=_bRes.first.second.first?.bidAmount
          acceptedBid = _bRes.first.second.second?.bidAmount
          //determine bid state and post to live data
          when {
             transaction.transactionStatus=="cancelled"->{
               transactionBidLiveData.postValue(
                 BidDetailsContractCancelled()
               )
             }
            transaction.transactionStatus=="allocated"->{
              transactionBidLiveData.postValue(
                BidDetailsUserBidState_ContractResult(
                  _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                )
              )
            }
            _bRes.third == 0 -> {
              transactionBidLiveData.postValue(
                BidDetailsUserBidState_PlaceBidFirst()
              )
              bidPriceLiveData.postValue(null)
            }
            _bRes.first.first == null -> {
              transactionBidLiveData.postValue(
                BidDetailsUserBidState_PlaceBid(
                  _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                )
              )
              bidPriceLiveData.postValue(null)
            }
            else -> {
              when (_bRes.first.first!!.status()) {
                Accepted,Rejected,Cancelled -> {
                  transactionBidLiveData.postValue(
                    BidDetailsUserBidState_ContractResult(
                      _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                    )
                  )
                  bidPriceLiveData.postValue(null)
                }
                else -> {
                 if(!transaction.isContractBiddingOpen()){
                    transactionBidLiveData.postValue(
                      BidDetailsUserBidState_ContractResult(
                        _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                      )
                    )
                    bidPriceLiveData.postValue(null)
                  }else{
                    transactionBidLiveData.postValue(
                      BidDetailsUserBidState_EditBid(
                        _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                      )
                    )
                    bidPriceLiveData.postValue(null)
                  }

                }
              }
            }
          }
        } else {
          error.handle()
        }
      }


  }

  fun createBid(
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int,
    tentativeTripCount:Int?,
    vehicleNumber:String?,
    placementDays:String?
  ) {
    compositeDisposable += bidsRepository.createBid(
      isPMT, transactionId, bidAmount, pmtRate, commercialType,null,null,tentativeTripCount,vehicleNumber,placementDays
    )
      .delay(BidsUpdateDelay, SECONDS)
      .onBackground()
      .subscribe { _res, error ->
        if (!error && _res.isSuccess) {
          successBidLiveData.postValue(Pair(true,false))
         // showSuccessPlaceReviseDialogLiveData.postValue(Triple(Pair(bidAmount.toString(),pmtRate.toString()),if(transaction.isItIntraCityContract())vehicleNumber?.toString() else tentativeTripCount?.toString(),Pair(isPMT,false)))
        } else {
          fetchTransactionBids()
          errorBiddingLiveData.postValue(true)
          error.handle()
        }
      }

  }

   fun editBid(
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int,
    tentativeTripCount:Int?,
    vehicleNumber:String?,
    placementDays:String?
  ) {
    compositeDisposable += bidsRepository.editBid(
      isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate,null,null,tentativeTripCount,vehicleNumber,placementDays
    )
      .delay(BidsUpdateDelay, SECONDS)
      .onBackground()
      .subscribe { _res, error ->
        if (!error && _res.isSuccess) {
          successBidLiveData.postValue(Pair(false,true))
        //  showSuccessPlaceReviseDialogLiveData.postValue(Triple(Pair(bidAmount.toString(),pmtRate.toString()),if(transaction.isItIntraCityContract())vehicleNumber?.toString() else tentativeTripCount?.toString(),Pair(isPMT,true)))
        } else {
          fetchTransactionBids()
          errorBiddingLiveData.postValue(true)
          error.handle()
        }
      }

  }

  var addressLiveData = MutableLiveData<FacilityAddressResponse>()

  var updateVehicleDetails= MutableLiveData<Boolean>()

 /* fun getFacilityAddress() {
    if(homePlacementsItemData.originCenterCode!=null)
      compositeDisposable += tpsRepository.getFacilityAddress(homePlacementsItemData.originCenterCode!!)
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            Log.i("Address", _tRes.toString())
            addressLiveData.postValue(_tRes)
          }else{ error.handle()
          }
        }
  }*/


  fun updateVehicleDetails(updateVehicleDetailsRequest: UpdateVehicleDetailsRequest) {
    compositeDisposable += tpsRepository.updateVehicleDetails(updateVehicleDetailsRequest)
      .onBackground()
      .subscribe { _tRes, error ->
        if (!error && _tRes != null) {

          updateVehicleDetails.postValue(true)
        }else{
          updateVehicleDetails.postValue(false)
          val errorBody = error.errorTPSResponseBody()
            ?.messageBody
          if (errorBody != null) {
            Throwable(errorBody.toString()).handle()
          } else {
            error?.handle()
          }
        }
      }
  }

  /*fun getAllInventories(searchText:String){

    val jsonObject = JsonObject()
    jsonObject.addProperty("supplier_id", userPrefs.parentId)

    jsonObject.addProperty("offset", 0)
    jsonObject.addProperty("limit", UserTrucksLoadLimit)
    jsonObject.addProperty("vehicle_prefix",searchText)

    compositeDisposable += inventoryRepository.getInventories(jsonObject)
      .onBackground()
      .subscribe{ _res, error ->
        if(!error && _res != null) {
          Log.i("inventories", _res.toString())
          val trucksList :List<HomeTrucksRequestItemData> = _res.trucks

          mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            if(trucksList != null && trucksList.isNotEmpty()) {
              for (trucks in trucksList) {
                add(Pair(HomeTrucksRequestItem(trucks), DataRVAdapterOperationType.AddUpdate))
              }
            }
          }.let {
            // userTrucksData.postValue(it)
          }
        }
      }

  }*/
  /**
   * Emit bids fetching progress
   */
  private fun <T> Single<T>.bidsProgress() = doOnSubscribe {
    if (transactionBidLiveData.value !is BidDetailsUserBidState_LoadingBids)
      transactionBidLiveData.postValue(BidDetailsUserBidState_LoadingBids())
  }

}

private const val BidsUpdateDelay = 1L