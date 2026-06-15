package com.dfd.delfin.ui.fastag.tagAssignment.assign

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.FastagRepository
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.response.VehicleImageProcessResponse
import com.dfd.delfin.api.response.VehicleImageProcessStatusResponse
import com.dfd.delfin.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class VehicleImageUploadViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _uploadState = MutableLiveData<Resource<VehicleImageProcessResponse>>()
    val uploadState: LiveData<Resource<VehicleImageProcessResponse>> = _uploadState

    private val _processStatus = MutableLiveData<Resource<VehicleImageProcessStatusResponse>>()
    val processStatus: LiveData<Resource<VehicleImageProcessStatusResponse>> = _processStatus

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
        private const val POLL_TIMEOUT_MS = 30_000L
    }

    fun uploadVehicleImages(
        vehicleFront: MultipartBody.Part,
        vehicleSide: MultipartBody.Part,
        orderId: String,
        orderItemId: Int,
        journeyId: String
    ) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val orderIdPart = MultipartBody.Part.createFormData("order_id", orderId)
            val orderItemIdPart = MultipartBody.Part.createFormData("order_item_id", orderItemId.toString())
            val journeyIdPart = MultipartBody.Part.createFormData("journey_id", journeyId)
            val result = fastagRepository.uploadVehicleImages(vehicleFront, vehicleSide, orderIdPart, orderItemIdPart, journeyIdPart)
            _uploadState.value = result
        }
    }

    /**
     * Polls vehicle image processing status every 5 seconds for up to 30 seconds.
     * Stops early if status is COMPLETED or FAILED.
     */
    fun startPolling(jobId: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < POLL_TIMEOUT_MS) {
                val result = fastagRepository.getVehicleImageProcessStatus(jobId)

                if (result is Resource.Success) {
                    val status = result.data?.status?.uppercase()
                    if (status == "COMPLETED" || status == "FAILED" || status == "NOT_FOUND") {
                        _processStatus.postValue(result)
                        return@launch
                    }
                }

                delay(POLL_INTERVAL_MS)
            }

            // Timeout — post last known state or a timeout indicator
            _processStatus.postValue(
                Resource.Success(VehicleImageProcessStatusResponse(status = "TIMEOUT", data = null))
            )
        }
    }
}
