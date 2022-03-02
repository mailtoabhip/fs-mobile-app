package com.delhivery.axle.ui.kyc.aadhaar

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.databinding.DialogVerifyGstOtpBinding
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.DialogUtilsInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
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

    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
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
     * update user pan number
     */
    var userUpdateLiveData = MutableLiveData<Boolean>()

    fun updateUserDetails() {
        if (!isConnected) return

        if (aadhaarCardNumber.length == 14) {
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber= userPrefs.phoneNumber.toString(), aadhaarNumber = aadhaarCardNumber.replace("-","")))
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