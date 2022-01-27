package com.delhivery.axle.ui.businessverification

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject

class BusinessVerificationViewModel@Inject constructor(
    private  val userRepository: UserRepository
) :BaseViewModel(){

    var truckNumber=MutableLiveData<String>()

    var selected = MutableLiveData<Boolean>().postValue(false)

//    fun visibility()= if(selected.value!!)
//        View.VISIBLE
//    else
//        View.GONE
//


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
}