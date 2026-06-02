package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.response.ValidateSalesCodeResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class SalesCodeViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _validateState = MutableLiveData<Resource<ValidateSalesCodeResponse>>()
    val validateState: LiveData<Resource<ValidateSalesCodeResponse>> = _validateState

    fun validateSalesCode(salesCode: String) {
        viewModelScope.launch {
            _validateState.value = Resource.Loading
            val result = salesCodeRepository.validateSalesCode(salesCode)
            _validateState.value = result
        }
    }

    fun resetValidateState() {
        _validateState.value = null
    }
}
