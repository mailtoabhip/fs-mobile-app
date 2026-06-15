package com.dfd.delfin.ui.kyc.aadhaar

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.request.UpdateUserRequest
import com.dfd.delfin.api.response.PanVerificationResponse
// Removed AWS imports - using Document API now
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class AadhaarVerificationViewModel@Inject constructor(
   private  val loadboardRepository: LoadboardRepository,
   private val userRepository: UserRepository,
   private val userPrefs: UserPrefs
) :
BaseViewModel() {

    var validateAadhaarLiveData = MutableLiveData<PanVerificationResponse>()

    var otpRecieved = MutableLiveData<Boolean>()

    var otpVerified = MutableLiveData<Boolean>()
    var docVerified = MutableLiveData<Boolean>()
    var docVerificationFailedCount = MutableLiveData<Int>().apply { postValue(0) }
    var errorText:String? = ""


    /* steps */
    var currentStep = ""

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

    var aadhaarCardNumber=""

    var aadhaarPolicyAccepted=false

    var otpStatusLiveData = MutableLiveData<Boolean>()

    fun verifyRequestAadhaarOtp(otp: CharArray) {
        if (!isConnected) return
        val _otp = otp.joinToString("")
            compositeDisposable +=loadboardRepository.verifyGstOrAadhaarOtp("aadhaar",aadhaarCardNumber.replace("-",""),_otp)
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        otpVerified.postValue(true)
                    } else{
                        error.handle()
                        otpVerified.postValue(false)
                    }
                }

    }

     fun getRequestAadhaarOtp(launchDialog:Boolean) {
        if (!isConnected) return

        if(aadhaarCardNumber.length==14){
            compositeDisposable +=loadboardRepository.getGstOrAadhaarOtp("aadhaar",aadhaarCardNumber.replace("-",""))
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        if(launchDialog){
                            otpRecieved.postValue(true)
                        }
                    } else{
                        error.handle()
                        if(launchDialog){
                            otpRecieved.postValue(false)
                        }


                    }
                }
        }else{
            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidAadhaarNumber, "Invalid Aadhaar Number"))
        }
    }

    fun verifyByDoc(docList:List<String>) {
        if (!isConnected) return

        compositeDisposable +=loadboardRepository.verifyByDocUpload("aadhaar",aadhaarCardNumber.replace("-",""),docList)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    if(_res.isVerified == true){
                        docVerified.postValue(true)
                    }else{
                        docVerified.postValue(false)
                    }
                } else{
                    error.handle()
                    docVerified.postValue(false)
                }
            }

    }

    // Removed delegationLiveData and getDelegationToken - uploads now handled directly by DocumentUtils
    /**
     * update user pan number
     */
    var userUpdateLiveData = MutableLiveData<Boolean>()

    fun updateUserDetails() {
        if (!isConnected) return

        if (aadhaarCardNumber.length == 14) {
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber= userPrefs.phoneNumber.toString(), aadhaarNumber = aadhaarCardNumber.replace("-",""),aadhaarPolicyAccepted= aadhaarPolicyAccepted))
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        userPrefs.aadhaarNumber = aadhaarCardNumber.replace("-","")
                        userUpdateLiveData.postValue(true)
                    } else{
                        error.handle()
                        userUpdateLiveData.postValue(false)
                    }
                }
        }

    }
}

/**
 * Authentication UI Error
 */
enum class AuthenticationUIError {
    None,
    InvalidAadhaarNumber
}