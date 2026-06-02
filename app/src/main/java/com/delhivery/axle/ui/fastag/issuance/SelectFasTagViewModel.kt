package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.request.CreateOrderRequest
import com.delhivery.axle.api.response.CreateOrderResponse
import com.delhivery.axle.api.response.KycOnboardValidateResponse
import com.delhivery.axle.api.response.VehicleClassResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectFasTagViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _vehicleClassesState = MutableLiveData<Resource<VehicleClassResponse>>()
    val vehicleClassesState: LiveData<Resource<VehicleClassResponse>> = _vehicleClassesState

    private val _createOrderState = MutableLiveData<Resource<CreateOrderResponse>>()
    val createOrderState: LiveData<Resource<CreateOrderResponse>> = _createOrderState

    private val _kycValidateState = MutableLiveData<Resource<KycOnboardValidateResponse>>()
    val kycValidateState: LiveData<Resource<KycOnboardValidateResponse>> = _kycValidateState

    fun fetchVehicleClasses() {
        viewModelScope.launch {
            _vehicleClassesState.value = Resource.Loading
            val result = salesCodeRepository.getVehicleClasses()
            _vehicleClassesState.value = result
        }
    }

    fun createOrder(request: CreateOrderRequest) {
        viewModelScope.launch {
            _createOrderState.value = Resource.Loading
            val result = salesCodeRepository.createOrder(request)
            _createOrderState.value = result
        }
    }

    fun kycOnboardValidate(bankCode: String) {
        viewModelScope.launch {
            _kycValidateState.value = Resource.Loading
            val result = salesCodeRepository.kycOnboardValidate(bankCode)
            _kycValidateState.value = result
        }
    }
}
