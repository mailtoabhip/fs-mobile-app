package com.delhivery.axle.ui.kyc.identityverification

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.request.VerificationDocUploadRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
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
    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    var verificationDocUploadLiveData = MutableLiveData<Boolean>()
    var errorText:String? = ""

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
                    verificationDocUploadLiveData.postValue(true)
                } else
                    error.handle()
            }
    }

    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return
        uploadDocForVerification(VerificationDocUploadRequest(proofDocumentType = selected,documentUrls = docList,cinNumber = if(cinNumber.isNotEmpty())cinNumber else null,udyogAadhaarNumber = if(udyogNumber.isNotEmpty())udyogNumber else null,shopEstablishmentNumber = if(shopNumber.isNotEmpty())shopNumber else null))
    }


}

