package com.delhivery.axle.ui.kyc.gst

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.AddAddressModel
import com.delhivery.axle.api.request.ResetKycDataRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstDetailItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.kyc.address.AddressDataItem
import com.delhivery.axle.ui.kyc.address.BaseAddressRVAdapterItem
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.File
import javax.inject.Inject

class GstVerificationViewModel@Inject constructor(
        private  val loadboardRepository: LoadboardRepository,
        private  val userRepository: UserRepository,
        private val userPrefs: UserPrefs
) : BaseViewModel() {

    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()

    var gstDetailData = MutableLiveData<GstDetailItemData>()
    var docVerificationFailedCount = MutableLiveData<Int>().apply { postValue(0) }

    var otpRecieved = MutableLiveData<Boolean>()

    var otpVerified = MutableLiveData<Boolean>()
    var docVerified = MutableLiveData<Boolean>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<com.delhivery.axle.ui.kyc.gst.AuthenticationUIError, String?>>()

    /* steps */
    var currentStep = ""

    var errorText:String? = ""

    var gstNumbersLiveData = MutableLiveData<List<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var gstDetailsLiveData = MutableLiveData<GstDetailData>()

    var phoneNum = userPrefs.phoneNumber
    var addressType ="gst"

    var gstFetchList = HashSet<String>()

    var resetKycLiveData = MutableLiveData<Boolean>()
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

    fun verifyRequestOtp(otp: CharArray) {
        if (!isConnected) return
        val _otp = otp.joinToString("")
        if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null) {
            compositeDisposable += loadboardRepository.verifyGstOrAadhaarOtp("gst", gstDetailData.value?.gstNumber!!.replace("-", ""), _otp)
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            otpVerified.postValue(true)
                            userPrefs.isGstNotBypassed=true

                        } else {
                            error.handle()
                            otpVerified.postValue(false)
                        }
                    }
        }else{
            errorLiveData.postValue(Pair(com.delhivery.axle.ui.kyc.gst.AuthenticationUIError.InvalidGSTNumber, "Invalid GST Number"))
        }
    }

    fun getRequestOtp(launchDialog:Boolean) {
        if (!isConnected) return
        if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null) {
            compositeDisposable += loadboardRepository.getGstOrAadhaarOtp("gst", gstDetailData.value?.gstNumber!!.replace("-", ""))
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            if (launchDialog) {
                                otpRecieved.postValue(true)
                            }
                        } else {
                            error.handle()
                            if (launchDialog) {
                                otpRecieved.postValue(false)
                            }


                        }
                    }
        }else{
            errorLiveData.postValue(Pair(com.delhivery.axle.ui.kyc.gst.AuthenticationUIError.InvalidGSTNumber, "Invalid GST Number"))
        }
    }

    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return
        if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null) {
            compositeDisposable += loadboardRepository.verifyByDocUpload("gst", "06AAPCS957SE1Z4", docList)
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            if(_res.isVerified == true){
                                docVerified.postValue(true)
                                userPrefs.isGstNotBypassed=true
                            }else{
                                docVerified.postValue(false)
                            }
                        } else {
                            error.handle()
                            docVerified.postValue(false)
                        }
                    }
        }else{
            errorLiveData.postValue(Pair(com.delhivery.axle.ui.kyc.gst.AuthenticationUIError.InvalidGSTNumber, "Invalid GST Number"))
        }

    }

    /**
     * update user gst number and address
     */
    var userUpdateLiveData = MutableLiveData<Boolean>()

    fun updateUserDetails() {
        if (!isConnected) return

        if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null && phoneNum!=null) {
            compositeDisposable +=  loadboardRepository.addAddress(phoneNum, gstDetailData.value?.address, null, null, addressType, false)
                    .flatMap { _Res-> loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = phoneNum!!,gstNumber = gstDetailData.value?.gstNumber!!.replace("-", "")))
                                .map {
                                    val msg = if (_Res.isNotNullOrEmpty()) {
                                       _Res
                                    } else {
                                        "Error updating user"
                                    }
                                    Triple(_Res, msg, it)
                                }
                    }
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error && !_res.second.equals("Error updating user")) {
                                userPrefs.gstNumber = gstDetailData.value?.gstNumber!!.replace("-", "")
                                val addressList = ArrayList<AddAddressModel>()
                                addressList.add(AddAddressModel(phoneNumber = phoneNum,addressType= "gst", address = gstDetailData.value?.address, isDeleted = false, proofDocumentType = null, documentUrls = null))
                                if(!userPrefs.getAddressList().isNullOrEmpty()){
                                    if(userPrefs.getAddressList()!!.size>1){
                                       addressList.add(userPrefs.getAddressList()!!.get(1) as AddAddressModel)
                                    }
                                }
                                userPrefs.setAddressList(addressList)
                                userPrefs.isGstVerfied = true
                                userUpdateLiveData.postValue(true)
                        }else {
                            userUpdateLiveData.postValue(false)
                        }
                    }
        }else{
            errorLiveData.postValue(Pair(com.delhivery.axle.ui.kyc.gst.AuthenticationUIError.InvalidGSTNumber, "Invalid GST Number"))
        }

    }

  fun resetKycDetails(ignoreClearDate:Boolean) {
    if (!isConnected) return

    if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null && phoneNum!=null) {
      compositeDisposable += loadboardRepository.resetKyc(ResetKycDataRequest(phoneNumber = userPrefs.phoneNumber!!, resetPoint = "identity", gstNumber = gstDetailData.value?.gstNumber, isAddressSameAsGST = userPrefs.isSameAsGst))
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            if(!ignoreClearDate){
              userPrefs.clearKycPreferenceDataBasedOnSteps("gst")
            }
            resetKycLiveData.postValue(true)
          } else{
            error.handle()
            resetKycLiveData.postValue(false)
          }
        }
    }

  }

    fun fetchGstNumbers() {
        compositeDisposable += loadboardRepository.gstNumbers(userPrefs.pancard)
                .onBackground()
                .subscribe { _res, error ->
                    mutableListOf<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(GstProgressItem(), DataRVAdapterOperationType.Remove))

                        if (!error) {
                            if (_res.getGstList(_res.gstin_numbers).isNullOrEmpty()) {
                                add(Pair(GstItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                            } else {
                              for (warehouse in _res.getGstList(_res.gstin_numbers)) {
                                    add(Pair(GstDataItem(warehouse), DataRVAdapterOperationType.Add))
                                }
                            }
                        } else {
                            /* add api time out item */
                            add(Pair(GstItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                        }
                    }
                            .let { gstNumbersLiveData.postValue(it) }
                }
    }

    /**
     * Fetch gst details
     */
    fun fetchDetails(data: GstDetailData) {
        if (!gstFetchList.contains(data.gstNumber)) {
            gstFetchList.add(data.gstNumber)
            compositeDisposable += loadboardRepository.gstDetails(data.gstNumber)
                    .onBackground()
                    .subscribe { _res, error ->
                        if (!error) {
                            data.gstDetailItemData = _res
                        } else {
                            data.gstDetailItemData = GstDetailItemData(data.gstNumber, "NA", "NA", false)
                        }
                        gstDetailsLiveData.postValue(data)
                    }
        }
    }
}

/**
 * Authentication UI Error
 */
enum class AuthenticationUIError {
    None,
    InvalidGSTNumber
}