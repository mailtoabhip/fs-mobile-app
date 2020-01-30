package com.delhivery.axle.ui.home.activity.docket

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.request.DispatchData
import com.delhivery.axle.api.request.UpdateDispatchRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject

/**
 * View model for [DocketUpdateActivity]
 */
class DocketUpdateViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsRepository: TripsRepository
) : BaseViewModel() {

  var trip: HomeTripsItemData? = null
  var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
  var statusLiveData = MutableLiveData<Boolean>()
  var transactionIds = mutableListOf<String>()
  var imagePath = ""
  var imageUrl = ""
  var dateOfDispatch = ""
  var trackingNumber = ""

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
   * Update disatch details
   */
  fun updateDispatchDetails() {
    val listDispatch = mutableListOf<DispatchData>()
    transactionIds.forEach {
      listDispatch.add(DispatchData(it, trackingNumber, imageUrl, dateOfDispatch))
    }
    compositeDisposable += tripsRepository.updateDispatchDetails(
        UpdateDispatchRequest(listDispatch)
    )
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            statusLiveData.postValue(true)
          } else {
            error?.handle()
            statusLiveData.postValue(false)
          }
        }
  }

}