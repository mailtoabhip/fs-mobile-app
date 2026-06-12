package com.dfd.delfin.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.repository.SalesCodeRepository
import com.dfd.delfin.api.request.CreateOrderRequest
import com.dfd.delfin.api.response.CreateOrderResponse
import com.dfd.delfin.api.response.KycOnboardValidateResponse
import com.dfd.delfin.api.response.VehicleClassResponse
import com.dfd.delfin.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectFasTagViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : BaseViewModel() {

    private val _vehicleClassesState = MutableLiveData<Resource<VehicleClassResponse>>()
    val vehicleClassesState: LiveData<Resource<VehicleClassResponse>> = _vehicleClassesState

    private val _createOrderState = MutableLiveData<Resource<CreateOrderResponse>>()
    val createOrderState: LiveData<Resource<CreateOrderResponse>> = _createOrderState

    private val _kycValidateState = MutableLiveData<Resource<KycOnboardValidateResponse>>()
    val kycValidateState: LiveData<Resource<KycOnboardValidateResponse>> = _kycValidateState

    fun fetchVehicleClasses() {
        viewModelScope.launch {
            showProgress()
            _vehicleClassesState.value = Resource.Loading
            val result = salesCodeRepository.getVehicleClasses()
            _vehicleClassesState.value = result
            showProgress(false)
        }
    }

    fun createOrder(request: CreateOrderRequest) {
        viewModelScope.launch {
            showProgress()
            _createOrderState.value = Resource.Loading
            val result = salesCodeRepository.createOrder(request)
            _createOrderState.value = result
            // Don't hide progress on success — kycOnboardValidate follows
            if (result is Resource.Failure) {
                showProgress(false)
            }
        }
    }

    fun kycOnboardValidate(bankCode: String) {
        viewModelScope.launch {
            showProgress()
            _kycValidateState.value = Resource.Loading
            val result = salesCodeRepository.kycOnboardValidate(bankCode)
            _kycValidateState.value = result
            showProgress(false)
        }
    }
}
