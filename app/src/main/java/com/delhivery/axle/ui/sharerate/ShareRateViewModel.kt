 package com.delhivery.axle.ui.sharerate

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings.Secure
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.PriceRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.PriceDetailRequest
import com.delhivery.axle.api.request.UpdatePriceRequest
// Removed DelegationToken import - no longer needed
import com.delhivery.axle.api.response.GetPricingDataResponse
import com.delhivery.axle.api.response.GetSupplierRewardsResponse
import com.delhivery.axle.api.response.TruckResponseArray
// Removed AWSConfig import - no longer needed
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.kyc.pan.AuthenticationUIError
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_VERSION
import com.delhivery.axle.utils.extensions.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.JsonObject
import java.io.File
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
    var documentListLiveData = MutableLiveData<List<com.delhivery.axle.api.response.DocumentFile>>()
    var documentListErrorLiveData = MutableLiveData<String>()
    
    fun loadDocuments(docType: String) {
        // This method can be called from Activity to trigger document loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }

  fun searchOffer(offerId:String) = appDB.offersDao().getOffers(offerId)


}