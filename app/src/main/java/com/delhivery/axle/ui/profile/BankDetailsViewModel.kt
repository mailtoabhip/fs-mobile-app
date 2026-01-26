package com.delhivery.axle.ui.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.request.VerificationDocUploadRequest
// Removed AWS imports - using Document API now
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
  var accountText= userPrefs.paymentAccountNumber
  var ifscText= userPrefs.ifscCode
  var accountHolderText= userPrefs.paymentAccountName
  // Removed delegation token LiveData - using Document API now
  var delegationDownloadLiveData = MutableLiveData<Triple<Any, String, File>>() // Keep for backward compatibility
  var verificationDocUploadMsg = MutableLiveData<String>()
  val accountkycDocuments = MutableLiveData<String>()
  val nine4CkycDocuments = MutableLiveData<String>()
  var imagePath = ""


  // Removed getDelegationToken - uploads now handled directly by DocumentUtils


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
  fun getKycDoc(){
    compositeDisposable += userRepository.getKycDocs()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res!=null) {
            _res.responseData?.kyc_documents?.forEach {
              // Filter for bank account documents (new secure API path structure)
              if(it.contains("bankaccount", ignoreCase = true) || 
                 it.contains("cancelled_cheque", ignoreCase = true) ||
                 it.contains("passbook", ignoreCase = true)) {
                accountkycDocuments.value = it
                Log.d("BankDetailsViewModel", "Found bank account document: $it")
              }
              // Filter for 194C documents (new secure API path structure)
              if(it.contains("section_194c", ignoreCase = true)) {
                nine4CkycDocuments.value = it
                Log.d("BankDetailsViewModel", "Found 194C document: $it")
              }
            }
          } else
            error.handle()
        }
  }
  // Removed getDownloadDelegationToken - downloads now handled by Document API

}