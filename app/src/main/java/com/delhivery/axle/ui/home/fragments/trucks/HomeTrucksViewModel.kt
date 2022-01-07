package com.delhivery.axle.ui.home.fragments.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import java.text.FieldPosition
import javax.inject.Inject

class HomeTrucksViewModel @Inject constructor(

): BaseViewModel() {

    var frequentTrucks = MutableLiveData<List<HomeTrucksRequestItemData>>()

    fun deactivateTruck(
        reason: String,
        position: Int
    ){
       // compositeDisposable+=

    }

    fun getFrequentTrucks(){

    }
}