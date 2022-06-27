package com.delhivery.axle.ui.paymentdetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.BankValidationRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.request.VerificationDocUploadRequest
import com.delhivery.axle.api.response.BankValidationResponse
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.kyc.pan.AuthenticationUIError
import com.delhivery.axle.utils.extensions.errorPaymentResponseBody
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class PaymentDetailsViewModel@Inject constructor(
    private  val userRepository: UserRepository,
    private  val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs
): BaseViewModel() {

    var errorText:String? = ""
    var accountText= MutableLiveData<String>()
    var ifscText=MutableLiveData<String>()
    var accountHolderText=MutableLiveData<String>()
    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    var verificationDocUploadMsg = MutableLiveData<String>()
    var verificationDocUploadFailed = MutableLiveData<Boolean>()
    var selected194CUpload= MutableLiveData<Boolean>()
    var userUpdateLiveData = MutableLiveData<Boolean>()
    var vendorUserUpdateLiveData = MutableLiveData<Boolean>()
    var nameDeclaration =false
    var bankValidaton=MutableLiveData<Pair<Boolean,BankValidationResponse>>()
    var bankValidated = false
    var panMatched  = true
    var bankValidationApiFailed=false
    var accountDoesNotExist = MutableLiveData<Pair<Boolean,String>>()

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


    fun uploadDocForVerification(verificationDocUploadRequest: VerificationDocUploadRequest){
        compositeDisposable += loadboardRepository.uploadVerificationDoc(verificationDocUploadRequest)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res!=null) {
                    if (selected194CUpload.value==true) {
                        userPrefs.ninteen4CDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                    }else{
                        userPrefs.paymentDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                        Log.d("docurl",verificationDocUploadRequest.documentUrls?.get(0).toString())
                    }
                    verificationDocUploadMsg.postValue(_res)
                } else {
                    if (selected194CUpload.value == true) {
                        userPrefs.ninteen4CDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                    } else {
                        userPrefs.paymentDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                    }
                    verificationDocUploadFailed.postValue(true)
                    error.handle()
                }
            }
    }

    fun updateUserDetails() {
        if (!isConnected) return
        if (!userPrefs.phoneNumber.isNullOrEmpty()) {

            compositeDisposable += loadboardRepository.updateUser(
                if (nameDeclaration){
                UpdateUserRequest(
                    phoneNumber = userPrefs.phoneNumber!!, accountNumber = accountText.value,
                    ifscCode = ifscText.value, accountHolderName = accountHolderText.value,nameDeclaration = "yes")
                }else{
                    UpdateUserRequest(
                        phoneNumber = userPrefs.phoneNumber!!, accountNumber = accountText.value,
                        ifscCode = ifscText.value, accountHolderName = accountHolderText.value
                    )
                }
            )
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        userUpdateLiveData.postValue(true)
                    } else {
                        userUpdateLiveData.postValue(false)
                        verificationDocUploadFailed.postValue(true)
                        error.handle()
                    }
                }
        }
    }

    fun updateUserDetailsForVendorPolicy() {
        if (!isConnected) return
        if (!userPrefs.phoneNumber.isNullOrEmpty()) {
            compositeDisposable += loadboardRepository.updateUser(
                    UpdateUserRequest(
                        phoneNumber = userPrefs.phoneNumber!!,vendorPolicyAccepted = true
            )
            )
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        vendorUserUpdateLiveData.postValue(true)
                    } else {
                        vendorUserUpdateLiveData.postValue(false)
                        error.handle()
                    }
                }
        }
    }

 fun getBankName(accountNum:String,ifsc:String){
     compositeDisposable += loadboardRepository.getBankName(
         BankValidationRequest(
             accountNum,ifsc
         )
     )
         .onBackground()
         .progress()
         .subscribe { _res, error ->
             if (!error) {
                 panMatched=_res.validated!!
                 bankValidaton.postValue(Pair(true,_res))
             } else {
                 Log.d("error",error.toString())
                 val errorBody = error.errorPaymentResponseBody()
                     ?.errorBody
                 if (errorBody != null) {
                     when (errorBody.code()) {
                         400-> {
                             if(errorBody.data?.accountExists != null){
                                 if(errorBody.data.accountExists==false){
                                     accountDoesNotExist.postValue(Pair(true,errorBody.errorMessage))
                                 }else{
                                     bankValidaton.postValue(Pair(false,_res))
                                     error.handle()
                                 }
                             }else{
                                 bankValidaton.postValue(Pair(false,_res))
                                 error.handle()
                             }
                         }
                         else -> {
                             when (errorBody.errorCode()){
                                 400->{
                                     if(errorBody.errorMessage.equals("Missing or invalid data provided for: account_number, ifsc_code")){
                                         accountDoesNotExist.postValue(Pair(true,errorBody.errorMessage))
                                     }else{
                                         bankValidaton.postValue(Pair(false,_res))
                                         error.handle()
                                     }
                                 }
                                 else -> {
                                     bankValidaton.postValue(Pair(false,_res))
                                     error.handle()
                                 }
                             }
                         }
                     }

                 } else {
                     bankValidaton.postValue(Pair(false,_res))
                     error.handle()

                 }

             }
         }
 }




    fun verifyByDoc(docList:List<String>) {
        if (selected194CUpload.value==true && docList.get(0).contains("194C",true)) {
            uploadDocForVerification(
                VerificationDocUploadRequest(
                    proofDocumentType = "section_194C",
                    documentUrls = docList
                )
            )
        }else{
            uploadDocForVerification(
                VerificationDocUploadRequest(
                    proofDocumentType = "cancelled_cheque",
                    documentUrls = docList
                )
            )
        }

    }
}