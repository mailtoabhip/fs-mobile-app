package com.delhivery.axle.ui.kyc.address

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class CommunicationAddressViewModel@Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs
) :
BaseViewModel() {

    var flatAddress=""
    var areaAddress=""
    var pincodeAddress=""
    var cityAddress=""
    var documentProofType = ""
    var documentProofUrl = "s3.amazon.sjdha"
    var addAddressLiveData = MutableLiveData<Boolean>()
  //  var addAddressErrorMsgLiveData = MutableLiveData<String>()



    fun addNewAddress() {
        if (!isConnected) return

       var businessAddress = flatAddress +","+areaAddress+","+cityAddress+"-"+pincodeAddress
            compositeDisposable += loadboardRepository.addAlternateAddress(
                userPrefs.phoneNumber!!,
                businessAddress,
                documentProofType,
                documentProofUrl

            )
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        addAddressLiveData.postValue(true)
                    } else {
                        addAddressLiveData.postValue(false)
                       // addAddressErrorMsgLiveData.postValue(_res.toString())
                    }
                }

    }

}