package com.delhivery.axle.ui.kyc.address

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.kyc.gst.BaseGstRVAdapterItem
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class CommunicationAddressViewModel@Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private  val userRepository: UserRepository,
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
    var updateAddressLiveData = MutableLiveData<Boolean>()
    var captureAddressProof = MutableLiveData<Boolean>()
    var showSubmitedDialog = MutableLiveData<Boolean>()
    var AddressLiveData = MutableLiveData<List<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var selectedComminicationAddress =""
    var isSameAsGst =false

    //  var addAddressErrorMsgLiveData = MutableLiveData<String>()



    fun addNewAddress() {
        if (!isConnected) return

       var businessAddress = flatAddress +","+areaAddress+","+cityAddress+"-"+pincodeAddress

        mutableListOf<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(AddressDataItem(AddressDetailData(userPrefs.phoneNumber,businessAddress)),
                DataRVAdapterOperationType.Add
            ))
        }.let {
            AddressLiveData.postValue(it)
        }
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

    fun updateCommunicationAddress(){
        compositeDisposable += loadboardRepository.updateCommunicationAddress(
            userPrefs.phoneNumber!!,
            selectedComminicationAddress,
            isSameAsGst
        )
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    updateAddressLiveData.postValue(true)
                } else {
                    updateAddressLiveData.postValue(false)
                    // addAddressErrorMsgLiveData.postValue(_res.toString())
                }
            }

    }




    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    /**
     * Get delegation token for AWS
     */
    fun getDelegationToken(file: File) {
        compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    delegationLiveData.postValue(Pair(_res.delegationToken, file))
                } else
                    error.handle()
            }
    }

}