package com.delhivery.axle.ui.kyc.aadhaar

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class AadhaarVerificationViewModel@Inject constructor(

) :
BaseViewModel() {

    var validateAadhaarLiveData = MutableLiveData<PanVerificationResponse>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

    var aadhaarCardNumber=""

    /**
     * Validate Aadhaar
     */
    fun validateAadhaar() {
        if (!isConnected) return

        if (aadhaarCardNumber.length < 10) {
            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidAadhaarNumber, "Invalid Aadhaar"))
            return
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