package com.dfd.delfin.ui.kyc.identityverification

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.request.UpdateUserRequest
import com.dfd.delfin.api.request.VerificationDocUploadRequest
// Removed DelegationToken and AWSConfig imports - no longer needed
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class IdentityVerificationViewModel@Inject constructor(
    private  val userRepository: UserRepository,
    private  val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs): BaseViewModel()
{
    var selected =""

    var currentStep = ""

    var cinNumber = ""
    var shopNumber=""
    var udyogNumber=""
    var userUpdateLiveData = MutableLiveData<Boolean>()
    var verificationDocUploadLiveData = MutableLiveData<Boolean>()
    var errorText:String? = ""

    // Removed delegation token logic - direct upload now handled in Activity
    
    // Download functionality
    var documentListLiveData = MutableLiveData<List<com.dfd.delfin.api.response.DocumentFile>>()
    var documentListErrorLiveData = MutableLiveData<String>()
    
    fun loadIdentityDocuments(docType: String) {
        // This method can be called from Activity to trigger identity document loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }

    /**
     * User Update api
     */
    fun updateUserDetails() {
        if (!isConnected) return
        compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = userPrefs.phoneNumber!!,cinNumber = if(cinNumber.isNotEmpty())cinNumber else null,udyogAadhaarNumber = if(udyogNumber.isNotEmpty())udyogNumber else null,shopEstablishmentNumber = if(shopNumber.isNotEmpty())shopNumber else null))
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    userPrefs.isIdentityVerified = true
                    userUpdateLiveData.postValue(true)
                } else{
                    error.handle()
                    userUpdateLiveData.postValue(false)
                }
            }

    }

    fun uploadDocForVerification(verificationDocUploadRequest: VerificationDocUploadRequest){
        compositeDisposable += loadboardRepository.uploadVerificationDoc(verificationDocUploadRequest)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res!=null) {
                    userPrefs.identityDocUrl = verificationDocUploadRequest.documentUrls?.get(0)?:""
                    verificationDocUploadLiveData.postValue(true)
                } else
                    error.handle()
            }
    }

    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return
        uploadDocForVerification(VerificationDocUploadRequest(verificationId =  if(cinNumber.isNotEmpty())cinNumber else if(udyogNumber.isNotEmpty())udyogNumber else if(shopNumber.isNotEmpty())shopNumber else null,proofDocumentType = selected,documentUrls = docList))
    }


}

