package com.dfd.delfin.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.repository.SalesCodeRepository
import com.dfd.delfin.api.response.ValidateSalesCodeResponse
import com.dfd.delfin.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SalesCodeViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : BaseViewModel() {

    var salesCode: String = ""
    private val _validateState = MutableLiveData<Resource<ValidateSalesCodeResponse>?>()
    val validateState: LiveData<Resource<ValidateSalesCodeResponse>?> = _validateState

    fun validateSalesCode(salesCode: String) {
        viewModelScope.launch {
            showProgress()
            _validateState.value = Resource.Loading
            val result = salesCodeRepository.validateSalesCode(salesCode)
            showProgress(false)
            _validateState.value = result
        }
    }

    fun resetValidateState() {
        _validateState.value = null
    }
}
