package com.delhivery.axle.ui.kyc.gst

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.data.gst.GstTimeOutItemData
import com.delhivery.axle.data.gst.GstWarningAction_NoResult
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.transactions.TransactionHeaderItemData
import com.delhivery.axle.data.transactions.TransactionType
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.activity.transactionlist.*
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class GstVerificationViewModel@Inject constructor(
        private  val loadboardRepository: LoadboardRepository,
        private  val userRepository: UserRepository
) : BaseViewModel() {

    var gstLiveData = MutableLiveData<List<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var dataLoadingLiveData = MutableLiveData<Boolean>()
    /* steps */
    var currentStep = ""

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
     * Get GST numbers and details
     */
    fun getGstDetails(pan_number:String){
    }
}