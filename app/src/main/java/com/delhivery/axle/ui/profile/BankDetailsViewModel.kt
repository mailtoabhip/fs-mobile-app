package com.delhivery.axle.ui.profile

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.VerificationDocUploadRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class BankDetailsViewModel@Inject constructor(
private  val userRepository: UserRepository,
private  val loadboardRepository: LoadboardRepository,
private val userPrefs: UserPrefs

): BaseViewModel() {
  var accountText= userPrefs.accNumber
  var ifscText= userPrefs.ifscCode
  var accountHolderText= userPrefs.accNumber
  var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
  var verificationDocUploadMsg = MutableLiveData<String>()

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
            verificationDocUploadMsg.postValue(_res)
          } else
            error.handle()
        }
  }

  fun verifyByDoc(docList:List<String>) {

      uploadDocForVerification(
          VerificationDocUploadRequest(
              proofDocumentType = "section_194C",
              documentUrls = docList
          )
      )
  }


}