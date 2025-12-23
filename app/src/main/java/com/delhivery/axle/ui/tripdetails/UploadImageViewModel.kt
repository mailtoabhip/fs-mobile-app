package com.delhivery.axle.ui.tripdetails

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.PODData
import com.delhivery.axle.api.request.PodRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.PodItem
import com.delhivery.axle.data.PodState
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * View model for [UploadImageActivity]
 */
class UploadImageViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsRepository: TripsRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
  var uploadResultLiveData = MutableLiveData<Boolean>()
  var podItemsLiveData = MutableLiveData<List<PodItem>>()
  var podSubmissionResultLiveData = MutableLiveData<Boolean>() // true = success, false = error

  lateinit var transactionId: String
  lateinit var reachedTime: String
  lateinit var unloadedTime: String
  var imagePaths: MutableList<String> = mutableListOf()
  var imageUrls: MutableList<String> = mutableListOf()

  private var podItems: MutableList<PodItem> = mutableListOf()
  private var currentUploadingPodId: Int? = null

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
   * Initialize POD items - all start as EMPTY, will become available when user starts upload
   */
  fun initializePods() {
    podItems = mutableListOf()
    // Initialize all pods as empty initially
    for (i in 1..10) {
      podItems.add(PodItem(i, PodState.EMPTY))
    }
      Log.d("PodItems-Init", "$podItems")
    podItemsLiveData.postValue(podItems)
  }

  /**
   * Start upload process - make first pod available
   */
  fun startUploadProcess() {
    if (podItems.isEmpty()) {
      initializePods()
    }
    // Make first pod available when user clicks maxWidth
    if (podItems.isNotEmpty() && podItems[0].state == PodState.EMPTY) {
      podItems[0] = podItems[0].copy(state = PodState.AVAILABLE)
        Log.d("PodItems-Start", "$podItems")
      podItemsLiveData.postValue(podItems)
    } else {
      Log.d("ViewModel", "Pod1 state is not EMPTY: ${podItems.getOrNull(0)?.state}")
    }
  }

  /**
   * Set image as selected (not uploaded yet)
   */
  fun setImageSelected(podId: Int, filePath: String, imageUri: Uri? = null) {
    val index = podItems.indexOfFirst { it.id == podId }
    if (index != -1) {
      podItems[index] = podItems[index].copy(
        state = PodState.SELECTED,
        imagePath = filePath,
        imageUri = imageUri
      )

      // Make next pod available if not at max
      if (podId < 10) {
        val nextIndex = podItems.indexOfFirst { it.id == podId + 1 }
        if (nextIndex != -1 && podItems[nextIndex].state == PodState.EMPTY) {
          podItems[nextIndex] = podItems[nextIndex].copy(state = PodState.AVAILABLE)
        }
      }
        Log.d("PodItems-Selected", "$podItems")
      podItemsLiveData.postValue(podItems)
    }
  }

  /**
   * Start upload for a specific pod (when Submit is clicked)
   */
  fun startUpload(podId: Int, filePath: String) {
    val index = podItems.indexOfFirst { it.id == podId }
    if (index != -1) {
      currentUploadingPodId = podId
      podItems[index] = podItems[index].copy(
        state = PodState.UPLOADING,
        imagePath = filePath
      )
        Log.d("PodItems-StartUpload", "$podItems")
      podItemsLiveData.postValue(podItems)
      imagePaths.add(filePath)
    }
  }

  /**
   * Handle successful upload
   */
  fun onUploadSuccess(podId: Int, imageUrl: String) {
    val index = podItems.indexOfFirst { it.id == podId }
    if (index != -1) {
      podItems[index] = podItems[index].copy(
        state = PodState.UPLOADED,
        imageUrl = imageUrl
      )
      imageUrls.add(imageUrl)

      // Make next pod available if not at max
      if (podId < 10) {
        val nextIndex = podItems.indexOfFirst { it.id == podId + 1 }
        if (nextIndex != -1 && podItems[nextIndex].state == PodState.EMPTY) {
          podItems[nextIndex] = podItems[nextIndex].copy(state = PodState.AVAILABLE)
        }
      }
        Log.d("PodItems-Success", "$podItems")
      podItemsLiveData.postValue(podItems)
      currentUploadingPodId = null
    }
  }

  /**
   * Handle upload failure - reset to SELECTED state so user can retry
   */
  fun onUploadFailure(podId: Int) {
    val index = podItems.indexOfFirst { it.id == podId }
    if (index != -1) {
      // Keep the image path so user can retry, just change state back to SELECTED
      podItems[index] = podItems[index].copy(
        state = PodState.SELECTED
      )
        Log.d("PodItems-Fail", "$podItems")
      podItemsLiveData.postValue(podItems)
      currentUploadingPodId = null
    }
  }

  /**
   * Delete a pod image (remove selection or uploaded image)
   */
  fun deletePod(podId: Int) {
    val index = podItems.indexOfFirst { it.id == podId }
    if (index != -1) {
      val podItem = podItems[index]

      // Remove from image paths and URLs
      podItem.imagePath?.let { imagePaths.remove(it) }
      podItem.imageUrl?.let { imageUrls.remove(it) }

      // Reset pod to available state (can select again)
      podItems[index] = podItem.copy(
        state = PodState.AVAILABLE,
        imagePath = null,
        imageUrl = null
      )

      // Hide all pods after this one (sequential availability)
      // Logic to reset subsequent pods removed to allow gaps and preserve selections
        Log.d("PodItems-Delete", "$podItems")
      podItemsLiveData.postValue(podItems)
    }
  }

  /**
   * Get all selected pods (for upload when Submit is clicked)
   */
  fun getSelectedPods(): List<PodItem> {
    return podItems.filter { it.state == PodState.SELECTED }
  }

  /**
   * Get current pod items list
   */
  fun getPodItems(): List<PodItem> = podItems

  /**
   * Upload POD
   */
  /**
   * Check if we are in the initial state (only first pod is available, nothing else)
   */
  fun isInitialState(): Boolean {
    val activePods = podItems.filter { it.state != PodState.EMPTY }
    return activePods.size == 1 && activePods[0].id == 1 && activePods[0].state == PodState.AVAILABLE
  }

  /**
   * Reset pods to all empty (used when cancelling initial upload)
   */
  fun resetPods() {
    initializePods()
  }

  fun uploadPod() {
    val podData = PODData(imageUrls, "axle-app", reachedTime, unloadedTime, "no", userRepository.userId(), userPrefs.userName, userPrefs.phoneNumber!!)
    val podRequest = PodRequest("TRP", "EPOD", transactionId, podData)

    // TODO: MOCKED RESPONSE
//    val mockSuccess = true // Change to true to mock success response
//
//    compositeDisposable += Single.just(mockSuccess)
//        .delay(1, TimeUnit.SECONDS) // Simulate network delay
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe { success ->
//          podSubmissionResultLiveData.postValue(success)
//        }

    // ACTUAL API CALL - Uncomment this block for production
    compositeDisposable += tripsRepository.uploadPod(transactionId, podRequest)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            podSubmissionResultLiveData.postValue(true)
          } else {
            podSubmissionResultLiveData.postValue(false)
            error.handle()
          }
        }
  }

}