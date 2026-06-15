package com.dfd.delfin.ui.kyc.address

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.address.AddressDetailData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class CommunicationAddressViewModel@Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private  val userRepository: UserRepository,
    private val userPrefs: UserPrefs
) :
BaseViewModel() {

    var flatAddress=""
    var areaAddress=MutableLiveData<String>()
    var pincodeAddress=""
    var cityAddress=""
    var phoneNum =userPrefs.phoneNumber!!
    var documentProofType: String? = null
    var selectedAdapterPos=-1
    var documentProofUrl= mutableListOf<String>()
    var addressType ="alternate"
    var alternateAddressAdded = MutableLiveData<Boolean>()
    var addAddressLiveData = MutableLiveData<Boolean>()
    var deleteAddressLiveData = MutableLiveData<Boolean>()
    var updateAddressLiveData = MutableLiveData<Boolean>()
    var subAddressLiveData = MutableLiveData<Boolean>()
    var captureAddressProof = MutableLiveData<Boolean>()
    var showSubmitedDialog = MutableLiveData<Boolean>()
    var addressLiveData = MutableLiveData<List<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var fetchGstAddressLiveData = MutableLiveData<List<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var fetchAltaddressLiveData = MutableLiveData<List<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var lastSelectedAddressLiveData = MutableLiveData<AddressDataItem>()
    var addressDetailDataList = MutableLiveData<List<AddressDetailData>>()
    var selectedComminicationAddress =""
    var isSameAsGst =false
    var errorText:String? = ""
    var gstAddress= ""
    var alternateAddress = ""


    fun addNewAddress(isDeleted:Boolean) {
        if (!isConnected) return

        var address = flatAddress + "," + areaAddress.value + "," + cityAddress + "-" + pincodeAddress

        documentProofType= when{
                    documentProofType.equals("Visiting Card")->"visiting_card"
                    documentProofType.equals("LR Copy/Loading Note")->"lr_copy"
                    documentProofType.equals("Letter Head")->"letterhead"
                    documentProofType.equals("Udyog Aadhaar Certificate")->"udyog_aadhaar"
                    documentProofType.equals("Shop & Establishment Certificate")->"shop_establishment"
                    documentProofType.equals("Driving Licence")->"driving_licence"
                    documentProofType.equals("RC copy")->"rc_copy"
            else -> null
        }
        addressType= addressType.lowercase()

            compositeDisposable += loadboardRepository.addAddress(
                phoneNum,
                address,
                documentProofType,
                documentProofUrl,
                addressType,
                isDeleted
            )
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        if(isDeleted){
                            deleteAddressLiveData.postValue(true)
                        }else{
                            addAddressLiveData.postValue(true)
                        }
                    } else {
                        error.handle()
                        addAddressLiveData.postValue(false)
                    }
                }

    }


    fun updateCommunicationAddress(selectedAddress : String,isSameAsGst:Boolean){
        compositeDisposable += loadboardRepository.updateCommunicationAddress(
            selectedAddress,
            isSameAsGst,
            userPrefs.phoneNumber!!
        )
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    updateAddressLiveData.postValue(true)
                    subAddressLiveData.postValue(true)
                    userPrefs.isSameAsGst=isSameAsGst
                } else {
                    error.handle()
                    updateAddressLiveData.postValue(false)
                    subAddressLiveData.postValue(false)
                }
            }

    }



    var currentStep = ""

}