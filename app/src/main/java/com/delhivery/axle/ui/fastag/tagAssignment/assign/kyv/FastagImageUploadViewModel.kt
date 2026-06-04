package com.delhivery.axle.ui.fastag.tagAssignment.assign.kyv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.FastagImageUploadResponse
import com.delhivery.axle.api.response.FastagImageValidateResponse
import com.delhivery.axle.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class FastagImageUploadViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _uploadState = MutableLiveData<Resource<FastagImageUploadResponse>>()
    val uploadState: LiveData<Resource<FastagImageUploadResponse>> = _uploadState

    private val _validateState = MutableLiveData<Resource<FastagImageValidateResponse>>()
    val validateState: LiveData<Resource<FastagImageValidateResponse>> = _validateState

    fun uploadFastagImage(
        fastagImage: MultipartBody.Part,
        journeyId: String
    ) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val journeyIdPart = MultipartBody.Part.createFormData("journey_id", journeyId)
            val result = fastagRepository.uploadFastagImage(fastagImage, journeyIdPart)
            _uploadState.value = result
        }
    }

    fun validateFastagImage(journeyId: String) {
        viewModelScope.launch {
            _validateState.value = Resource.Loading
            val result = fastagRepository.validateFastagImage(journeyId)
            _validateState.value = result
        }
    }
}
