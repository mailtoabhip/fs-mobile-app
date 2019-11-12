package com.delhivery.axle.ui.tripdetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject

/**
 * View model for [UploadImageActivity]
 */
class UploadImageViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsRepository: TripsRepository
) : BaseViewModel() {

  var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
  var uploadResultLiveData = MutableLiveData<Boolean>()

  lateinit var transactionId: String
  var imagePaths: MutableList<String> = mutableListOf()
  var imageUrls: MutableList<String> = mutableListOf()

  /**
   * Get delegation token for AWS
   */
  fun getDelegationToken(
    file: File
  ) {
    compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            delegationLiveData.postValue(Pair(_res.delegationToken, file))
          } else
            error.handle()
        }
  }

  /**
   * Upload POD
   */
  fun uploadPod() {
    compositeDisposable += tripsRepository.uploadPod(transactionId, imageUrls)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            uploadResultLiveData.postValue(true)
          } else {
            uploadResultLiveData.postValue(false)
          }
        }
  }

}