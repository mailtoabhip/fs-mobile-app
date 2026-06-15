package com.dfd.delfin.ui.paymentdetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.request.BankValidationRequest
import com.dfd.delfin.api.request.UpdateUserRequest
import com.dfd.delfin.api.request.VerificationDocUploadRequest
import com.dfd.delfin.api.response.BankValidationResponse
// Removed DelegationToken and AWSConfig imports - no longer needed
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.errorPaymentResponseBody
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
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
    var verificationDocUploadMsg = MutableLiveData<String>()
    var verificationDocUploadFailed = MutableLiveData<Boolean>()
    var selected194CUpload= MutableLiveData<Boolean>()
    var accountUpload= MutableLiveData<Boolean>()
    var isAccountProofDoc = false
    var userUpdateLiveData = MutableLiveData<Boolean>()
    var vendorUserUpdateLiveData = MutableLiveData<Boolean>()
    var nameDeclaration =false
    var bankValidaton=MutableLiveData<Pair<Boolean,BankValidationResponse>>()
    var bankValidated = false
    var panMatched  = true
    var bankValidationApiFailed=false
    var accountDoesNotExist = MutableLiveData<Pair<Boolean,String>>()

    // Removed delegation token logic - direct upload now handled in Activity
    
    // Download functionality
    var documentListLiveData = MutableLiveData<List<com.dfd.delfin.api.response.DocumentFile>>()
    var documentListErrorLiveData = MutableLiveData<String>()
    var isBankVerified = false
    
    fun loadPaymentDocuments() {
        // This method can be called from Activity to trigger payment document loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }
    
    fun load194CDocuments() {
        // This method can be called from Activity to trigger 194C document loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }


    init {
        setBankVerificationStatus()
    }
    fun uploadDocForVerification(verificationDocUploadRequest: VerificationDocUploadRequest){
        compositeDisposable += loadboardRepository.uploadVerificationDoc(verificationDocUploadRequest)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res!=null) {
                    if (isAccountProofDoc==false) {
                        userPrefs.ninteen4CDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                    }else{
                        userPrefs.paymentDocUrl =
                            verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                        Log.d("docurl",verificationDocUploadRequest.documentUrls?.get(0).toString())
                    }
                    verificationDocUploadMsg.postValue(_res)
                } else {
                    if (isAccountProofDoc == false) {
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
            isAccountProofDoc = false
        }else{
            uploadDocForVerification(
                VerificationDocUploadRequest(
                    proofDocumentType = "cancelled_cheque",
                    documentUrls = docList
                )
            )
            isAccountProofDoc = true
        }

    }
    fun setBankVerificationStatus(){
        isBankVerified = userPrefs.ifscCode.isNotNullOrEmpty() && userPrefs.accNumber.isNotNullOrEmpty() && !userPrefs.accNumber.equals("Not Available",true)
    }
}