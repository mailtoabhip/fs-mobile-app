package com.delhivery.axle.ui.fastag.tagAssignment.assign

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.RcProcessResponse
import com.delhivery.axle.api.response.RcProcessStatusResponse
import com.delhivery.axle.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class RCUploadViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _rcUploadState = MutableLiveData<Resource<RcProcessResponse>>()
    val rcUploadState: LiveData<Resource<RcProcessResponse>> = _rcUploadState

    private val _rcProcessStatus = MutableLiveData<Resource<RcProcessStatusResponse>?>()
    val rcProcessStatus: LiveData<Resource<RcProcessStatusResponse>?> = _rcProcessStatus

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
        private const val POLL_TIMEOUT_MS = 30_000L
    }

    fun uploadRcImages(
        rcFront: MultipartBody.Part,
        rcBack: MultipartBody.Part,
        orderId: String,
        orderItemId: Int
    ) {
        // Reset previous states to avoid stale observers firing
        _rcProcessStatus.value = null
        viewModelScope.launch {
            _rcUploadState.value = Resource.Loading
            val orderIdPart = MultipartBody.Part.createFormData("order_id", orderId)
            val orderItemIdPart = MultipartBody.Part.createFormData("order_item_id", orderItemId.toString())
            val result = fastagRepository.uploadRcImages(rcFront, rcBack, orderIdPart, orderItemIdPart)
            _rcUploadState.value = result
        }
    }

    /**
     * Polls RC processing status every 5 seconds for up to 30 seconds.
     * Stops early if status is COMPLETED or FAILED.
     */
    fun startRcPolling(jobId: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < POLL_TIMEOUT_MS) {
                val result = fastagRepository.getRcProcessStatus(jobId)

                if (result is Resource.Success) {
                    val status = result.data?.status?.uppercase()
                    if (status == "COMPLETED" || status == "FAILED" || status == "NOT_FOUND") {
                        _rcProcessStatus.postValue(result)
                        return@launch
                    }
                }

                delay(POLL_INTERVAL_MS)
            }

            // Timeout — polling exhausted while still pending
            _rcProcessStatus.postValue(
                Resource.Success(RcProcessStatusResponse(status = "TIMEOUT", currentStep = null, completedSteps = null, journeyId = null, skipVehicleImageUpload = null))
            )
        }
    }
}
