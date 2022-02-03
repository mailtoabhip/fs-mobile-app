package com.delhivery.axle.ui.kyc.pan

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.regex.Matcher
import javax.inject.Inject
import java.util.regex.Pattern


class PanVerificationViewModel@Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs
) :
    BaseViewModel() {

    var validatePanLiveData = MutableLiveData<PanVerificationResponse>()

    var userUpdateLiveData = MutableLiveData<Boolean>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

    var panCardNumber=""
    var isValidPan = false

    /**
     * Validate PAN regex
     */
    private var panValidationRegex=  "[A-Z]{5}[0-9]{4}[A-Z]{1}";

    /**
     * Validate PAN
     */
    fun validatePAN() {
        if (!isConnected) return

        if (panCardNumber.length < 10 || !parsePanToValidate(panCardNumber)) {
            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
            return
        }
        compositeDisposable += loadboardRepository.validatePanNumber(panCardNumber)
            .onBackground()
            .subscribe { _res, error ->
                if (!error) {
                    isValidPan = true
                    validatePanLiveData.postValue(_res)
                } else{
                    error.handle()
                   errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                }
            }

    }

    /**
     * update user pan number
     */
    fun updateUserDetails() {
        if (!isConnected) return

        if (panCardNumber.length == 10 || isValidPan) {
            compositeDisposable += loadboardRepository.updateUser("+91"+userPrefs.phoneNumber,panCardNumber,null)
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        userUpdateLiveData.postValue(true)
                    } else{
                        error.handle()
                        userUpdateLiveData.postValue(false)
                    }
                }
        }

    }

    //Validate Pan regex check
    fun parsePanToValidate(panCardNo:String):Boolean{
        val p: Pattern = Pattern.compile(panValidationRegex)
        val m: Matcher = p.matcher(panCardNo)
        return m.matches()
    }
}

/**
 * Authentication UI Error
 */
enum class AuthenticationUIError {
    None,
    InvalidPANNumber
}
