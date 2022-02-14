package com.delhivery.axle.ui.businessverification

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.VerificationDocUploadRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.ErrorResponseBody
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.exception.HttpErrorCode
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import retrofit2.adapter.rxjava2.Result.response
import java.io.File
import javax.inject.Inject

class BusinessVerificationViewModel@Inject constructor(
    private  val userRepository: UserRepository,
    private  val loadboardRepository: LoadboardRepository

) :BaseViewModel(){

    var truckNumber=MutableLiveData<String>()

    var selected = MutableLiveData<String>()

    var currentStep = ""



    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    var manualVerificationRequired = MutableLiveData<Boolean>()
    var rcVerificationErrorMsg = MutableLiveData<String>()
    var verificationDocUploadMsg = MutableLiveData<String>()



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
                } else
                    error.handle()
                if(!error.message.isNullOrEmpty()) {
                    rcVerificationErrorMsg.postValue(error.message)
                   // manualVerificationRequired.postValue(true)

                }
            }
    }


    fun uploadDocForVerification(verificationDocUploadRequest: VerificationDocUploadRequest){
        compositeDisposable += loadboardRepository.uploadVerificationDoc(verificationDocUploadRequest)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res!=null) {
                    verificationDocUploadMsg.postValue(_res)
                } else
                    error.handle()
                if(error.message!=null) {
                    rcVerificationErrorMsg.postValue(error.message)
                }
            }
    }


    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return

      uploadDocForVerification(VerificationDocUploadRequest(proofDocumentType = selected.value,documentUrls = docList))

    }


}