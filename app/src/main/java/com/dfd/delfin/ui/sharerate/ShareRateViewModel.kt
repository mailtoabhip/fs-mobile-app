 package com.dfd.delfin.ui.sharerate

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.PriceRepository
import com.dfd.delfin.api.repository.TruckRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.request.PriceDetailRequest
// Removed DelegationToken import - no longer needed
import com.dfd.delfin.api.response.GetPricingDataResponse
import com.dfd.delfin.api.response.TruckResponseArray
// Removed AWSConfig import - no longer needed
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.*
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [LoadAlertActivity]
 */
class ShareRateViewModel @Inject constructor(
    val truckRepository: TruckRepository,
    private val userPrefs: UserPrefs,
    private val userRepository: UserRepository,
    private val priceRepository: PriceRepository,
    private val appDB: AppDatabase
) : BaseViewModel() {

    var rateUpdatedLiveData = MutableLiveData<Boolean>()
    var errorrateUpdatedLiveData = MutableLiveData<String?>()

    var truckGetLiveData = MutableLiveData<List<TruckResponseArray>>()
    var pricingLiveData = MutableLiveData<GetPricingDataResponse>()

    var selected_truck_type:String? = null
    var selected_vehicle_number:String? = null
    var selected_truck_capacity:String? = null
    var expectedPrice:Double? = 0.0
    var priceUnit = ""
    var origin: CityModel? = null
    var destination: CityModel? = null
    var tripDate:String? = null
    var proofType:String? = null
    var bannerText:String? = ""
  var documentProofUrl= mutableListOf<String>()
  


  fun getPricingData(priceDetailRequest: PriceDetailRequest) {
    compositeDisposable += priceRepository.getPricingData(priceDetailRequest)
      .onBackground()
      .progress()
      .subscribe { _tRes, error ->
        if(!error && _tRes != null){
          pricingLiveData.postValue(_tRes)
        }
        else{
          error.handle()
          pricingLiveData.postValue(null)
        }
      }
  }


    // Removed delegation token logic - direct upload now handled in Activity
    
    // Download functionality
    var documentListLiveData = MutableLiveData<List<com.dfd.delfin.api.response.DocumentFile>>()
    var documentListErrorLiveData = MutableLiveData<String>()
    
    fun loadDocuments(docType: String) {
        // This method can be called from Activity to trigger document loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }

  fun searchOffer(offerId:String) = appDB.offersDao().getOffers(offerId)


}