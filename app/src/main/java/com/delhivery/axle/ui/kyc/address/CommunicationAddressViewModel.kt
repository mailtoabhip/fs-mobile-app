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
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
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
    var phoneNum =userPrefs.phoneNumber!!
    var documentProofType: String? = null
    var selectedAdapterPos=-1
    var documentProofUrl= mutableListOf<String>()
    var addressType ="alternate"
    var alternateAddressAdded = MutableLiveData<Boolean>()
    var addressSavedChanges = MutableLiveData<Boolean>()
    var addAddressLiveData = MutableLiveData<Boolean>()
    var updateAddressLiveData = MutableLiveData<Boolean>()
    var subAddressLiveData = MutableLiveData<Boolean>()
    var captureAddressProof = MutableLiveData<Boolean>()
    var showSubmitedDialog = MutableLiveData<Boolean>()
    var AddressLiveData = MutableLiveData<List<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var lastSelectedAddressLiveData = MutableLiveData<AddressDataItem>()
    var addressDetailDataList = MutableLiveData<List<AddressDetailData>>()
    var selectedComminicationAddress =""
    var isSameAsGst =false



   fun fetchAndAddUserAddress(){
   if(!userPrefs.getAddressList().isNullOrEmpty()){
       for(i in userPrefs.getAddressList()!!){

           mutableListOf<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
               add(
                   Pair(
                       AddressDataItem(
                           AddressDetailData(userPrefs.phoneNumber,
                               i?.address!!,i.proofDocumentType,i.documentUrls,i.addressType,i.isDeleted)
                       ),
                       DataRVAdapterOperationType.Add
                   )
               )
           }.let {
               AddressLiveData.postValue(it)

           }
           if(i?.addressType!!.startsWith("al",true)){alternateAddressAdded.postValue(true)}
       }
   }
   }



    fun addNewAddress(isDeleted:Boolean) {
        if (!isConnected) return

        var address = flatAddress + "," + areaAddress + "," + cityAddress + "-" + pincodeAddress

        documentProofType= when{
                    documentProofType.equals("Visiting Card")->"visiting_card"
                    documentProofType.equals("LR Copy")->"lr_copy"
                    documentProofType.equals("Letter Head")->"letterhead"
                    documentProofType.equals("Udyog Aadhaar Certificate")->"udhyog_aadhaar"
                    documentProofType.equals("Shop & Establishment Certificate")->"shop_establishment"
            else -> null
        }
        addressType= addressType.toLowerCase()

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
                        addAddressLiveData.postValue(true)
                        if(!isDeleted) {
                            mutableListOf<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                                add(
                                    Pair(
                                        AddressDataItem(AddressDetailData(userPrefs.phoneNumber, address,documentProofType,documentProofUrl,addressType,isDeleted)),
                                        DataRVAdapterOperationType.Add
                                    )
                                )
                            }.let {
                                AddressLiveData.postValue(it)
                                alternateAddressAdded.postValue(true)
                            }
                        }else{
                            mutableListOf<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                                add(
                                    Pair(
                                        AddressDataItem(AddressDetailData(userPrefs.phoneNumber, address,documentProofType,documentProofUrl,addressType,isDeleted)),
                                        DataRVAdapterOperationType.Remove
                                    )
                                )
                            }.let {
                                AddressLiveData.postValue(it)
                                alternateAddressAdded.postValue(false)
                            }

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
                    userPrefs.isSameAsGst=true
                } else {
                    error.handle()
                    updateAddressLiveData.postValue(false)
                    subAddressLiveData.postValue(false)
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

    var currentStep = ""

}