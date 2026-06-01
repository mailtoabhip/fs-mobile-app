package com.delhivery.axle.ui.contractDetails

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TPSRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.api.response.FacilityAddressResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_LoadingBids
import com.delhivery.axle.utils.JsonUtils
import com.delhivery.axle.utils.extensions.errorTPSResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import javax.inject.Inject

class ContractDetailsViewModel @Inject constructor(private val transactionsRepository: TransactionsRepository,
                                                    private val tpsRepository: TPSRepository, val userPrefs: UserPrefs
): BaseViewModel(){

  companion object {
    // Cache TypeToken instance as static field to prevent ProGuard/R8 obfuscation issues
    private val HOME_BIDS_REQUEST_ITEM_DATA_TYPE = object : TypeToken<HomeBidsRequestItemData>() {}.type
  }

  /* transaction id */
  //lateinit var transactionId: String

  /* transaction id */
  var transactionId: String?=null
  //contract code
  var contractCode: String?=null
  //placement type for placements details page
  var placementType: String?=null

  var propertyAddressData : FacilityAddressResponse? = null

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
    compositeDisposable += transactionsRepository.transactionDetails(transactionId?:"", if(requestType==RequestType.Contract.type) userPrefs.userId()else null)
      .onBackground()
      .progress()
      .subscribe { _tRes, error ->
        if (!error) {
          transaction = _tRes
          transactionLiveData.postValue(_tRes)
          //fetchTransactionBids()
        } else {
          transactionLiveData.postValue(null)
        }
      }
  }



  /**
   * Fetch placement details
   */
  fun fetchPlacementDetails() {
    Log.d("PlacementDetails", "Fetching placement details with params: placementType='$placementType', transactionId='$transactionId', contractCode='$contractCode'")

    compositeDisposable += tpsRepository.getPlacementDetails(placementType = placementType?:"", transactionId = transactionId, contractCode = contractCode)
      .onBackground()
      .progress()
      .subscribe({ _tRes ->
        Log.d("PlacementDetails", "_tRes received: ${_tRes?.toString() ?: "null"}")
        Log.d("PlacementDetails", "_tRes type: ${_tRes?.javaClass?.simpleName ?: "null"}")

        if (_tRes != null) {
          Log.d("PlacementDetails", "Successfully received placement details")
          _tRes.loadType = placementType
          transaction = _tRes
          transactionLiveData.postValue(_tRes)
          //fetching bids data - don't uncomment
          //moved this call to refreshData function
          //No need of this api call anymore
          //fetchTransactionBids()
        } else {
          Log.e("PlacementDetails", "Response is null - this indicates an issue with the API response parsing")
          transactionLiveData.postValue(null)
        }
      }, { error ->
        Log.e("PlacementDetails", "Error occurred: ${error?.toString() ?: "null"}")
        Log.e("PlacementDetails", "Error type: ${error?.javaClass?.simpleName ?: "null"}")
        error?.printStackTrace()
        transactionLiveData.postValue(null)
      })
  }

  fun fetchPlacementDetailsLocal(mContext: Context) {
    Log.d("PlacementDetails", "Fetching placement details with params: placementType='$placementType', transactionId='$transactionId', contractCode='$contractCode'")


    val jsonString = JsonUtils.readJsonFromRaw(mContext, R.raw.placements_details_response)
    val gson = Gson()
    val mockResponse: HomeBidsRequestItemData = gson.fromJson(jsonString, HOME_BIDS_REQUEST_ITEM_DATA_TYPE)
    Log.d("HomeBidsRequestItemData====>>>>", ""+mockResponse)


    // TODO -
    // Remove this code snippet
    // read from local json file
    //val jsonString = JsonUtils.readJsonFromRaw(mContext, R.raw.placements_details_response) as HomeBidsRequestItemData
    //Log.d("HomeBidsRequestItemData::jsonString====>>>>", Gson().toJson(jsonString))
    //Log.d("HomeBidsRequestItemData::jsonString::Gson====>>>>", Gson().toJson(jsonString))
    //var res = Gson().fromJson(jsonString, HomeBidsRequestItemData::class.java)
    //Log.d("HomeBidsRequestItemData====>>>>", ""+res)



    if (mockResponse != null) {
      Log.d("PlacementDetails", "Successfully received placement details")
      mockResponse.loadType = placementType
      transaction = mockResponse
      transactionLiveData.postValue(mockResponse)
      //fetching bids data - don't uncomment
      //moved this call to refreshData function
      //No need ot this api call anymore
      //fetchTransactionBids()
    } else {
      Log.e("PlacementDetails", "Response is null - this indicates an issue with the API response parsing")
      transactionLiveData.postValue(null)
    }
  }



  /* Fetch transaction bids and update UI as per response
  */

  var addressLiveData = MutableLiveData<FacilityAddressResponse>()

  var updateVehicleDetails= MutableLiveData<Boolean>()

  fun getFacilityAddress(originCenterCode:String?) {
    if(originCenterCode!=null)
      compositeDisposable += tpsRepository.getFacilityAddress(originCenterCode)
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            Log.i("Address", _tRes.toString())
            addressLiveData.postValue(_tRes)
          }else{ error.handle()
          }
        }
  }

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