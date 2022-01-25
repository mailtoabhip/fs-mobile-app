package com.delhivery.axle.ui.businessverification

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class BusinessVerificationViewModel@Inject constructor() :BaseViewModel(){

    var truckNumber=""

    var selected = MutableLiveData<Boolean>()

    fun visibility() = if(selected.value!!)
        View.VISIBLE
    else
        View.GONE

}