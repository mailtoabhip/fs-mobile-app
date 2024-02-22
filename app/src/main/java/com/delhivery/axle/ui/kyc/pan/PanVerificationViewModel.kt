package com.delhivery.axle.ui.kyc.pan

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.request.ResetKycDataRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.kyc.gst.BaseGstRVAdapterItem
import com.delhivery.axle.ui.kyc.pan.AuthenticationUIError
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
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
    var resetKycLiveData = MutableLiveData<Boolean>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()
    var duplicatePanErrorLiveData = MutableLiveData<String?>()
    var panNotLinkedToAadhaarErrorLiveData = MutableLiveData<Pair<String?,String?>>()


    var panCardNumber=""
    var isValidPan = false

    /* steps */
    var currentStep = ""
    var errorMsg = ""

    /* pay type */
    var panType = "person"

    /**
     * Validate PAN regex
     */
    private var panValidationRegex=  "[A-Z]{5}[0-9]{4}[A-Z]{1}";

    var gstNumbersLiveData = MutableLiveData<List<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>>()

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
                  Log.d("error",error.toString())
                  val errorBody = error.errorResponseBody()
                      ?.errorBody
                  if (errorBody != null) {
                    when (errorBody.errorCode()) {
                      400-> {
                        if(errorBody.data!=null){
                          if(errorBody.data.isNotEmpty()) {
                            if (errorBody.data[0].isDuplicatePan == true) {
                              duplicatePanErrorLiveData.postValue(R.string.error_duplicate_pan.toString())
                            } else if (errorBody.data[0].linkingStatus == false) {
                              panNotLinkedToAadhaarErrorLiveData.postValue(Pair(errorBody.data[0].message,errorBody.data[0].panHolderName))
                            }
                            else{
                              error.handle()
                              errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                            }
                          }
                          else if(errorBody.errorMessage.isNotNullOrEmpty()){
                            Throwable(errorBody.errorMessage).handle()
                            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                          }
                          else{
                            error.handle()
                            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                          }
                        }
                        else if(errorBody.errorMessage.isNotNullOrEmpty()){
                          Throwable(errorBody.errorMessage).handle()
                          errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                        }
                        else {
                          error.handle()
                          errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                        }
                      }
                      else -> {
                        errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number"))
                        Throwable(errorBody.errorMessage).handle()
                      }
                    }
                  } else {
                    error.handle()
                    errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan Number")
                    )
                  }
                }
            }
    }

    /**
     * update user pan number
     */
    fun updateUserDetails() {
        if (!isConnected) return

        if (panCardNumber.length == 10 || isValidPan) {
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = userPrefs.phoneNumber!!,panNumber = panCardNumber))
                    .flatMap { _Res-> loadboardRepository.gstNumbers(panCardNumber)
                            .map {
                                val msg = if (_Res.isNotNullOrEmpty()) {
                                    _Res
                                } else {
                                    "Error getting gst"
                                }
                                userPrefs.isGstsByPanNotRegistered = it.gstin_numbers.isNullOrEmpty()
                                Log.d("reject",it.gstin_numbers.isNullOrEmpty().toString())
                                Triple(_Res, msg, it)
                            }
                    }
                    .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error && !_res.second.equals("Error getting gst")) {
                        userUpdateLiveData.postValue(true)
                        userPrefs.pancard= panCardNumber
                    } else{
                        error.handle()
                        userUpdateLiveData.postValue(false)
                    }
                }
        }

    }

  /**
   * update user pan number
   */
  fun resetKycDetails(ignoreClearData:Boolean) {
    if (!isConnected) return

    if (panCardNumber.length == 10 || isValidPan) {
      compositeDisposable += loadboardRepository.resetKyc(ResetKycDataRequest(phoneNumber = userPrefs.phoneNumber!!, resetPoint = "pan",panNumber = panCardNumber))
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            if(!ignoreClearData){
              userPrefs.clearKycPreferenceDataBasedOnSteps("pan")
            }
            resetKycLiveData.postValue(true)
          } else{
            error.handle()
            resetKycLiveData.postValue(true)
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
