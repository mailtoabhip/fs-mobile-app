package com.dfd.delfin.ui.tripdetails

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.TripsRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.request.PODData
import com.dfd.delfin.api.request.PodRequest
import com.dfd.delfin.data.PodItem
import com.dfd.delfin.data.PodState
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [UploadImageActivity]
 */
class UploadImageViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsRepository: TripsRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

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

  // Removed getDelegationToken and delegationLiveData - uploads now handled directly by DocumentUtils

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
      Log.d("POD-Req", "$podRequest")

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