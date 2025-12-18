package com.delhivery.axle.ui.home.activity.docket

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.DispatchData
import com.delhivery.axle.api.request.UpdateDispatchRequest
// Removed AWS imports - using Document API now
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.DocketItem
import com.delhivery.axle.data.DocketState
import com.delhivery.axle.data.home.trips.HomeTripsItemData
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
  // Removed delegationLiveData - using Document API now
  var statusLiveData = MutableLiveData<Boolean>()
  var docketItemsLiveData = MutableLiveData<List<DocketItem>>()

  var transactionIds = mutableListOf<String>()
  var imagePath = ""
  var imageUrl = ""
  var dateOfDispatch = ""
  var trackingNumber = ""

  private var docketItems: MutableList<DocketItem> = mutableListOf()
  private var currentUploadingDocketId: Int? = null

  // Removed getDelegationToken - uploads now handled directly by DocumentUtils

  /**
   * Initialize Docket items - all start as EMPTY, will become available when user starts upload
   */
  fun initializeDockets() {
    docketItems = mutableListOf()
    // Initialize only 1 docket as empty initially (can be extended to more in future)
    for (i in 1..1) {
      docketItems.add(DocketItem(i, DocketState.EMPTY))
    }
    Log.d("DocketItems-Init", "$docketItems")
    docketItemsLiveData.postValue(docketItems)
  }

  /**
   * Start upload process - make first docket available
   */
  fun startUploadProcess() {
    if (docketItems.isEmpty()) {
      initializeDockets()
    }
    // Make first docket available
    if (docketItems.isNotEmpty() && docketItems[0].state == DocketState.EMPTY) {
      docketItems[0] = docketItems[0].copy(state = DocketState.AVAILABLE)
      Log.d("DocketItems-Start", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
    } else {
      Log.d("ViewModel", "Docket1 state is not EMPTY: ${docketItems.getOrNull(0)?.state}")
    }
  }

  /**
   * Load existing docket image (when trip is not null)
   */
  fun loadExistingDocket(imageUrl: String) {
    if (docketItems.isEmpty()) {
      initializeDockets()
    }
    // Set first docket as uploaded with the existing image URL
    if (docketItems.isNotEmpty()) {
      docketItems[0] = docketItems[0].copy(
        state = DocketState.UPLOADED,
        imageUrl = imageUrl
      )
      this.imageUrl = imageUrl
      Log.d("DocketItems-LoadExisting", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
    }
  }

  /**
   * Set image as selected (not uploaded yet)
   */
  fun setImageSelected(docketId: Int, filePath: String, imageUri: Uri? = null) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      docketItems[index] = docketItems[index].copy(
        state = DocketState.SELECTED,
        imagePath = filePath,
        imageUri = imageUri
      )
      imagePath = filePath
      Log.d("DocketItems-Selected", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
    }
  }

  /**
   * Start upload for docket (automatically when image is selected)
   */
  fun startUpload(docketId: Int, file: File) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      currentUploadingDocketId = docketId
      docketItems[index] = docketItems[index].copy(
        state = DocketState.UPLOADING
      )
      Log.d("DocketItems-StartUpload", "$docketItems")
      docketItemsLiveData.postValue(docketItems)

      // Upload is now handled directly in Activity using DocumentUtils
      // No need to get delegation token - removed AWS dependency
    }
  }

  /**
   * Handle successful upload
   */
  fun onUploadSuccess(docketId: Int, awsImageUrl: String) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      docketItems[index] = docketItems[index].copy(
        state = DocketState.UPLOADED,
        imageUrl = awsImageUrl
      )
      imageUrl = awsImageUrl
      Log.d("DocketItems-Success", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
      currentUploadingDocketId = null
    }
  }

  /**
   * Handle upload failure - reset to SELECTED state so user can retry
   */
  fun onUploadFailure(docketId: Int) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      // Keep the image path so user can retry, just change state back to SELECTED
      docketItems[index] = docketItems[index].copy(
        state = DocketState.SELECTED
      )
      Log.d("DocketItems-Fail", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
      currentUploadingDocketId = null
    }
  }

  /**
   * Update local image path for a docket (used after downloading/pre-fetching)
   */
  fun updateDocketLocalPath(docketId: Int, localPath: String) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      docketItems[index] = docketItems[index].copy(
        imagePath = localPath
      )
      Log.d("DocketItems-UpdatePath", "ID: $docketId, Path: $localPath")
      docketItemsLiveData.postValue(docketItems)
    }
  }

  /**
   * Delete a docket image (remove selection or uploaded image)
   */
  fun deleteDocket(docketId: Int) {
    val index = docketItems.indexOfFirst { it.id == docketId }
    if (index != -1) {
      val docketItem = docketItems[index]

      // Clear paths and URLs
      imagePath = ""
      imageUrl = ""

      // Reset docket to available state (can select again)
      docketItems[index] = docketItem.copy(
        state = DocketState.AVAILABLE,
        imagePath = null,
        imageUrl = null
      )

      Log.d("DocketItems-Delete", "$docketItems")
      docketItemsLiveData.postValue(docketItems)
    }
  }

  /**
   * Get all selected dockets (for upload when Submit is clicked)
   */
  fun getSelectedDockets(): List<DocketItem> {
    return docketItems.filter { it.state == DocketState.SELECTED }
  }

  /**
   * Reset dockets to all empty (used when cancelling initial upload)
   */
  fun resetDockets() {
    initializeDockets()
  }

  /**
   * Get current docket items list
   */
  fun getDocketItems(): List<DocketItem> = docketItems

  /**
   * Update disatch details
   * Note: Progress is controlled at Activity level to maintain continuous progress bar
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