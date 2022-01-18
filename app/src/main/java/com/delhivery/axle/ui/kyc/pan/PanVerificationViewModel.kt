package com.delhivery.axle.ui.kyc.pan

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.ui.auth.AuthenticationUIState
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.GlobalPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import javax.inject.Inject

class PanVerificationViewModel@Inject constructor(

) :
    BaseViewModel() {

    var validatePanLiveData = MutableLiveData<PanVerificationResponse>()

    /* error live data */
    var errorLiveData = MutableLiveData<Pair<AuthenticationUIError, String?>>()

    var panCardNumber=""

    /**
     * Validate PAN
     */
    fun validatePAN() {
        if (!isConnected) return

        if (panCardNumber.length < 10) {
            errorLiveData.postValue(Pair(AuthenticationUIError.InvalidPANNumber, "Invalid Pan"))
            return
        }
        if(true){
            val panVerificationResponse = PanVerificationResponse(panCardNumber,"Rahul Kumar","personal")
            validatePanLiveData.postValue(panVerificationResponse)
        }

    }
}

/**
 * Authentication UI Error
 */
enum class AuthenticationUIError {
    None,
    InvalidPANNumber
}