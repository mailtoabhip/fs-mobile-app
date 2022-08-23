package com.delhivery.axle.ui.businessverification

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.request.VerificationDocUploadRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.ErrorResponseBody
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.exception.HttpErrorCode
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import retrofit2.adapter.rxjava2.Result.response
import java.io.File
import javax.inject.Inject

class BusinessVerificationViewModel@Inject constructor(
    private  val userRepository: UserRepository,
    private  val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs


) :BaseViewModel(){

    var truckNumber=MutableLiveData<String>()
    var attachedTruck=MutableLiveData<String>()
    var ownedTruck=MutableLiveData<String>()

    var selected = MutableLiveData<String>()

    var currentStep = ""

    var errorText:String? = ""
    var userUpdateLiveData = MutableLiveData<Boolean>()
    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    var manualVerificationRequired = MutableLiveData<Boolean>()
    var rcVerificationErrorMsg = MutableLiveData<String>()
    var verificationDocUploadMsg = MutableLiveData<String>()
    var verificationDocUploadFailed = MutableLiveData<Boolean>()



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

    fun validateRC (rc :String){
        compositeDisposable += loadboardRepository.validateRC(rc)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    manualVerificationRequired.postValue(_res.manualVerificationRequired)
                    userPrefs.rcManualverificationreq=_res.manualVerificationRequired
                } else{
                  //  error.handle()
                val errorBody = error.errorResponseBody()
                    ?.errorBody
                if (errorBody != null) {
                    when (errorBody.errorCode()) {
                      400-> {
                            rcVerificationErrorMsg.postValue(errorBody.errorMessage)
                        }
                        else -> {
                            Throwable(errorBody.errorMessage).handle()
                        }
                    }
                } else {
                    error?.handle()
                }
               /* if(!error.message.isNullOrEmpty()) {
                    rcVerificationErrorMsg.postValue(error.message)
                   // manualVerificationRequired.postValue(true)

                }*/
              }
            }
    }


    fun uploadDocForVerification(verificationDocUploadRequest: VerificationDocUploadRequest){
        compositeDisposable += loadboardRepository.uploadVerificationDoc(verificationDocUploadRequest)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res!=null) {
                  userPrefs.businessDocUrl =
                    verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                    verificationDocUploadMsg.postValue(_res)
                } else {
                    userPrefs.businessDocUrl =
                      verificationDocUploadRequest.documentUrls?.get(0) ?: ""
                  verificationDocUploadFailed.postValue(true)
                  userPrefs.rcManualverificationreq=false
                  error.handle()
                }
            }
    }


    fun updateUserDetails() {
        if (!isConnected) return

            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = userPrefs.phoneNumber!!,isTruckingDocumentUploaded = true,numberOfAttachedTrucks = attachedTruck.value?.toInt(),numberOfOwnedTrucks = ownedTruck.value?.toInt()))
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        userPrefs.ownedTruck= ownedTruck.value.toString()
                      userPrefs.businessDocType=selected.value.toString()
                      userUpdateLiveData.postValue(true)
                        userPrefs.isTruckingDocumentUploaded=true
                    } else{
                        error.handle()
                        userUpdateLiveData.postValue(false)
                    }
                }
    }

    fun updateUserRCDetails(rc: String) {
        if (!isConnected) return

        compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = userPrefs.phoneNumber!!,rcNumber = rc,numberOfAttachedTrucks = attachedTruck.value?.toInt(),numberOfOwnedTrucks = ownedTruck.value?.toInt()))
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    userPrefs.rcNumber=rc
                    userPrefs.businessDocType=selected.value.toString()
                    userPrefs.attachedTruck=attachedTruck.value.toString()
                    userUpdateLiveData.postValue(true)
                } else{
                    error.handle()
                    userUpdateLiveData.postValue(false)
                }
            }

    }


    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return
       if(selected.value.equals("rc")) {
         userPrefs.businessDocType=selected.value.toString()
         uploadDocForVerification(
               VerificationDocUploadRequest(
                   verificationId = truckNumber.value,
                   proofDocumentType = selected.value,
                   documentUrls = docList
               )
           )
       }else{
         userPrefs.businessDocType=selected.value.toString()
         uploadDocForVerification(
               VerificationDocUploadRequest(
                   proofDocumentType = selected.value,
                   documentUrls = docList
               )
           )

       }

    }


}