package com.delhivery.axle.ui.kyc.gst

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstDetailItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
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

    var otpRecieved = MutableLiveData<Boolean>()

    var otpVerified = MutableLiveData<Boolean>()
    var docVerified = MutableLiveData<Boolean>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<com.delhivery.axle.ui.kyc.gst.AuthenticationUIError, String?>>()

    /* steps */
    var currentStep = ""

    var gstNumbersLiveData =
            MutableLiveData<List<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var gstDetailsLiveData = MutableLiveData<GstDetailData>()

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

    /**
     * Get GST numbers and details
     */
    /* fun getGstDetails(){
         compositeDisposable += loadboardRepository.gstNumbers(userPrefs.pancard).flatMap{ t->
            loadboardRepository.gstDetails(t.gstin_numbers)
         }
                 .onBackground()
                 .subscribe { _res, error ->
                     if (!error) {
                         mutableListOf<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                             /* remove progress item */
                             add(Pair(GstProgressItem(), DataRVAdapterOperationType.Remove))

                             if (_res.gst_number.isNullOrEmpty()) {
                                 add(Pair(GstWarningItem_Transaction, DataRVAdapterOperationType.Add))
                             } else {
                                 for (gstDetail in _res.gst_number) {
                                     add(Pair(GstDataItem(_res), DataRVAdapterOperationType.Add))
                                 }
                             }
                         }.let {
                             gstLiveData.postValue(it)
                         }
                     } else {
                         mutableListOf<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {

                             /* remove progress item */
                             add(Pair(GstProgressItem(), DataRVAdapterOperationType.Remove))
                             /* remove search item */
                             add(Pair(GstDataItem(), DataRVAdapterOperationType.Remove))

                             add(Pair(GstItem_TimeOut, DataRVAdapterOperationType.Add))
                         }.let {
                             gstLiveData.postValue(it)
                         }
                     }
                 }
     }*/

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
            compositeDisposable += loadboardRepository.verifyByDocUpload("gst", gstDetailData.value?.gstNumber!!.replace("-", ""), docList)
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            docVerified.postValue(true)
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
     * update user pan number
     */
    var userUpdateLiveData = MutableLiveData<Boolean>()

    fun updateUserDetails() {
        if (!isConnected) return

        if(gstDetailData.value!=null && gstDetailData.value?.gstNumber!=null) {
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = "+91"+userPrefs.phoneNumber,gst_number = gstDetailData.value?.gstNumber!!.replace("-", "")))
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            userPrefs.gstNumber = gstDetailData.value?.gstNumber!!.replace("-","")
                            userPrefs.gstAddress = gstDetailData.value?.address.toString()
                            userUpdateLiveData.postValue(true)
                        } else{
                            error.handle()
                            userUpdateLiveData.postValue(false)
                        }
                    }
        }else{
            errorLiveData.postValue(Pair(com.delhivery.axle.ui.kyc.gst.AuthenticationUIError.InvalidGSTNumber, "Invalid GST Number"))
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
                                Log.d("dmsdlfslf", _res.getGstList(_res.gstin_numbers).size.toString())

                                for (warehouse in _res.getGstList(_res.gstin_numbers)) {
                                    Log.d("csdmksdk","mkop")
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
        compositeDisposable += loadboardRepository.gstDetails(data.gstNumber)
                .onBackground()
                .subscribe { _res, error ->
                    if (!error) {
                        data.gstDetailItemData = _res
                        Log.d("dsenssdkksd", data.toString())
                    } else {
                        data.gstDetailItemData = null
                    }
                    gstDetailsLiveData.postValue(data)
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