package com.delhivery.axle.ui.paymentdetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class PaymentDetailsViewModel@Inject constructor(
    private  val userRepository: UserRepository,
    private  val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs
): BaseViewModel() {

    var errorText:String? = ""
    var accountText= MutableLiveData<String>()
    var ifscText=MutableLiveData<String>()
    var accountHolderText=MutableLiveData<String>()

}