package com.dfd.delfin.ui.placementdetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.TPSRepository
import com.dfd.delfin.api.request.UpdateVehicleDetailsRequest
import com.dfd.delfin.api.response.FacilityAddressResponse
import com.dfd.delfin.data.home.placements.HomePlacementsItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.errorTPSResponseBody
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class PlacementDetailsViewModel @Inject constructor(
    private val tpsRepository: TPSRepository, val userPrefs: UserPrefs
): BaseViewModel() {

    lateinit var homePlacementsItemData: HomePlacementsItemData
    var addressLiveData = MutableLiveData<FacilityAddressResponse>()

    var updateVehicleDetails= MutableLiveData<Boolean>()

    fun getFacilityAddress() {
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
}