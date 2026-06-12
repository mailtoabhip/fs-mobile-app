package com.dfd.delfin.ui.fastag.tagAssignment.assign.kyv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.FastagRepository
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.response.FastagImageUploadResponse
import com.dfd.delfin.api.response.FastagImageValidateResponse
import com.dfd.delfin.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class KYVFastagImageUploadViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _uploadState = MutableLiveData<Resource<FastagImageUploadResponse>>()
    val uploadState: LiveData<Resource<FastagImageUploadResponse>> = _uploadState

    private val _validateState = MutableLiveData<Resource<FastagImageValidateResponse>>()
    val validateState: LiveData<Resource<FastagImageValidateResponse>> = _validateState

    fun uploadFastagImage(
        fastagImage: MultipartBody.Part,
        journeyId: String,
        orderId: String,
        orderItemId: String
    ) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val journeyIdPart = MultipartBody.Part.createFormData("journey_id", journeyId)
            val orderIdPart = MultipartBody.Part.createFormData("order_id", orderId)
            val orderItemIdPart = MultipartBody.Part.createFormData("order_item_id", orderItemId)
            val result = fastagRepository.uploadFastagImage(fastagImage, journeyIdPart, orderIdPart, orderItemIdPart)
            _uploadState.value = result
        }
    }

    fun validateFastagImage(journeyId: String, orderId: String, orderItemId: String) {
        viewModelScope.launch {
            _validateState.value = Resource.Loading
            val result = fastagRepository.validateFastagImage(journeyId, orderId, orderItemId)
            _validateState.value = result
        }
    }
}
